resource "azurerm_app_service" "sensitive_app_service" {
  client_cert_enabled = false # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting client_cert_enabled disables certificate-based authentication. Make sure it is safe here.}}
resource "azurerm_app_service" "sensitive_app_service" {
}

resource "azurerm_app_service" "compliant_app_service" {
  client_cert_enabled = true # Compliant
}

resource "azurerm_other_service" "other_resource" {
  client_cert_enabled = false # Compliant
}
