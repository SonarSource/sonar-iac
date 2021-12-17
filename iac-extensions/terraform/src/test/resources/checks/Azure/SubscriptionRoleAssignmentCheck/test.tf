resource "azurerm_role_assignment" "noncompliant_example_1" {
# Noncompliant@+1 {{Make sure assigning this role with a Subscription scope is safe here.}}
  scope = data.azurerm_subscription.primary.id
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_role_assignment" "compliant_example_2" {
  scope = "/subscriptions/00000000-0000-0000-0000-000000000000" # Noncompliant
}

resource "azurerm_role_assignment" "compliant_example_3" {
  scope = "/subscriptions/${data.azurerm_subscription.primary.id}/" # Noncompliant
}

resource "azurerm_role_assignment" "compliant_example_3" {
  scope = "${data.azurerm_subscription.primary.id}" # Noncompliant
}

# Role assigned with a Management Group scope
resource "azurerm_role_assignment" "noncompliant_example_1" {
# Noncompliant@+1 {{Make sure assigning this role with a Management Group scope is safe here.}}
  scope = data.azurerm_management_group.example_parent.id
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_role_assignment" "compliant_example_2" {
  scope = "/providers/microsoft.management/managementGroups/parent_group" # Noncompliant
}

resource "azurerm_role_assignment" "compliant_example_1" {
  scope = azurerm_resource_group.example.id # Compliant, assigment scope is limited to a resource group
}

resource "azurerm_role_assignment" "compliant_example_2" {
  scope = "/subscriptions/${data.azurerm_subscription.primary.id}/resourceGroups/${var.environment_name}" # Limited to a resource group
}

resource "azurerm_role_assignment" "compliant_missing_scope" {
}

resource "azurerm_other_resource" "coverage" {
  scope = data.azurerm_subscription.primary.id
}
