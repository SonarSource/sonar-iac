resource "azurerm_api_management" "example" {
  sku_name = "Consumption_1"
  client_certificate_enabled = false # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
}

resource "azurerm_api_management" "example" {
  sku_name = "Consumption_1"
  client_certificate_enabled = true # Compliant
}

# Noncompliant@+1 {{Omitting client_certificate_enabled disables certificate-based authentication. Make sure it is safe here.}}
resource "azurerm_api_management" "example" {
  sku_name = "Consumption_1"
}

resource "azurerm_api_management" "example" {
  sku_name = "Consumption_" # Compliant
}

resource "azurerm_api_management" "example" {
}
