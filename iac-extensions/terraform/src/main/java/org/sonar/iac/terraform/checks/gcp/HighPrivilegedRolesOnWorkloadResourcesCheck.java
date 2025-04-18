/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks.gcp;

import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.matchesPattern;

@Rule(key = "S6400")
public class HighPrivilegedRolesOnWorkloadResourcesCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE_FOR_BINDING = "Make sure it is safe to give those members full access to the resource.";
  private static final String MESSAGE_FOR_MEMBER = "Make sure it is safe to grant that member full access to the resource.";
  private static final String MESSAGE_ON_GRANT_FULL_ACCESS = "Make sure it is safe to grant full access to the resource.";
  private static final String SECONDARY_MESSAGE = "The policy is used here.";

  private static final String SENSITIVE_ROLES = ".*(?:admin|developer|manager|owner|superuser)(?:\\.?v\\d+)?";

  private final PolicyReferenceCollector collector = new PolicyReferenceCollector(IAM_POLICY_RESOURCE_TYPES);

  @Override
  public void initialize(InitContext init) {
    super.initialize(init);
    init.register(FileTree.class, (ctx, tree) -> collector.scan(new TreeContext(), tree));
  }

  @Override
  protected void registerResourceConsumer() {
    register(List.of(IAM_BINDING_RESOURCE_NAMES),
      resource -> resource.attribute("role")
        .reportIf(matchesPattern(SENSITIVE_ROLES), MESSAGE_FOR_BINDING));

    register(List.of(IAM_MEMBER_RESOURCE_NAMES),
      resource -> resource.attribute("role")
        .reportIf(matchesPattern(SENSITIVE_ROLES), MESSAGE_FOR_MEMBER));

    register("google_cloud_identity_group",
      resource -> resource.blocks("roles").forEach(
        block -> block.attribute("name")
          .reportIf(matchesPattern("MANAGER|OWNER"), MESSAGE_ON_GRANT_FULL_ACCESS)));

    register(List.of(
      "google_bigquery_dataset_access",
      "google_storage_bucket_access_control",
      "google_storage_default_object_access_control",
      "google_storage_object_access_control"),
      resource -> resource.attribute("role")
        .reportIf(equalTo("OWNER"), MESSAGE_ON_GRANT_FULL_ACCESS));

    register(List.of(
      "google_storage_bucket_acl",
      "google_storage_default_object_acl",
      "google_storage_object_acl"),
      resource -> resource.list("role_entity")
        .reportItemIf(matchesPattern("OWNER:.*"), MESSAGE_ON_GRANT_FULL_ACCESS));
  }

  @Override
  protected void provideResource(CheckContext ctx, BlockTree blockTree) {
    super.provideResource(ctx, blockTree);
    if (isDataOfType(blockTree, "google_iam_policy")) {
      ResourceSymbol dataData = ResourceSymbol.fromPresent(ctx, blockTree);
      collector.checkPolicy(dataData, matchesPattern(SENSITIVE_ROLES), MESSAGE_FOR_BINDING, SECONDARY_MESSAGE);
    }
  }

  private static final String[] IAM_BINDING_RESOURCE_NAMES = {
    // 108 *_iam_binding variants:
    "google_access_context_manager_access_policy_iam_binding",
    "google_api_gateway_api_config_iam_binding",
    "google_api_gateway_api_iam_binding",
    "google_api_gateway_gateway_iam_binding",
    "google_apigee_environment_iam_binding",
    "google_artifact_registry_repository_iam_binding",
    "google_bigquery_analytics_hub_data_exchange_iam_binding",
    "google_bigquery_analytics_hub_listing_iam_binding",
    "google_bigquery_connection_iam_binding",
    "google_bigquery_datapolicy_data_policy_iam_binding",
    "google_bigquery_dataset_iam_binding",
    "google_bigquery_table_iam_binding",
    "google_bigtable_instance_iam_binding",
    "google_bigtable_table_iam_binding",
    "google_billing_account_iam_binding",
    "google_binary_authorization_attestor_iam_binding",
    "google_cloud_run_service_iam_binding",
    "google_cloud_run_v2_job_iam_binding",
    "google_cloud_run_v2_service_iam_binding",
    "google_cloud_tasks_queue_iam_binding",
    "google_cloudbuildv2_connection_iam_binding",
    "google_clouddeploy_custom_target_type_iam_binding",
    "google_clouddeploy_delivery_pipeline_iam_binding",
    "google_clouddeploy_target_iam_binding",
    "google_cloudfunctions2_function_iam_binding",
    "google_cloudfunctions_function_iam_binding",
    "google_compute_backend_bucket_iam_binding",
    "google_compute_backend_service_iam_binding",
    "google_compute_disk_iam_binding",
    "google_compute_image_iam_binding",
    "google_compute_instance_iam_binding",
    "google_compute_machine_image_iam_binding",
    "google_compute_region_backend_service_iam_binding",
    "google_compute_region_disk_iam_binding",
    "google_compute_snapshot_iam_binding",
    "google_compute_subnetwork_iam_binding",
    "google_container_analysis_note_iam_binding",
    "google_data_catalog_entry_group_iam_binding",
    "google_data_catalog_policy_tag_iam_binding",
    "google_data_catalog_tag_template_iam_binding",
    "google_data_catalog_taxonomy_iam_binding",
    "google_data_fusion_instance_iam_binding",
    "google_dataform_repository_iam_binding",
    "google_dataplex_aspect_type_iam_binding",
    "google_dataplex_asset_iam_binding",
    "google_dataplex_datascan_iam_binding",
    "google_dataplex_entry_group_iam_binding",
    "google_dataplex_entry_type_iam_binding",
    "google_dataplex_lake_iam_binding",
    "google_dataplex_task_iam_binding",
    "google_dataplex_zone_iam_binding",
    "google_dataproc_autoscaling_policy_iam_binding",
    "google_dataproc_cluster_iam_binding",
    "google_dataproc_job_iam_binding",
    "google_dataproc_metastore_federation_iam_binding",
    "google_dataproc_metastore_service_iam_binding",
    "google_dns_managed_zone_iam_binding",
    "google_endpoints_service_iam_binding",
    "google_gke_backup_backup_plan_iam_binding",
    "google_gke_backup_restore_plan_iam_binding",
    "google_gke_hub_feature_iam_binding",
    "google_gke_hub_membership_iam_binding",
    "google_gke_hub_scope_iam_binding",
    "google_healthcare_consent_store_iam_binding",
    "google_healthcare_dataset_iam_binding",
    "google_healthcare_dicom_store_iam_binding",
    "google_healthcare_fhir_store_iam_binding",
    "google_healthcare_hl7_v2_store_iam_binding",
    "google_iap_tunnel_dest_group_iam_binding",
    "google_iap_tunnel_iam_binding",
    "google_iap_tunnel_instance_iam_binding",
    "google_iap_web_backend_service_iam_binding",
    "google_iap_web_iam_binding",
    "google_iap_web_region_backend_service_iam_binding",
    "google_iap_web_type_app_engine_iam_binding",
    "google_iap_web_type_compute_iam_binding",
    "google_kms_crypto_key_iam_binding",
    "google_kms_ekm_connection_iam_binding",
    "google_kms_key_ring_iam_binding",
    "google_logging_log_view_iam_binding",
    "google_notebooks_instance_iam_binding",
    "google_notebooks_runtime_iam_binding",
    "google_privateca_ca_pool_iam_binding",
    "google_privateca_certificate_template_iam_binding",
    "google_pubsub_schema_iam_binding",
    "google_pubsub_subscription_iam_binding",
    "google_pubsub_topic_iam_binding",
    "google_runtimeconfig_config_iam_binding",
    "google_scc_source_iam_binding",
    "google_scc_v2_organization_source_iam_binding",
    "google_secret_manager_regional_secret_iam_binding",
    "google_secret_manager_secret_iam_binding",
    "google_secure_source_manager_instance_iam_binding",
    "google_secure_source_manager_repository_iam_binding",
    "google_service_directory_namespace_iam_binding",
    "google_service_directory_service_iam_binding",
    "google_sourcerepo_repository_iam_binding",
    "google_spanner_database_iam_binding",
    "google_spanner_instance_iam_binding",
    "google_storage_bucket_iam_binding",
    "google_storage_managed_folder_iam_binding",
    "google_tags_tag_key_iam_binding",
    "google_tags_tag_value_iam_binding",
    "google_vertex_ai_endpoint_iam_binding",
    "google_vertex_ai_featurestore_entitytype_iam_binding",
    "google_vertex_ai_featurestore_iam_binding",
    "google_workbench_instance_iam_binding",
    "google_workstations_workstation_config_iam_binding"
  };

  private static final String[] IAM_MEMBER_RESOURCE_NAMES = {
    // and their corresponding 108 *_iam_member variants:
    "google_access_context_manager_access_policy_iam_member",
    "google_api_gateway_api_config_iam_member",
    "google_api_gateway_api_iam_member",
    "google_api_gateway_gateway_iam_member",
    "google_apigee_environment_iam_member",
    "google_artifact_registry_repository_iam_member",
    "google_bigquery_analytics_hub_data_exchange_iam_member",
    "google_bigquery_analytics_hub_listing_iam_member",
    "google_bigquery_connection_iam_member",
    "google_bigquery_datapolicy_data_policy_iam_member",
    "google_bigquery_dataset_iam_member",
    "google_bigquery_table_iam_member",
    "google_bigtable_instance_iam_member",
    "google_bigtable_table_iam_member",
    "google_billing_account_iam_member",
    "google_binary_authorization_attestor_iam_member",
    "google_cloud_run_service_iam_member",
    "google_cloud_run_v2_job_iam_member",
    "google_cloud_run_v2_service_iam_member",
    "google_cloud_tasks_queue_iam_member",
    "google_cloudbuildv2_connection_iam_member",
    "google_clouddeploy_custom_target_type_iam_member",
    "google_clouddeploy_delivery_pipeline_iam_member",
    "google_clouddeploy_target_iam_member",
    "google_cloudfunctions2_function_iam_member",
    "google_cloudfunctions_function_iam_member",
    "google_compute_backend_bucket_iam_member",
    "google_compute_backend_service_iam_member",
    "google_compute_disk_iam_member",
    "google_compute_image_iam_member",
    "google_compute_instance_iam_member",
    "google_compute_machine_image_iam_member",
    "google_compute_region_backend_service_iam_member",
    "google_compute_region_disk_iam_member",
    "google_compute_snapshot_iam_member",
    "google_compute_subnetwork_iam_member",
    "google_container_analysis_note_iam_member",
    "google_data_catalog_entry_group_iam_member",
    "google_data_catalog_policy_tag_iam_member",
    "google_data_catalog_tag_template_iam_member",
    "google_data_catalog_taxonomy_iam_member",
    "google_data_fusion_instance_iam_member",
    "google_dataform_repository_iam_member",
    "google_dataplex_aspect_type_iam_member",
    "google_dataplex_asset_iam_member",
    "google_dataplex_datascan_iam_member",
    "google_dataplex_entry_group_iam_member",
    "google_dataplex_entry_type_iam_member",
    "google_dataplex_lake_iam_member",
    "google_dataplex_task_iam_member",
    "google_dataplex_zone_iam_member",
    "google_dataproc_autoscaling_policy_iam_member",
    "google_dataproc_cluster_iam_member",
    "google_dataproc_job_iam_member",
    "google_dataproc_metastore_federation_iam_member",
    "google_dataproc_metastore_service_iam_member",
    "google_dns_managed_zone_iam_member",
    "google_endpoints_service_iam_member",
    "google_gke_backup_backup_plan_iam_member",
    "google_gke_backup_restore_plan_iam_member",
    "google_gke_hub_feature_iam_member",
    "google_gke_hub_membership_iam_member",
    "google_gke_hub_scope_iam_member",
    "google_healthcare_consent_store_iam_member",
    "google_healthcare_dataset_iam_member",
    "google_healthcare_dicom_store_iam_member",
    "google_healthcare_fhir_store_iam_member",
    "google_healthcare_hl7_v2_store_iam_member",
    "google_iap_tunnel_dest_group_iam_member",
    "google_iap_tunnel_iam_member",
    "google_iap_tunnel_instance_iam_member",
    "google_iap_web_backend_service_iam_member",
    "google_iap_web_iam_member",
    "google_iap_web_region_backend_service_iam_member",
    "google_iap_web_type_app_engine_iam_member",
    "google_iap_web_type_compute_iam_member",
    "google_kms_crypto_key_iam_member",
    "google_kms_ekm_connection_iam_member",
    "google_kms_key_ring_iam_member",
    "google_logging_log_view_iam_member",
    "google_notebooks_instance_iam_member",
    "google_notebooks_runtime_iam_member",
    "google_privateca_ca_pool_iam_member",
    "google_privateca_certificate_template_iam_member",
    "google_pubsub_schema_iam_member",
    "google_pubsub_subscription_iam_member",
    "google_pubsub_topic_iam_member",
    "google_runtimeconfig_config_iam_member",
    "google_scc_source_iam_member",
    "google_scc_v2_organization_source_iam_member",
    "google_secret_manager_regional_secret_iam_member",
    "google_secret_manager_secret_iam_member",
    "google_secure_source_manager_instance_iam_member",
    "google_secure_source_manager_repository_iam_member",
    "google_service_directory_namespace_iam_member",
    "google_service_directory_service_iam_member",
    "google_sourcerepo_repository_iam_member",
    "google_spanner_database_iam_member",
    "google_spanner_instance_iam_member",
    "google_storage_bucket_iam_member",
    "google_storage_managed_folder_iam_member",
    "google_tags_tag_key_iam_member",
    "google_tags_tag_value_iam_member",
    "google_vertex_ai_endpoint_iam_member",
    "google_vertex_ai_featurestore_entitytype_iam_member",
    "google_vertex_ai_featurestore_iam_member",
    "google_workbench_instance_iam_member",
    "google_workstations_workstation_config_iam_member"
  };

  private static final String[] IAM_POLICY_RESOURCE_NAMES = {
    "google_apigee_environment_iam_policy",
    "google_api_gateway_api_config_iam_policy",
    "google_api_gateway_api_iam_policy",
    "google_api_gateway_gateway_iam_policy",
    "google_artifact_registry_repository_iam_policy",
    "google_bigquery_dataset_iam_policy",
    "google_bigquery_table_iam_policy",
    "google_bigtable_instance_iam_policy",
    "google_bigtable_table_iam_policy",
    "google_billing_account_iam_policy",
    "google_binary_authorization_attestor_iam_policy",
    "google_cloudfunctions_function_iam_policy",
    "google_cloud_run_service_iam_policy",
    "google_compute_disk_iam_policy",
    "google_compute_image_iam_policy",
    "google_compute_instance_iam_policy",
    "google_compute_machine_image_iam_policy",
    "google_compute_region_disk_iam_policy",
    "google_compute_subnetwork_iam_policy",
    "google_dataproc_cluster_iam_policy",
    "google_dataproc_job_iam_policy",
    "google_data_catalog_entry_group_iam_policy",
    "google_data_catalog_policy_tag_iam_policy",
    "google_data_catalog_tag_template_iam_policy",
    "google_data_catalog_taxonomy_iam_policy",
    "google_endpoints_service_iam_policy",
    "google_kms_crypto_key_iam_policy",
    "google_kms_key_ring_iam_policy",
    "google_healthcare_consent_store_iam_policy",
    "google_healthcare_dataset_iam_policy",
    "google_healthcare_dicom_store_iam_policy",
    "google_healthcare_fhir_store_iam_policy",
    "google_healthcare_hl7_v2_store_iam_policy",
    "google_iap_app_engine_service_iam_policy",
    "google_iap_app_engine_version_iam_policy",
    "google_iap_tunnel_iam_policy",
    "google_iap_tunnel_instance_iam_policy",
    "google_iap_web_backend_service_iam_policy",
    "google_iap_web_iam_policy",
    "google_iap_web_type_app_engine_iam_policy",
    "google_iap_web_type_compute_iam_policy",
    "google_notebooks_instance_iam_policy",
    "google_notebooks_runtime_iam_policy",
    "google_privateca_ca_pool_iam_policy",
    "google_pubsub_subscription_iam_policy",
    "google_pubsub_topic_iam_policy",
    "google_runtimeconfig_config_iam_policy",
    "google_secret_manager_secret_iam_policy",
    "google_service_directory_namespace_iam_policy",
    "google_service_directory_service_iam_policy",
    "google_sourcerepo_repository_iam_policy",
    "google_spanner_database_iam_policy",
    "google_spanner_instance_iam_policy",
    "google_storage_bucket_iam_policy",
    "google_tags_tag_key_iam_policy",
    "google_tags_tag_value_iam_policy"
  };

  private static final Set<String> IAM_POLICY_RESOURCE_TYPES = Set.of(IAM_POLICY_RESOURCE_NAMES);
}
