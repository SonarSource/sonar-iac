# Function apps fire only when public_network_access_enabled = false (intentionally private / M2M).
# The deprecated azurerm_function_app exposes only client_cert_mode (no enabled flag), so it follows the mode-only path.
# The newer Linux/Windows variants gate on client_certificate_enabled first because enabled=false overrides any mode value.

resource "azurerm_function_app" "private_optional" {
  public_network_access_enabled = false
  client_cert_mode              = "Optional" # Noncompliant {{Require client certificates for this resource.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Set "client_certificate_mode" to enable client certificate authentication.}}
resource "azurerm_function_app" "private_missing" {
  public_network_access_enabled = false
}

resource "azurerm_function_app" "private_compliant" {
  public_network_access_enabled = false
  client_cert_mode              = "Required"
}

# Noncompliant@+1 {{Set "client_certificate_enabled" to enable client certificate authentication.}}
resource "azurerm_linux_function_app" "private_missing_enabled" {
  public_network_access_enabled = false
}

resource "azurerm_linux_function_app" "private_disabled" {
  public_network_access_enabled = false
  client_certificate_enabled    = false # Noncompliant {{Enable client certificate authentication for this resource.}}
}

# enabled=false + a "safe" mode is the false-negative the gating-on-enabled-first pattern prevents.
resource "azurerm_linux_function_app" "private_disabled_with_strict_mode" {
  public_network_access_enabled = false
  client_certificate_enabled    = false # Noncompliant
  client_certificate_mode       = "Required"
}

# Noncompliant@+1 {{Set "client_certificate_mode" to enable client certificate authentication.}}
resource "azurerm_linux_function_app" "private_enabled_missing_mode" {
  public_network_access_enabled = false
  client_certificate_enabled    = true
}

resource "azurerm_linux_function_app" "private_enabled_optional" {
  public_network_access_enabled = false
  client_certificate_enabled    = true
  client_certificate_mode       = "Optional" # Noncompliant
}

resource "azurerm_linux_function_app" "private_compliant" {
  public_network_access_enabled = false
  client_certificate_enabled    = true
  client_certificate_mode       = "Required"
}

resource "azurerm_linux_function_app_slot" "private_disabled" {
  public_network_access_enabled = false
  client_certificate_enabled    = false # Noncompliant
}

# Noncompliant@+1 {{Set "client_certificate_enabled" to enable client certificate authentication.}}
resource "azurerm_windows_function_app" "private_missing_enabled" {
  public_network_access_enabled = false
}

resource "azurerm_windows_function_app" "private_enabled_optional" {
  public_network_access_enabled = false
  client_certificate_enabled    = true
  client_certificate_mode       = "Optional" # Noncompliant
}

resource "azurerm_windows_function_app_slot" "private_enabled_optional" {
  public_network_access_enabled = false
  client_certificate_enabled    = true
  client_certificate_mode       = "Optional" # Noncompliant
}

resource "azurerm_windows_function_app_slot" "private_compliant" {
  public_network_access_enabled = false
  client_certificate_enabled    = true
  client_certificate_mode       = "Required"
}

# Default (public_network_access_enabled absent or true) — skip.

resource "azurerm_function_app" "public_default" {
  client_cert_mode = "Optional"
}

resource "azurerm_function_app" "public_explicit" {
  public_network_access_enabled = true
  client_cert_mode              = "Optional"
}

resource "azurerm_linux_function_app" "public_default" {
  client_certificate_enabled = false
  client_certificate_mode    = "Optional"
}

resource "azurerm_other_app" "compliant_unrelated" {
  public_network_access_enabled = false
  client_certificate_mode       = "Optional" # Compliant - unrelated resource type
}
