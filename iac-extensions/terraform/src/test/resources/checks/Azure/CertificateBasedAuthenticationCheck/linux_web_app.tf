# Web apps fire only when public_network_access_enabled = false (intentionally private / M2M).

# Noncompliant@+1 {{Set "client_certificate_enabled" to enable client certificate authentication.}}
resource "azurerm_linux_web_app" "private_missing_enabled" {
  public_network_access_enabled = false
}

resource "azurerm_linux_web_app" "private_disabled" {
  public_network_access_enabled = false
  client_certificate_enabled    = false # Noncompliant {{Enable client certificate authentication for this resource.}}
}

# Noncompliant@+1 {{Set "client_certificate_mode" to enable client certificate authentication.}}
resource "azurerm_linux_web_app" "private_enabled_missing_mode" {
  public_network_access_enabled = false
  client_certificate_enabled    = true
}

resource "azurerm_linux_web_app" "private_enabled_optional" {
  public_network_access_enabled = false
  client_certificate_enabled    = true
  client_certificate_mode       = "Optional" # Noncompliant {{Require client certificates for this resource.}}
}

resource "azurerm_linux_web_app" "private_compliant" {
  public_network_access_enabled = false
  client_certificate_enabled    = true
  client_certificate_mode       = "Required"
}

# Default (public_network_access_enabled absent or true) — skip.

resource "azurerm_linux_web_app" "public_default" {
  # no public_network_access_enabled → defaults to true → skipped
}

resource "azurerm_linux_web_app" "public_explicit" {
  public_network_access_enabled = true
  client_certificate_enabled    = false
}

resource "azurerm_windows_web_app" "private_disabled" {
  public_network_access_enabled = false
  client_certificate_enabled    = false # Noncompliant
}

resource "azurerm_linux_web_app_slot" "private_disabled" {
  public_network_access_enabled = false
  client_certificate_enabled    = false # Noncompliant
}

# Noncompliant@+1 {{Set "client_certificate_enabled" to enable client certificate authentication.}}
resource "azurerm_linux_web_app_slot" "private_missing_enabled" {
  public_network_access_enabled = false
}

resource "azurerm_windows_web_app_slot" "private_disabled" {
  public_network_access_enabled = false
  client_certificate_enabled    = false # Noncompliant
}

# Noncompliant@+1 {{Set "client_certificate_enabled" to enable client certificate authentication.}}
resource "azurerm_windows_web_app_slot" "private_missing_enabled" {
  public_network_access_enabled = false
}
