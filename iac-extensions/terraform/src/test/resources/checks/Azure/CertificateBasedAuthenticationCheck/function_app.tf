resource "azurerm_function_app" "sensitive1" {
  client_cert_mode = "Optional" # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_function_app" "sensitive1" {
  client_certificate_mode = "Optional" # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting "client_certificate_mode" disables certificate-based authentication. Make sure it is safe here.}}
resource "azurerm_function_app" "sensitive2" {
}

resource "azurerm_linux_function_app" "sensitive_linux_function_app" {
  client_certificate_mode = "Optional" # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}
resource "azurerm_linux_function_app_slot" "sensitive_linux_function_app_slot" {
  client_certificate_mode = "Optional" # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_windows_function_app" "sensitive_windows_function_app" {
  client_certificate_mode = "Optional" # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}
resource "azurerm_windows_function_app_slot" "sensitive_windows_function_app_slot" {
  client_certificate_mode = "Optional" # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_function_app" "compliant1" {
  client_cert_mode = "Mandatory" # Compliant
}

resource "azurerm_other_app" "compliant2" {
  client_cert_mode = false # Compliant
}


resource "azurerm_function_app" "compliant1" {
  client_certificate_mode = "Mandatory" # Compliant
}

resource "azurerm_other_app" "compliant2" {
  client_certificate_mode = false # Compliant
}
