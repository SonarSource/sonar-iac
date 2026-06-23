# Noncompliant@+1 {{Omitting "purge_protection_enabled" disables purge protection. Make sure it is safe here.}}
resource "azurerm_key_vault" "purge_protection_missing" {
  name                = "example-vault"
  resource_group_name = azurerm_resource_group.example.name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  sku_name            = "standard"
}

resource "azurerm_key_vault" "purge_protection_disabled" {
  name                     = "example-vault"
  sku_name                 = "standard"
  purge_protection_enabled = false # Noncompliant {{Make sure that disabling purge protection is safe here.}}
}

resource "azurerm_key_vault" "purge_protection_disabled_string" {
  name                     = "example-vault"
  sku_name                 = "standard"
  purge_protection_enabled = "false" # Noncompliant {{Make sure that disabling purge protection is safe here.}}
}

resource "azurerm_key_vault" "purge_protection_enabled" {
  name                       = "example-vault"
  sku_name                   = "standard"
  purge_protection_enabled   = true
  soft_delete_retention_days = 90
}

resource "azurerm_key_vault" "purge_protection_enabled_string" {
  name                     = "example-vault"
  sku_name                 = "standard"
  purge_protection_enabled = "true"
}

resource "azurerm_key_vault" "purge_protection_unresolved" {
  name                     = "example-vault"
  sku_name                 = "standard"
  purge_protection_enabled = var.purge_protection_enabled
}

resource "not_azurerm_key_vault" "for_coverage" {
  purge_protection_enabled = false
}
