package org.sonar.iac.terraform.checks.gcp;

import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;


public class IamResourcesCheckPart extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that assigning the %s role is safe here.";

  private static final String[] PRIVILEGED_ROLES = {"ADMIN", "MANAGER", "OWNER", "SUPERUSER"};

  @Override
  protected void registerResourceChecks() {
    register(IamResourcesCheckPart::checkIamResources, IAM_RESOURCE_NAMES);
  }

  private static void checkIamResources(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "role", AttributeTree.class)
      .filter(attr -> TextUtils.matchesValue(attr.value(), IamResourcesCheckPart::isaPrivilegedRole).isTrue())
      .ifPresent(attr -> ctx.reportIssue(attr, String.format(MESSAGE, ((TextTree) attr.value()).value())));
  }

  private static boolean isaPrivilegedRole(String roleName) {
    // Bear in mind PRIVILEGED_ROLES are all CAPS:
    roleName = roleName.toUpperCase();
    for (String privilegedRole : PRIVILEGED_ROLES) {
      if (roleName.contains(privilegedRole)) {
        return true;
      }
    }
    return false;
  }

  String[] IAM_RESOURCE_NAMES = {
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
    "google_tags_tag_value_iam_binding",

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
