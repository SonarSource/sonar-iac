# Noncompliant@+1 {{Omitting "backup" results in a short backup retention duration. Make sure it is safe here.}}
resource "azurerm_app_service" "s6364-as-nc-no-backup" {
}

resource "azurerm_app_service" "s6364-as-nc-disabled-backup" {
  backup {
    enabled = false  # Noncompliant {{Make sure disabling backup is safe here.}}
  }
}

resource "azurerm_app_service" "s6364-as-c-missing-schedule" {
  backup {
    enabled = true
  }
}

resource "azurerm_app_service" "s6364-as-c-missing-retention_period_in_days" {
  backup {
    enabled = true
    schedule {
    }
  }
}

resource "azurerm_app_service" "s6364-as-nc-low" {
  backup {
    enabled = true
    schedule {
      retention_period_in_days = 3  # Noncompliant {{Make sure that defining a short backup retention duration is safe here.}}
    }
  }
}

resource "azurerm_app_service" "s6364-as-nc-single-issue-only" {
  backup {
    enabled = false # Noncompliant {{Make sure disabling backup is safe here.}}
    schedule {
      retention_period_in_days = 3
    }
  }
}

resource "azurerm_app_service" "s6364-as-c" {
  backup {
    enabled = true
    schedule {
      retention_period_in_days = 7
    }
  }
}


resource "non_azurerm_app_service" "s6364-as-cov" {
}
