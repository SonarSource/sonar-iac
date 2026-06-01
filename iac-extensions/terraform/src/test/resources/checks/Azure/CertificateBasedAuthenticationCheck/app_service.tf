# Deprecated azurerm_app_service has no public_network_access_enabled — the rule fires unconditionally.
# Note: the resource's Terraform schema only documents `client_cert_enabled` / `client_cert_mode`; the
# `client_certificate_*` variants in sensitive2, sensitive6, and compliant2 would be rejected by Terraform
# itself. They are kept here to lock in the name-agnostic matcher from SONARIAC-1006, which deliberately
# accepts both old and new names for all impacted resources.

resource "azurerm_app_service" "sensitive1" {
  client_cert_enabled = false # Noncompliant {{Enable client certificate authentication for this resource.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_app_service" "sensitive2" {
  client_certificate_enabled = false # Noncompliant {{Enable client certificate authentication for this resource.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Set "client_certificate_enabled" to enable client certificate authentication.}}
resource "azurerm_app_service" "sensitive3" {
}

# Noncompliant@+1 {{Set "client_certificate_mode" to enable client certificate authentication.}}
resource "azurerm_app_service" "sensitive4_missing_mode" {
  client_cert_enabled = true
}

resource "azurerm_app_service" "sensitive5_optional_mode" {
  client_cert_enabled = true
  client_cert_mode    = "Optional" # Noncompliant {{Require client certificates for this resource.}}
}

resource "azurerm_app_service" "sensitive6_optional_mode_new_names" {
  client_certificate_enabled = true
  client_certificate_mode    = "Optional" # Noncompliant
}

resource "azurerm_app_service" "compliant1" {
  client_cert_enabled = true # Compliant
  client_cert_mode    = "Required"
}

resource "azurerm_app_service" "compliant2" {
  client_certificate_enabled = true # Compliant
  client_certificate_mode    = "Required"
}

resource "azurerm_app_service" "compliant3_string_true" {
  client_certificate_enabled = "true" # Compliant - string "true" is accepted equivalently to the boolean
  client_certificate_mode    = "Required"
}

resource "azurerm_other_app" "compliant3" {
  client_certificate_enabled = false # Compliant - unrelated resource type
}
