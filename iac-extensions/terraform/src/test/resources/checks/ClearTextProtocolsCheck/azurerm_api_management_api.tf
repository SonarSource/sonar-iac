resource "azurerm_api_management_api" "noncompliant_api_1" {
  protocols = ["https", "sftp", "http"] # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
#                               ^^^^^^
}

resource "azurerm_api_management_api" "compliant_api_1" {
  protocols = ["https", "sftp"]
}

resource "azurerm_api_management_api" "compliant_by_default" {
}

resource "non_azurerm_api_management_api" "coverage" {
  protocols = ["http"]
}
