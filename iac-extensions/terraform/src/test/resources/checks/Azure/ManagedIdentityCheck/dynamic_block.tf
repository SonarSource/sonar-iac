variable "managed_identities" {
  type = map(object({
    principal_id = string
    tenant_id    = string
  }))
}

resource "azurerm_cosmosdb_account" "compliant" {
  dynamic "identity" {
    for_each = var.managed_identities
    type     = "SystemAssigned"
    content {}
  }
}
