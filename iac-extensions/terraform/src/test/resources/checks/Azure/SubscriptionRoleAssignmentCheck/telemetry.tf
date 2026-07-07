resource "azurerm_role_assignment" "builtin_name_user" {
  scope                = data.azurerm_subscription.primary.id # Noncompliant
  role_definition_name = "Reader"
  principal_type       = "User"
}

resource "azurerm_role_assignment" "custom_name_service_principal" {
  scope                = data.azurerm_subscription.primary.id # Noncompliant
  role_definition_name = "My Custom Role"
  principal_type       = "ServicePrincipal"
}

resource "azurerm_role_assignment" "builtin_id_group" {
  scope              = data.azurerm_management_group.parent.id # Noncompliant
  role_definition_id = "/providers/Microsoft.Authorization/roleDefinitions/b24988ac-6180-42a0-ab88-20f7382dd24c"
  principal_type     = "Group"
}

# Compliant: condition set, excluded from telemetry
resource "azurerm_role_assignment" "with_condition" {
  scope                = data.azurerm_subscription.primary.id
  role_definition_name = "Owner"
  condition_version    = "2.0"
  condition            = "(@Resource[Microsoft.Storage/storageAccounts/blobServices/containers:name] StringEquals 'public')"
}
