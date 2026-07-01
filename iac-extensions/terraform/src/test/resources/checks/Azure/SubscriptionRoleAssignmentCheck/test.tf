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

# Compliant: a condition restricts the effective permissions, so a broad scope is intentional
resource "azurerm_role_assignment" "compliant_with_condition" {
  scope             = data.azurerm_subscription.primary.id
  condition_version = "2.0"
  condition         = "(@Resource[Microsoft.Storage/storageAccounts/blobServices/containers:name] StringEquals 'public-data')"
}

resource "azurerm_role_assignment" "compliant_with_condition_management_group" {
  scope             = data.azurerm_management_group.example_parent.id
  condition_version = "2.0"
  condition         = "(@Resource[Microsoft.Storage/storageAccounts/blobServices/containers:name] StringEquals 'public-data')"
}

# An empty condition imposes no restriction on the broad scope, so the finding must still be raised
resource "azurerm_role_assignment" "noncompliant_empty_condition" {
  scope     = data.azurerm_subscription.primary.id # Noncompliant {{Make sure assigning this role with a Subscription scope is safe here.}}
  condition = ""
}

# Compliant: a heredoc condition exercises the non-blank literal branch
resource "azurerm_role_assignment" "compliant_heredoc_condition" {
  scope             = data.azurerm_subscription.primary.id
  condition_version = "2.0"
  condition         = <<-EOT
    (@Resource[Microsoft.Storage/storageAccounts/blobServices/containers:name] StringEquals 'public-data')
  EOT
}

# Compliant: an interpolated condition is a non-literal expression that cannot be resolved statically
resource "azurerm_role_assignment" "compliant_interpolated_condition" {
  scope     = data.azurerm_subscription.primary.id
  condition = "(@Resource[Microsoft.Storage/storageAccounts/blobServices/containers:name] StringEquals '${var.container_name}')"
}

# Compliant: a condition referencing another value is a non-literal expression that cannot be resolved statically
resource "azurerm_role_assignment" "compliant_reference_condition" {
  scope     = data.azurerm_subscription.primary.id
  condition = var.role_condition
}

# A blank-body heredoc still carries its <<TAG ... TAG markers, so its literal value is not blank and it is treated as
# a condition (Compliant). Documents the behavior of hasEffectiveCondition for this edge case.
resource "azurerm_role_assignment" "compliant_blank_heredoc_condition" {
  scope     = data.azurerm_subscription.primary.id
  condition = <<-EOT

  EOT
}
