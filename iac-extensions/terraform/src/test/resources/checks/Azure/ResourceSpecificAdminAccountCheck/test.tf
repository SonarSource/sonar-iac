resource "azurerm_container_registry" "sensitive" {
  # Noncompliant@+1 {{Make sure that enabling an administrative account or administrative permissions is safe here.}}
  admin_enabled  = true
# ^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_container_registry" "disabled" {
  admin_enabled  = false
}

resource "azurerm_container_registry" "missing" {
}

resource "azurerm_batch_pool" "sensitive_pool" {
  name = "sensitive"
  start_task {
    user_identity {
      auto_user {
        elevation_level = "Admin" # Noncompliant
      }
    }
  }
}

resource "azurerm_batch_pool" "compliant_pool" {
  name = "sensitive"
  start_task {
    user_identity {
      auto_user {
        elevation_level = "NonAdmin"
      }
    }
  }
}

resource "azurerm_batch_pool" "compliant_pool" {
  name = "sensitive"
  start_task {
    user_identity {
    }
  }
}

resource "azurerm_batch_pool" "compliant_pool" {
  name = "sensitive"
  start_task {
  }
}

resource "azurerm_batch_pool" "compliant_pool" {
  name = "sensitive"
}

resource "some_other_typer" "coverage" {}
