resource "azurerm_logic_app_standard" "sensitive1" {
  public_network_access_enabled = false
  client_cert_mode              = "Optional" # Noncompliant {{Require client certificates for this resource.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_logic_app_standard" "sensitive2" {
  public_network_access_enabled = false
  client_certificate_mode       = "Optional" # Noncompliant {{Require client certificates for this resource.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Set "client_certificate_mode" to enable client certificate authentication.}}
resource "azurerm_logic_app_standard" "sensitive3" {
  public_network_access_enabled = false
}

resource "azurerm_logic_app_standard" "compliant1" {
  public_network_access_enabled = false
  client_cert_mode              = "Mandatory"
}

resource "azurerm_logic_app_standard" "compliant2" {
  public_network_access_enabled = false
  client_certificate_mode       = "Mandatory"
}

# Default (public_network_access_enabled absent or true) — skip.

resource "azurerm_logic_app_standard" "public_default" {
  client_cert_mode = "Optional"
}

resource "azurerm_logic_app_standard" "public_explicit" {
  public_network_access_enabled = true
  client_certificate_mode       = "Optional"
}

resource "azurerm_other_app" "compliant3" {
  client_certificate_mode = false # Compliant - unrelated resource type
}
