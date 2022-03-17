# Noncompliant@+1 {{Omitting "backup.retention_in_hours" results in a short backup retention duration. Make sure it is safe here.}}
resource "azurerm_cosmosdb_account" "s6364-cdba-nc-no-backup" {
  # Backup is automatically enabled but defaults to retention of 8 hours
}

resource "azurerm_cosmosdb_account" "s6364-cdba-nc-no-retention_in_hours" {
  # Noncompliant@+1 {{Omitting "retention_in_hours" results in a short backup retention duration. Make sure it is safe here.}}
  backup {
  }
}

resource "azurerm_cosmosdb_account" "s6364-cdba-nc-short-backup" {
  backup {
    retention_in_hours = 48  # Noncompliant {{Make sure that defining a short backup retention duration is safe here.}}
  }
}

resource "azurerm_cosmosdb_account" "s6364-cdba-c" {
  backup {
    retention_in_hours = 168  # 7 * 24
  }
}

resource "azurerm_cosmosdb_account" "s6364-cdba-c-ref" {
  backup {
    retention_in_hours = var.db_retention_in_hours
  }
}

resource "non_azurerm_cosmosdb_account" "s6364-cdba-cov" {
  backup {
    retention_in_hours = 8
  }
}
