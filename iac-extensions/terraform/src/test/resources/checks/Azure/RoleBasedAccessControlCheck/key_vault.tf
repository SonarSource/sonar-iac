resource "azurerm_key_vault" "production" {
  # Noncompliant@+1 {{Make sure that disabling role-based access control is safe here.}}
  enable_rbac_authorization   = false
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting 'enable_rbac_authorization' disables role-based access control for this resource. Make sure it is safe here.}}
resource "azurerm_key_vault" "production2" {
  #      ^^^^^^^^^^^^^^^^^^^
  # enable_rbac_authorization is missing
}

resource "azurerm_key_vault" "production" {
  enable_rbac_authorization   = true # Compliant, Defaults to false
}

resource "azurerm" "coverage" { # Compliant
}
