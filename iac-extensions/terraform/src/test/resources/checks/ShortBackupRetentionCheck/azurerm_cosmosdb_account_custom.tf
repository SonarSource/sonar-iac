resource "azurerm_cosmosdb_account" "s6364-cdba-c-short-backup" {
  backup {
    retention_in_hours = 48
  }
}

resource "azurerm_cosmosdb_account" "s6364-cdba-nc-no-retention_in_hours" {
  backup {
    retention_in_hours = 8 # Noncompliant
  }
}
