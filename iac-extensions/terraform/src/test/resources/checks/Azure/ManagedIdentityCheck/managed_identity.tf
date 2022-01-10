# Noncompliant@+1 {{Omitting the `identity` block disables Azure Managed Identities. Make sure that it is safe here.}}
resource "azurerm_kubernetes_cluster" "sensitive" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name = "example-k8s"
}

resource "azurerm_kubernetes_cluster" "compliant" {
  name = "example-k8s"
  identity {
    type = "SystemAssigned"
  }
}

resource "some_other_type" "coverage" {
}

resource "azurerm_api_management" "sensitive" {} // Noncompliant
resource "azurerm_application_gateway" "sensitive" {} // Noncompliant
resource "azurerm_app_configuration" "sensitive" {} // Noncompliant
resource "azurerm_app_service" "sensitive" {} // Noncompliant
resource "azurerm_app_service_slot" "sensitive" {} // Noncompliant
resource "azurerm_batch_account" "sensitive" {} // Noncompliant
resource "azurerm_batch_pool" "sensitive" {} // Noncompliant
resource "azurerm_cognitive_account" "sensitive" {} // Noncompliant
resource "azurerm_container_group" "sensitive" {} // Noncompliant
resource "azurerm_container_registry" "sensitive" {} // Noncompliant
resource "azurerm_cosmosdb_account" "sensitive" {} // Noncompliant
resource "azurerm_data_factory" "sensitive" {} // Noncompliant
resource "azurerm_data_protection_backup_vault" "sensitive" {} // Noncompliant
resource "azurerm_eventgrid_domain" "sensitive" {} // Noncompliant
resource "azurerm_eventgrid_system_topic" "sensitive" {} // Noncompliant
resource "azurerm_eventgrid_topic" "sensitive" {} // Noncompliant
resource "azurerm_eventhub_namespace" "sensitive" {} // Noncompliant
resource "azurerm_express_route_port" "sensitive" {} // Noncompliant
resource "azurerm_firewall_policy" "sensitive" {} // Noncompliant
resource "azurerm_function_app" "sensitive" {} // Noncompliant
resource "azurerm_function_app_slot" "sensitive" {} // Noncompliant
resource "azurerm_kubernetes_cluster" "sensitive" {} // Noncompliant
resource "azurerm_kusto_cluster" "sensitive" {} // Noncompliant
resource "azurerm_linux_virtual_machine" "sensitive" {} // Noncompliant
resource "azurerm_linux_virtual_machine_scale_set" "sensitive" {} // Noncompliant
resource "azurerm_linux_web_app" "sensitive" {} // Noncompliant
resource "azurerm_logic_app_standard" "sensitive" {} // Noncompliant
resource "azurerm_machine_learning_compute_cluster" "sensitive" {} // Noncompliant
resource "azurerm_machine_learning_compute_instance" "sensitive" {} // Noncompliant
resource "azurerm_machine_learning_inference_cluster" "sensitive" {} // Noncompliant
resource "azurerm_machine_learning_synapse_spark" "sensitive" {} // Noncompliant
resource "azurerm_management_group_policy_assignment" "sensitive" {} // Noncompliant
resource "azurerm_media_services_account" "sensitive" {} // Noncompliant
resource "azurerm_mssql_server" "sensitive" {} // Noncompliant
resource "azurerm_mysql_server" "sensitive" {} // Noncompliant
resource "azurerm_policy_assignment" "sensitive" {} // Noncompliant
resource "azurerm_postgresql_server" "sensitive" {} // Noncompliant
resource "azurerm_purview_account" "sensitive" {} // Noncompliant
resource "azurerm_recovery_services_vault" "sensitive" {} // Noncompliant
resource "azurerm_resource_group_policy_assignment" "sensitive" {} // Noncompliant
resource "azurerm_resource_policy_assignment" "sensitive" {} // Noncompliant
resource "azurerm_search_service" "sensitive" {} // Noncompliant
resource "azurerm_spring_cloud_app" "sensitive" {} // Noncompliant
resource "azurerm_sql_server" "sensitive" {} // Noncompliant
resource "azurerm_storage_account" "sensitive" {} // Noncompliant
resource "azurerm_stream_analytics_job" "sensitive" {} // Noncompliant
resource "azurerm_subscription_policy_assignment" "sensitive" {} // Noncompliant
resource "azurerm_synapse_workspace" "sensitive" {} // Noncompliant
resource "azurerm_virtual_machine" "sensitive" {} // Noncompliant
resource "azurerm_virtual_machine_scale_set" "sensitive" {} // Noncompliant
resource "azurerm_windows_virtual_machine" "sensitive" {} // Noncompliant
resource "azurerm_windows_virtual_machine_scale_set" "sensitive" {} // Noncompliant
resource "azurerm_windows_web_app" "sensitive" {} // Noncompliant
