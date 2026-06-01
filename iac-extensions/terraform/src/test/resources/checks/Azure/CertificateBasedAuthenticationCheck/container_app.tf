# Container Apps fire only when ingress is internal (external_enabled != true), mirroring the ARM gate.

resource "azurerm_container_app" "internal_ignore" {
  name = "internal-ignore"
  ingress {
    target_port             = 80
    external_enabled        = false
    client_certificate_mode = "ignore" # Noncompliant {{Enable client certificate authentication for this resource.}}
  }
}

resource "azurerm_container_app" "internal_accept" {
  name = "internal-accept"
  ingress {
    target_port             = 80
    external_enabled        = false
    client_certificate_mode = "accept" # Noncompliant {{Require client certificates for this resource.}}
  }
}

resource "azurerm_container_app" "internal_default_no_mode" {
  name = "internal-default-no-mode"
  ingress { # Noncompliant {{Set "client_certificate_mode" to enable client certificate authentication.}}
    target_port = 80
    # external_enabled absent → defaults to internal → check applies
  }
}

resource "azurerm_container_app" "internal_explicit_no_mode" {
  name = "internal-explicit-no-mode"
  ingress { # Noncompliant
    target_port      = 80
    external_enabled = false
  }
}

resource "azurerm_container_app" "internal_require" {
  name = "internal-require"
  ingress {
    target_port             = 80
    external_enabled        = false
    client_certificate_mode = "require" # Compliant
  }
}

# External-facing ingress (external_enabled = true) → skip.

resource "azurerm_container_app" "external_ignore" {
  name = "external-ignore"
  ingress {
    target_port             = 80
    external_enabled        = true
    client_certificate_mode = "ignore" # Compliant - external app
  }
}

resource "azurerm_container_app" "external_no_mode" {
  name = "external-no-mode"
  ingress {
    target_port      = 80
    external_enabled = true
    # mode missing - skipped because external
  }
}

# No ingress block at all → not a public endpoint at all → skip (mirrors ARM logic).

resource "azurerm_container_app" "no_ingress" {
  name = "no-ingress"
  # no ingress block - the app isn't exposed via ingress at all
}

# Cross-resource sanity: unrelated resource type with an ingress block should not be touched.

resource "azurerm_other_resource" "unrelated" {
  ingress {
    client_certificate_mode = "ignore" # Compliant - unrelated resource type
  }
}
