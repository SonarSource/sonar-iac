resource "azurerm_role_assignment" "owner-role-assignment" {
  role_definition_name = "Owner" # Noncompliant {{Make sure that assigning the Owner role is safe here.}}
}

resource "azurerm_role_assignment" "contributor-role-assignment" {
  role_definition_name = "Contributor" # Noncompliant {{Make sure that assigning the Contributor role is safe here.}}
}

resource "azurerm_role_assignment" "user-access-admin-role-assignment" {
  role_definition_name = "User Access Administrator" # Noncompliant {{Make sure that assigning the User Access Administrator role is safe here.}}
}

resource "azurerm_role_assignment" "user-access-admin-role-assignment" {
  role_definition_name = "Editor" # Compliant
}

resource "azurerm_role_assignment" "user-access-admin-role-assignment" {
  # Compliant
}
