resource "azurerm_app_service" "s6413-as-c1" {
  logs {
    http_logs {
      azure_blob_storage {
# NonCompliant@+1
#       TODO foo bar
        retention_in_days = 14
      }
      file_system {
        # NonCompliant@+1
        retention_in_days = 14  # foo TODO bar
      }
    }
#   comment
    application_logs {
      azure_blob_storage {
        retention_in_days = 14 # comment
      }
    }
  }
}
