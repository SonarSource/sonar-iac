/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.terraform.checks.gcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.StatementTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.AttributeSymbol;
import org.sonar.iac.terraform.symbols.BlockSymbol;
import org.sonar.iac.terraform.symbols.ReferenceSymbol;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.terraform.api.tree.TerraformTree.Kind.BLOCK;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.matchesPattern;

@Rule(key = "S6404")
public class PublicAccessCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Ensure that granting public access to this resource is safe here.";
  private static final String SECONDARY_MESSAGE = "Excessive granting of permissions.";
  private static final String OMITTING_DNS = "Omitting %s will grant public access to this managed zone. Ensure it is safe here.";
  private static final String OMITTING_KUBERNETES = "Omitting %s grants public access to parts of this cluster. Make sure it is safe here.";
  private static final String MESSAGE_KUBERNETES = "Ensure that granting public access is safe here.";

  private static final String GCP_RESOURCE_PREFIX = "google_";

  private static final List<String> IAM_RESOURCES = List.of("apigee_environment", "api_gateway_api_config", "api_gateway_api",
    "api_gateway_gateway", "artifact_registry_repository", "bigquery_dataset", "bigquery_table", "bigtable_instance", "bigtable_table",
    "billing_account", "binary_authorization_attestor", "cloudfunctions_function", "cloud_run_service", "compute_disk",
    "compute_image", "compute_instance", "compute_machine_image", "compute_region_disk", "compute_subnetwork", "dataproc_cluster",
    "dataproc_job", "data_catalog_entry_group", "data_catalog_policy_tag", "data_catalog_tag_template", "data_catalog_taxonomy",
    "endpoints_service", "kms_crypto_key", "kms_key_ring", "healthcare_consent_store", "healthcare_dataset", "healthcare_dicom_store",
    "healthcare_fhir_store", "healthcare_hl7_v2_store", "iap_app_engine_service", "iap_app_engine_version", "iap_tunnel",
    "iap_tunnel_instance", "iap_web_backend_service", "iap_web", "iap_web_type_app_engine", "iap_web_type_compute", "notebooks_instance",
    "notebooks_runtime", "privateca_ca_pool", "pubsub_subscription", "pubsub_topic", "runtimeconfig_config", "secret_manager_secret",
    "service_directory_namespace", "service_directory_service", "sourcerepo_repository", "spanner_database", "spanner_instance",
    "storage_bucket", "tags_tag_key", "tags_tag_value", "project", "organization", "service_account", "folder");

  private static final String CONTAINS_SENSITIVE_MEMBER = ".*all(Authenticated)?Users.*";

  private Map<String, BlockSymbol> policyDataCollection = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, this::collectPolicyData);
    super.initialize(init);
  }

  private void collectPolicyData(CheckContext ctx, FileTree file) {
    policyDataCollection = file.properties().stream()
      .filter(PublicAccessCheck::isPolicyDataBlock)
      .map(BlockTree.class::cast)
      .collect(Collectors.toMap(data -> String.format("data.google_iam_policy.%s.policy_data", getName(data)), data -> ResourceSymbol.fromPresent(ctx, data)));
  }

  @Override
  protected void registerResourceConsumer() {
    register(iamResourceNameList("_iam_binding"),
      resource -> resource.list("members")
        .reportItemIf(matchesPattern(CONTAINS_SENSITIVE_MEMBER), MESSAGE));

    register(iamResourceNameList("_iam_member"),
      resource -> resource.attribute("member")
        .reportIf(matchesPattern(CONTAINS_SENSITIVE_MEMBER), MESSAGE));

    register(List.of("google_storage_default_object_access_control", "google_storage_object_access_control"),
      resource -> resource.attribute("entity")
        .reportIf(matchesPattern("all(Authenticated)?Users"), MESSAGE));

    register("google_bigquery_dataset_access",
      resource -> resource.attribute("special_group")
        .reportIf(matchesPattern("all(Authenticated)?Users"), MESSAGE));

    register(List.of("google_storage_bucket_acl",
      "google_storage_default_object_acl",
      "google_storage_object_acl"),
      resource -> resource.list("role_entity")
        .reportItemIf(matchesPattern(".*:all(Authenticated)?Users"), MESSAGE));

    register("google_dns_managed_zone",
      resource -> resource.attribute("visibility")
        .reportIf(equalTo("public"), MESSAGE)
        .reportIfAbsent(OMITTING_DNS));

    register("google_container_cluster",
      resource -> {
        BlockSymbol config = resource.block("private_cluster_config");
        config.reportIfAbsent(OMITTING_KUBERNETES);
        AttributeSymbol nodes = config.attribute("enable_private_nodes");
        AttributeSymbol endpoint = config.attribute("enable_private_endpoint");

        if (nodes.isAbsent() && endpoint.isAbsent()) {
          config.report(String.format(OMITTING_KUBERNETES, "enable_private_nodes and enable_private_endpoint"));
        } else if (nodes.is(isFalse()) && endpoint.is(isFalse())) {
          nodes.report(MESSAGE_KUBERNETES, endpoint.toSecondary(MESSAGE_KUBERNETES));
        } else {
          Stream.of(nodes, endpoint)
            .forEach(symbol -> symbol.reportIf(isFalse(), MESSAGE_KUBERNETES)
              .reportIfAbsent(OMITTING_KUBERNETES));
        }
      });

    register(iamResourceNameList("_iam_policy"),
      resource -> {
        ReferenceSymbol reference = resource.reference("policy_data");
        List<SecondaryLocation> sensitiveMemberBindings = new ArrayList<>();

        reference.resolve(policyDataCollection).blocks("binding")
          .forEach(block -> block.list("members")
            .getItemIf(matchesPattern(CONTAINS_SENSITIVE_MEMBER))
            .forEach(sensitiveMember -> sensitiveMemberBindings.add(new SecondaryLocation(sensitiveMember, SECONDARY_MESSAGE))));

        if (!sensitiveMemberBindings.isEmpty()) {
          reference.report(MESSAGE, sensitiveMemberBindings);
        }
      });
  }

  private static List<String> iamResourceNameList(String suffix) {
    return IAM_RESOURCES.stream()
      .map(resourceName -> GCP_RESOURCE_PREFIX + resourceName + suffix)
      .collect(Collectors.toList());
  }

  private static boolean isPolicyDataBlock(StatementTree statement) {
    return statement.is(BLOCK) && isDataOfType((BlockTree) statement, "google_iam_policy") && getName((BlockTree) statement) != null;
  }

  @CheckForNull
  private static String getName(BlockTree block) {
    return block.labels().size() >= 2 ? block.labels().get(1).value() : null;
  }
}
