resource "azurerm_function_app" "sensitive1" {
  client_cert_mode = "Optional" # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting client_cert_mode disables certificate-based authentication. Make sure it is safe here.}}
resource "azurerm_function_app" "sensitive2" {
}

resource "azurerm_function_app" "compliant1" {
  client_cert_mode = "Mandatory" # Compliant
}

resource "azurerm_other_app" "compliant2" {
  client_cert_mode = false # Compliant
}
