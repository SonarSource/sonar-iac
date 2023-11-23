#### application_insights ####

resource "azurerm_application_insights" "production" {
  # Noncompliant@+1 {{Make sure allowing public network access is safe here.}}
  internet_ingestion_enabled = true
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  # Noncompliant@+1 {{Make sure allowing public network access is safe here.}}
  internet_query_enabled = true
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_application_insights" "production" {
  internet_ingestion_enabled = true # Noncompliant
  internet_query_enabled = false
}

resource "azurerm_application_insights" "production" {
  internet_ingestion_enabled = false
  internet_query_enabled = true # Noncompliant
}

# Noncompliant@+1 {{Omitting "internet_query_enabled" allows network access from the Internet. Make sure it is safe here.}}
resource "azurerm_application_insights" "production" {
  internet_ingestion_enabled = false
}

# Noncompliant@+1 {{Omitting "internet_ingestion_enabled" allows network access from the Internet. Make sure it is safe here.}}
resource "azurerm_application_insights" "production" {
  internet_query_enabled = false
}

# Noncompliant@+1 {{Omitting "internet_ingestion_enabled" and "internet_query_enabled" allows network access from the Internet. Make sure it is safe here.}}
resource "azurerm_application_insights" "foo" {
}

#### sql_managed_instance ####

resource "azurerm_sql_managed_instance" "production" {
  # Noncompliant@+1 {{Make sure allowing public network access is safe here.}}
  public_data_endpoint_enabled = true
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_sql_managed_instance" "production" {
  public_data_endpoint_enabled = false
}

resource "azurerm_sql_managed_instance" "production" {
}
