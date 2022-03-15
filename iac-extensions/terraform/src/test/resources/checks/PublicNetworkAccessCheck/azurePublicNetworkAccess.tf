# Noncompliant@+1 {{Omitting "public_network_access_enabled" allows network access from the Internet. Make sure it is safe here.}}
resource "azurerm_batch_account" "production"  {}

resource "azurerm_batch_account" "production"  {
  public_network_access_enabled = true # Noncompliant {{Make sure allowing public network access is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_batch_account" "production"  {
  public_network_access_enabled = false
}

resource "azurerm_batch_account" "production"  { # Noncompliant
  xxx = true
}

resource "azurerm_cognitive_account" "production"  {} # Noncompliant
resource "azurerm_container_registry" "production"  {} # Noncompliant
resource "azurerm_cosmosdb_account" "production"  {} # Noncompliant
resource "azurerm_databricks_workspace" "production"  {} # Noncompliant
resource "azurerm_eventgrid_domain" "production"  {} # Noncompliant
resource "azurerm_eventgrid_topic" "production"  {} # Noncompliant
resource "azurerm_healthcare_service" "production"  {} # Noncompliant
resource "azurerm_iothub" "production"  {} # Noncompliant
resource "azurerm_managed_disk" "production"  {} # Noncompliant
resource "azurerm_mariadb_server" "production"  {} # Noncompliant
resource "azurerm_mssql_server" "production"  {} # Noncompliant
resource "azurerm_mysql_server" "production"  {} # Noncompliant
resource "azurerm_postgresql_server" "production"  {} # Noncompliant
resource "azurerm_redis_cache" "production"  {} # Noncompliant
resource "azurerm_search_service" "production"  {} # Noncompliant
resource "azurerm_synapse_workspace" "production"  {} # Noncompliant

resource "unrelated_resource_type" "production"  {}

resource "unrelated_resource_type" "production"  {
  public_network_access_enabled = true
}
