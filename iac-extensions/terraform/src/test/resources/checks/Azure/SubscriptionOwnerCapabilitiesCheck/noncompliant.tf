# Noncompliant@+1 {{Narrow the number of actions or the assignable scope of this custom role.}}
resource "azurerm_role_definition" "subscription-owner-role" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^
  name        = "subscription-owner-role"
  scope       = data.azurerm_subscription.primary.id

  permissions {
    actions     = ["*"]
  #                ^^^< {{Allows all actions.}}
    not_actions = []
  }

  assignable_scopes = [
    data.azurerm_subscription.primary.id
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{High scope level.}}
  ]
}

resource "azurerm_role_definition" "subscription-owner-role-multiple-scopes" { # Noncompliant
  name        = "subscription-owner-role"
  scope       = data.azurerm_subscription.primary.id

  permissions {
    actions     = ["*"]
    not_actions = []
  }

  assignable_scopes = [
    data.azurerm_subscription.foo.id,
    data.azurerm_subscription.primary.id
  ]
}


# Alternative ways to define a custom role with a subscription assignable scope
resource "azurerm_role_definition" "subscription-owner-role-alternative-scope-1" { # Noncompliant
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^
  name        = "subscription-owner-role-alternative-scope-1"
  scope       = data.azurerm_subscription.primary.id
  permissions {
    actions     = ["*"]
    #              ^^^<
    not_actions = []
  }

  assignable_scopes = [
    "${data.azurerm_subscription.primary.id}"
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{High scope level.}}
  ]
}


resource "azurerm_role_definition" "subscription-owner-role-alternative-scope-2" { # Noncompliant
  name        = "subscription-owner-role-alternative-scope-2"
  scope       = data.azurerm_subscription.primary.id
  permissions {
    actions     = ["*"]
    not_actions = []
  }

  assignable_scopes = [
    "/subscriptions/00000000-0000-0000-0000-000000000000"
  ]
}

resource "azurerm_role_definition" "subscription-owner-role-alternative-scope-3" { # FN - We do not resolve variables yet
  name        = "subscription-owner-role-alternative-scope-3"
  scope       = data.azurerm_subscription.primary.id

  permissions {
    actions     = ["*"]
    not_actions = []
  }

  assignable_scopes = [
    "/subscriptions/${var.subscription_id}/"
  ]
}

# Custom role defined with a Management Group assignable scope
resource "azurerm_role_definition" "management-group-owner-role" { # Noncompliant
  name        = "management group-owner-role"
  scope       = data.azurerm_management_group.example_parent.id

  permissions {
    actions     = ["*"]
    not_actions = []
  }

  assignable_scopes = [
    data.azurerm_management_group.example_parent.id
  ]
}

# Custom role defined with a Management Group assignable scope
resource "azurerm_role_definition" "management-group-owner-role" { # Noncompliant
  permissions {
    actions     = ["*"]
  }

  assignable_scopes = [
    data.azurerm_management_group.root_example.id
  ]
}

# Alternative way to define a custom role with a Management Group assignable scope
resource "azurerm_role_definition" "management-group-owner-role-alternative-scope" { # Noncompliant
  name        = "management-group-owner-role"
  scope       = data.azurerm_management_group.example_parent.id

  permissions {
    actions     = ["*"]
    not_actions = []
  }

  assignable_scopes = [
    "/providers/microsoft.management/managementGroups/parent_group"
  ]
}
