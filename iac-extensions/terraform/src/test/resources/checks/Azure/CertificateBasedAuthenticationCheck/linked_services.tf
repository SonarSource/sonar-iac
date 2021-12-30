resource "azurerm_data_factory_linked_service_web" "production" {
  authentication_type = "Basic" # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
}

resource "azurerm_data_factory_linked_service_web" "production" {
  authentication_type = "ClientCertificate" # Compliant
}

resource "azurerm_data_factory_linked_service_web" "production" {
  # Compliant
}

resource "azurerm_data_factory_linked_service_sftp" "production" {
  authentication_type = "Basic" # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
}
