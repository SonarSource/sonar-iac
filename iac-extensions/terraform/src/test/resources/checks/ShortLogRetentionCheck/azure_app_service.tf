resource "azurerm_app_service" "s6413-as-nc1" {
  logs {
    http_logs {
      azure_blob_storage {
        retention_in_days = 13  # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
      }
      file_system {
        retention_in_days = 13  # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
      }
    }
    application_logs {
      azure_blob_storage {
        retention_in_days = 13  # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
      }
    }
  }
}

resource "azurerm_app_service" "s6413-as-c1" {
  logs {
    http_logs {
      azure_blob_storage {
        retention_in_days = 14
      }
      file_system {
        retention_in_days = 14
      }
    }
    application_logs {
      azure_blob_storage {
        retention_in_days = 14
      }
    }
  }
}

resource "non_azurerm_app_service" "s6413-as-c2" {
  logs {
    http_logs {
      azure_blob_storage {
        retention_in_days = 13
      }
      file_system {
        retention_in_days = 13
      }
    }
    application_logs {
      azure_blob_storage {
        retention_in_days = 13
      }
    }
  }
}

resource "azurerm_app_service" "s6413-as-c3" {
  logs {
    http_logs {
      azure_blob_storage {
        retention_in_days = 0
      }
      file_system {
        retention_in_days = 0
      }
    }
    application_logs {
      azure_blob_storage {
        retention_in_days = 0
      }
    }
  }
}

resource "non_azurerm_app_service" "s6413-as-cov" {
  logs {
    http_logs {
      azure_blob_storage {
        retention_in_days = 13
      }
      file_system {
        retention_in_days = 13
      }
    }
    application_logs {
      azure_blob_storage {
        retention_in_days = 13
      }
    }
  }
}
