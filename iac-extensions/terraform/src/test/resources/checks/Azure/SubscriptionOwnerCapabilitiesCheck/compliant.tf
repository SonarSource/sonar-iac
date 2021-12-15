resource "azurerm_role_definition" "role-with-limited-actions" { # Compliant
  name        = "role-with-limited-actions"
  scope       = data.azurerm_subscription.primary.id

  permissions {
    actions     = ["Microsoft.Compute/*"] # Limited set of actions
    not_actions = []
  }
}

resource "azurerm_role_definition" "role-with-limited-assignable-scopes" { # Compliant
  name        = "role-with-limited-scope"
  scope       = data.azurerm_subscription.primary.id

  permissions {
    actions     = ["*"]
    not_actions = []
  }

  assignable_scopes = [
    azurerm_resource_group.test.id # Limited to a resource group
  ]
}

# Alternative way to define a resource group level scope
resource "azurerm_role_definition" "role-with-limited-assignable-scopes-alternative" { # Compliant
  name        = "role-with-limited-scope"
  scope       = data.azurerm_subscription.primary.id

  permissions {
    actions     = ["*"]
    not_actions = []
  }

  assignable_scopes = [
    "/subscriptions/${var.subscription_id}/resourceGroups/${var.environment_name}" # Limited to a resource group
  ]
}
