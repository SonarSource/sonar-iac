resource "azurerm_api_management_api" "noncompliant_api_1" {
  protocols = ["https", "sftp", "http"] # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
#                               ^^^^^^
}

resource "azurerm_api_management_api" "compliant_api_1" {
  protocols = ["https", "sftp"]
}

resource "azurerm_api_management_api" "noncompliant_api_2" {  # Sensitive (S5332); source api is insecure
  source_api_id = azurerm_api_management_api.noncompliant_api_1.id
}

resource "azurerm_api_management_api" "compliant_api_2" {  # source api is secure
  source_api_id = azurerm_api_management_api.compliant_api_1.id
}

### some weird cases:

resource "azurerm_api_management_api" "compliant_api_3" { # our protocols are ok, but source is a self-reference !?
  protocols = ["https", "sftp"]
  source_api_id = azurerm_api_management_api.compliant_api_3.id
}

resource "azurerm_api_management_api" "compliant_api_4" { # our protocols are ok, source's are noncompliant
  protocols = ["https", "sftp"]
  source_api_id = azurerm_api_management_api.noncompliant_api_1.id
}

resource "azurerm_api_management_api" "noncompliant_api_3" { # do we support more than one level  of indirection?
  source_api_id = azurerm_api_management_api.noncompliant_api_2.id
}

resource "azurerm_api_management_api" "compliant_api_5" {
  source_api_id = azurerm_api_management_api.compliant_api_3.NOTID # wrong source_api_id reference suffix (for coverage)
}

resource "azurerm_api_management_api" "compliant_api_6" {
  source_api_id = NOT_azurerm_api_management_api.compliant_api_3.id # wrong source_api_id reference prefix (for coverage)
}

resource "azurerm_api_management_api" "compliant_api_7" {
  source_api_id = azurerm_api_management_api.WRONG_RESOURCE_NAME.id # wrong source_api_id reference (for coverage)
}
