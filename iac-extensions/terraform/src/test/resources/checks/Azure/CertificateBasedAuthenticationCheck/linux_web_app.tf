# Noncompliant@+1 {{Omitting client_cert_mode disables certificate-based authentication. Make sure it is safe here.}}
resource "azurerm_linux_web_app" "sensitive1" {
  client_cert_enabled = true # Compliant
}

resource "azurerm_linux_web_app" "sensitive2" {
  client_cert_enabled = false # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting client_cert_enabled disables certificate-based authentication. Make sure it is safe here.}}
resource "azurerm_linux_web_app" "sensitive3" {
}



resource "azurerm_linux_web_app" "sensitive4" {
  client_cert_enabled = true # Compliant
  client_cert_mode = "Optional" # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
}

resource "azurerm_linux_web_app" "sensitive5" {
  client_cert_enabled = false # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
  client_cert_mode = "Optional" # Compliant
}

# Noncompliant@+1 {{Omitting client_cert_enabled disables certificate-based authentication. Make sure it is safe here.}}
resource "azurerm_linux_web_app" "sensitive6" {
  client_cert_mode = "Optional" # Compliant
}



resource "azurerm_linux_web_app" "compliant1" {
  client_cert_enabled = true # Compliant
  client_cert_mode = "SomeMode" # Compliant
}

resource "azurerm_linux_web_app" "sensitive7" {
  client_cert_enabled = false # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
  client_cert_mode = "SomeMode" # Compliant
}

# Noncompliant@+1 {{Omitting client_cert_enabled disables certificate-based authentication. Make sure it is safe here.}}
resource "azurerm_linux_web_app" "sensitive8" {
  client_cert_mode = "SomeMode" # Compliant
}


resource "other_resource_type" "compliant2" {
  client_cert_enabled = false # Compliant
  client_cert_mode = "Optional" # Compliant
}


# also check for windows_web_app
resource "azurerm_windows_web_app" "sensitive2" {
  client_cert_enabled = false # Noncompliant {{Make sure that disabling certificate-based authentication is safe here.}}
}
