resource "azurerm_key_vault" "production1" {
  # Noncompliant@+1 {{Make sure that disabling role-based access control is safe here.}}
  enable_rbac_authorization   = false
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_key_vault" "production2" {
  # Noncompliant@+1 {{Make sure that disabling role-based access control is safe here.}}
  rbac_authorization_enabled   = false
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting "rbac_authorization_enabled" disables role-based access control for this resource. Make sure it is safe here.}}
resource "azurerm_key_vault" "production3" {
  #      ^^^^^^^^^^^^^^^^^^^
  # rbac_authorization_enabled is missing
}

resource "azurerm_key_vault" "production4" {
  enable_rbac_authorization   = true # Compliant, Defaults to false
}

resource "azurerm_key_vault" "production5" {
  rbac_authorization_enabled   = true # Compliant, Defaults to false
}

resource "azurerm" "coverage" { # Compliant
}
