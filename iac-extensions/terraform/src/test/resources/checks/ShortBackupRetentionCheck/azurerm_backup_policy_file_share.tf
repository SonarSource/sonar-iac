resource "azurerm_backup_policy_file_share" "s6364-bpfs-nc1" {
  retention_daily {
    count = 3 # Noncompliant {{Make sure that defining a short backup retention duration is safe here.}}
  }
}

resource "azurerm_backup_policy_file_share" "s6364-bpfs-c1" {
  retention_daily {
    count = 7
  }
}

resource "azurerm_backup_policy_file_share" "s6364-bpfs-c2" {
  retention_daily {
    # The "count" attribute is required
  }
}

resource "azurerm_backup_policy_file_share" "s6364-bpfs-c3" {
  # The "retention_daily" block is required
}

resource "non_azurerm_backup_policy_file_share" "s6364-bpfs-cov" {
  retention_daily {
    count = 3
  }
}
