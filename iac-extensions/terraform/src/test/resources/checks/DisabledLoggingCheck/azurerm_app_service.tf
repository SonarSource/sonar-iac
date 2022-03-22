resource "azurerm_app_service" "example" {
  logs {
    http_logs {}
    application_logs { # Noncompliant {{Make sure that deactivating application logs is safe here.}}
      file_system_level = "Off"
      azure_blob_storage {
        level = "Off"
      }
    }
  }
}

resource "azurerm_app_service" "example" {
  logs { # Noncompliant {{Make sure that omitting HTTP logs is safe here.}}
    # No http_logs {}
    application_logs {
      file_system_level = "Anything"
      azure_blob_storage {
        level = "Anything"
      }
    }
  }
}

resource "azurerm_app_service" "example" {
  logs { # Noncompliant {{Make sure that disabling logging is safe here.}}
    # No http_logs {}
    application_logs {
      file_system_level = "Off"
      azure_blob_storage {
        level = "Off"
      }
    }
  }
}


resource "azurerm_app_service_slot" "example" {
  logs { # Noncompliant {{Make sure that disabling logging is safe here.}}
    application_logs {
      file_system_level = "Off"

      azure_blob_storage {
        level = "Off"
      }
    }
  }
}

resource "azurerm_app_service_slot" "example" {
  logs {
    http_logs {}
    application_logs {
      file_system_level = "Off"

      azure_blob_storage {
        level = "Anything"
      }
    }
  }
}

resource "azurerm_app_service_slot" "example" {
  logs {
    http_logs {}
    application_logs {
      file_system_level = "Anything"

      azure_blob_storage {
        level = "Off"
      }
    }
  }
}

resource "azurerm_app_service_slot" "example" {
  logs {
    http_logs {}
    application_logs {
      azure_blob_storage {
        level = "Anything"
      }
    }
  }
}

resource "azurerm_app_service_slot" "example" {
  logs {
    http_logs {}
    application_logs {
      file_system_level = "Off"
      azure_blob_storage {
        # No valid semantic due to level attribute is required
      }
    }
  }
}

resource "azurerm_app_service_slot" "example" {
  logs {
    http_logs {}
    application_logs {
      file_system_level = "Anything"
    }
  }
}

resource "azurerm_app_service_slot" "example" { # Noncompliant {{Make sure that omitting the "logs" block is safe here.}}
}

resource "azurerm_app_service_slot" "example" {
  logs { # Noncompliant {{Make sure that omitting http and application logging blocks is safe here.}}
  }
}

resource "azurerm_app_service_slot" "example" {
  logs {
    http_logs {}
    application_logs {} # Noncompliant {{Make sure that deactivating application logs is safe here.}}
  }
}

resource "azurerm_app_service_slot" "example" {
  logs { # Noncompliant {{Make sure that disabling logging is safe here.}}
    application_logs {}
  }
}
