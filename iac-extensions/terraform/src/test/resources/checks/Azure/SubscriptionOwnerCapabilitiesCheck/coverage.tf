provider "azurerm" {
  skip_provider_registration = true
  features {}
}

data "azurerm_subscription" "primary" {
}

data "azurerm_management_group" "example_parent" {
  display_name = "parent_group"
}

resource "azurerm_resource_group" "test" {
  name     = "test_resource_group"
  location = "West Europe"
}

resource "azurerm_role_definition" "management-group-owner-role-alternative-scope" {
  permissions {
    actions     = ["*"]
  }
  assignable_scopes = [
    "/providers/microsoft.other_tool/managementGroups/parent_group"
  ]
}

resource "azurerm_role_definition" "management-group-owner-role-non_parsed_interpolation" {
  permissions {
    actions     = ["*"]
  }
  assignable_scopes = [
    "/providers/microsoft.other_tool/managementGroups/parent_group"
  ]
}

resource "azurerm_role_definition" "management-group-owner-role" {
  permissions {
    actions     = ["*"]
  }

  assignable_scopes = [
    data.azurerm_management_group.root_example.*
  ]
}

resource "azurerm_role_definition" "management-group-owner-role" {
  permissions {
    actions     = ["*"]
  }

  assignable_scopes = [
    data.*.foo.id
  ]
}

