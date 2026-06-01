resource "azurerm_data_factory_linked_service_web" "production" {
  authentication_type = "Basic" # Noncompliant {{Use client certificate authentication for this resource.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_data_factory_linked_service_web" "production" {
  authentication_type = "ClientCertificate" # Compliant
}

resource "azurerm_data_factory_linked_service_web" "production" {
  # Compliant
}

resource "azurerm_data_factory_linked_service_sftp" "production" {
  authentication_type = "Basic" # Noncompliant {{Use client certificate authentication for this resource.}}
}
