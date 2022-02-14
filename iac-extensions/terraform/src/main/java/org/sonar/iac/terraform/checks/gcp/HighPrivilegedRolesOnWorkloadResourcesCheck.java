/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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

import org.sonar.check.Rule;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.checks.ResourceVisitor;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static org.sonar.iac.terraform.checks.utils.PredicateUtils.containsMatchStringPredicate;
import static org.sonar.iac.terraform.checks.utils.PredicateUtils.exactMatchStringPredicate;
import static org.sonar.iac.terraform.checks.utils.PredicateUtils.treePredicate;

@Rule(key = "S6400")
public class HighPrivilegedRolesOnWorkloadResourcesCheck extends ResourceVisitor {

  private static final String MESSAGE_FOR_BINDING = "Make sure it is safe to give those members full access to the resource.";
  private static final String MESSAGE_FOR_MEMBER = "Make sure it is safe to grant that member full access to the resource.";
  private static final String MESSAGE_ON_GRANT_FULL_ACCESS = "Make sure it is safe to grant full access to the resource.";

  private static final Predicate<ExpressionTree> IS_OWNER_OR_MANAGER = treePredicate(exactMatchStringPredicate("MANAGER|OWNER", Pattern.CASE_INSENSITIVE));
  private static final Predicate<ExpressionTree> CONTAINS_PRIVILEGED_ROLE = treePredicate(containsMatchStringPredicate("ADMIN|MANAGER|OWNER|SUPERUSER", Pattern.CASE_INSENSITIVE));

  @Override
  protected void registerResourceConsumer() {
    register(List.of(IAM_BINDING_RESOURCE_NAMES),
      resource -> resource.attribute("role")
        .reportIf(CONTAINS_PRIVILEGED_ROLE, MESSAGE_FOR_BINDING));

    register(List.of(IAM_MEMBER_RESOURCE_NAMES),
      resource -> resource.attribute("role")
        .reportIf(CONTAINS_PRIVILEGED_ROLE, MESSAGE_FOR_MEMBER));

    register("google_cloud_identity_group",
      resource -> resource.blocks("roles").forEach(
        block -> block.attribute("name").reportIf(IS_OWNER_OR_MANAGER, MESSAGE_ON_GRANT_FULL_ACCESS)));
  }

  private static final String[] IAM_BINDING_RESOURCE_NAMES = {
    // 56 *_iam_binding variants:
    "google_apigee_environment_iam_binding",
    "google_api_gateway_api_config_iam_binding",
    "google_api_gateway_api_iam_binding",
    "google_api_gateway_gateway_iam_binding",
    "google_artifact_registry_repository_iam_binding",
    "google_bigquery_dataset_iam_binding",
    "google_bigquery_table_iam_binding",
    "google_bigtable_instance_iam_binding",
    "google_bigtable_table_iam_binding",
    "google_billing_account_iam_binding",
    "google_binary_authorization_attestor_iam_binding",
    "google_cloudfunctions_function_iam_binding",
    "google_cloud_run_service_iam_binding",
    "google_compute_disk_iam_binding",
    "google_compute_image_iam_binding",
    "google_compute_instance_iam_binding",
    "google_compute_machine_image_iam_binding",
    "google_compute_region_disk_iam_binding",
    "google_compute_subnetwork_iam_binding",
    "google_dataproc_cluster_iam_binding",
    "google_dataproc_job_iam_binding",
    "google_data_catalog_entry_group_iam_binding",
    "google_data_catalog_policy_tag_iam_binding",
    "google_data_catalog_tag_template_iam_binding",
    "google_data_catalog_taxonomy_iam_binding",
    "google_endpoints_service_iam_binding",
    "google_kms_crypto_key_iam_binding",
    "google_kms_key_ring_iam_binding",
    "google_healthcare_consent_store_iam_binding",
    "google_healthcare_dataset_iam_binding",
    "google_healthcare_dicom_store_iam_binding",
    "google_healthcare_fhir_store_iam_binding",
    "google_healthcare_hl7_v2_store_iam_binding",
    "google_iap_app_engine_service_iam_binding",
    "google_iap_app_engine_version_iam_binding",
    "google_iap_tunnel_iam_binding",
    "google_iap_tunnel_instance_iam_binding",
    "google_iap_web_backend_service_iam_binding",
    "google_iap_web_iam_binding",
    "google_iap_web_type_app_engine_iam_binding",
    "google_iap_web_type_compute_iam_binding",
    "google_notebooks_instance_iam_binding",
    "google_notebooks_runtime_iam_binding",
    "google_privateca_ca_pool_iam_binding",
    "google_pubsub_subscription_iam_binding",
    "google_pubsub_topic_iam_binding",
    "google_runtimeconfig_config_iam_binding",
    "google_secret_manager_secret_iam_binding",
    "google_service_directory_namespace_iam_binding",
    "google_service_directory_service_iam_binding",
    "google_sourcerepo_repository_iam_binding",
    "google_spanner_database_iam_binding",
    "google_spanner_instance_iam_binding",
    "google_storage_bucket_iam_binding",
    "google_tags_tag_key_iam_binding",
    "google_tags_tag_value_iam_binding"
  };

