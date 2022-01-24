#### azurerm_storage_account ####

resource "azurerm_storage_account" "noncompliant_storage_account_disabled" {
  enable_https_traffic_only = false  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
}

resource "azurerm_storage_account" "compliant_storage_account" {
  enable_https_traffic_only = true
}

resource "azurerm_storage_account" "compliant_storage_account_by_default" {
}

resource "non_azurerm_storage_account" "coverage" {
  enable_https_traffic_only = false
}

#### azurerm_storage_account_blob_container_sas ####

data "azurerm_storage_account_blob_container_sas" "noncompliant_storage_account_blob_disabled" {
  https_only = false  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
}

data "azurerm_storage_account_blob_container_sas" "compliant_storage_account_blob" {
  https_only = true
}

data "azurerm_storage_account_blob_container_sas" "compliant_storage_account_blob_by_default" {
}

data "non_azurerm_storage_account_blob_container_sas" "coverage" {
  https_only = false
}
