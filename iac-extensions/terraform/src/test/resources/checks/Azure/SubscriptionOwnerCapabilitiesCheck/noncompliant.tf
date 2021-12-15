


# Alternative ways to define a custom role with a subscription assignable scope
resource "azurerm_role_definition" "subscription-owner-role-alternative-scope-1" {
  name        = "subscription-owner-role-alternative-scope-1"
  scope       = data.azurerm_subscription.primary.id
  permissions {
    actions     = ["*"]
    not_actions = []
  }

  assignable_scopes = [
    "${data.azurerm_subscription.primary.id}" # Noncompliant
  ]
}


resource "azurerm_role_definition" "subscription-owner-role-alternative-scope-2" {
  name        = "subscription-owner-role-alternative-scope-2"
  scope       = data.azurerm_subscription.primary.id
  permissions {
    actions     = ["*"]
    not_actions = []
  }

  assignable_scopes = [
    "/subscriptions/00000000-0000-0000-0000-000000000000" # Noncompliant
  ]
}

resource "azurerm_role_definition" "subscription-owner-role-alternative-scope-3" {
  name        = "subscription-owner-role-alternative-scope-3"
  scope       = data.azurerm_subscription.primary.id

  permissions {
    actions     = ["*"]
    not_actions = []
  }

  assignable_scopes = [
    "/subscriptions/${var.subscription_id}/" # FN - We do not resolve variables yet
  ]
}

# Custom role defined with a Management Group assignable scope
resource "azurerm_role_definition" "management-group-owner-role" {
  name        = "management group-owner-role"
  scope       = data.azurerm_management_group.example_parent.id

  permissions {
    actions     = ["*"]
    not_actions = []
  }

  assignable_scopes = [
    data.azurerm_management_group.example_parent.id # Noncompliant
  ]
}

# Custom role defined with a Management Group assignable scope
resource "azurerm_role_definition" "management-group-owner-role" {
  permissions {
    actions     = ["*"]
  }

  assignable_scopes = [
    data.azurerm_management_group.root_example.id # Noncompliant
  ]
}

# Alternative way to define a custom role with a Management Group assignable scope
resource "azurerm_role_definition" "management-group-owner-role-alternative-scope" {
  name        = "management-group-owner-role"
  scope       = data.azurerm_management_group.example_parent.id

  permissions {
    actions     = ["*"]
    not_actions = []
  }

  assignable_scopes = [
  # Noncompliant@+1 {{Make sure assigning this role with a Management Group scope is safe here.}}
    "/providers/microsoft.management/managementGroups/parent_group"
  ]
}