  private static final String[] IAM_MEMBER_RESOURCE_NAMES = {
    // and their corresponding 56 *_iam_member variants:
    "google_apigee_environment_iam_member",
    "google_api_gateway_api_config_iam_member",
    "google_api_gateway_api_iam_member",
    "google_api_gateway_gateway_iam_member",
    "google_artifact_registry_repository_iam_member",
    "google_bigquery_dataset_iam_member",
    "google_bigquery_table_iam_member",
    "google_bigtable_instance_iam_member",
    "google_bigtable_table_iam_member",
    "google_billing_account_iam_member",
    "google_binary_authorization_attestor_iam_member",
    "google_cloudfunctions_function_iam_member",
    "google_cloud_run_service_iam_member",
    "google_compute_disk_iam_member",
    "google_compute_image_iam_member",
    "google_compute_instance_iam_member",
    "google_compute_machine_image_iam_member",
    "google_compute_region_disk_iam_member",
    "google_compute_subnetwork_iam_member",
    "google_dataproc_cluster_iam_member",
    "google_dataproc_job_iam_member",
    "google_data_catalog_entry_group_iam_member",
    "google_data_catalog_policy_tag_iam_member",
    "google_data_catalog_tag_template_iam_member",
    "google_data_catalog_taxonomy_iam_member",
    "google_endpoints_service_iam_member",
    "google_kms_crypto_key_iam_member",
    "google_kms_key_ring_iam_member",
    "google_healthcare_consent_store_iam_member",
    "google_healthcare_dataset_iam_member",
    "google_healthcare_dicom_store_iam_member",
    "google_healthcare_fhir_store_iam_member",
    "google_healthcare_hl7_v2_store_iam_member",
    "google_iap_app_engine_service_iam_member",
    "google_iap_app_engine_version_iam_member",
    "google_iap_tunnel_iam_member",
    "google_iap_tunnel_instance_iam_member",
    "google_iap_web_backend_service_iam_member",
    "google_iap_web_iam_member",
    "google_iap_web_type_app_engine_iam_member",
    "google_iap_web_type_compute_iam_member",
    "google_notebooks_instance_iam_member",
    "google_notebooks_runtime_iam_member",
    "google_privateca_ca_pool_iam_member",
    "google_pubsub_subscription_iam_member",
    "google_pubsub_topic_iam_member",
    "google_runtimeconfig_config_iam_member",
    "google_secret_manager_secret_iam_member",
    "google_service_directory_namespace_iam_member",
    "google_service_directory_service_iam_member",
    "google_sourcerepo_repository_iam_member",
    "google_spanner_database_iam_member",
    "google_spanner_instance_iam_member",
    "google_storage_bucket_iam_member",
    "google_tags_tag_key_iam_member",
    "google_tags_tag_value_iam_member",
  };
}
