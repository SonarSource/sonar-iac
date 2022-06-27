resource "azurerm_kubernetes_cluster" "rbac_disabled" {
  role_based_access_control {
    enabled = false # Noncompliant {{Make sure that disabling role-based access control is safe here.}}
  # ^^^^^^^^^^^^^^^
  }
}

resource "azurerm_kubernetes_cluster" "ac_rbac_disabled" {
  role_based_access_control {
    enabled = true
    azure_active_directory {
      managed = true
      azure_rbac_enabled = false # Noncompliant
    }
  }
}

resource "azurerm_kubernetes_cluster" "ac_rbac_disabled" {
  role_based_access_control {
    enabled = true
    azure_active_directory {
      managed = true # Noncompliant
    }
  }
}

resource "azurerm_kubernetes_cluster" "rbac_enabled" { # Compliant
  role_based_access_control {
    enabled = true
  }
}

resource "azurerm_kubernetes_cluster" "ac_rbac_enabled" { # Compliant
  role_based_access_control {
    enabled = true
    azure_active_directory {
      managed = true
      azure_rbac_enabled = true
    }
  }
}

# Noncompliant@+1 {{Omitting 'role_based_access_control' disables role-based access control for this resource. Make sure it is safe here.}}
resource "azurerm_kubernetes_cluster" "missing_role_based_access_control" {
}


resource "azurerm" "coverage" { # Compliant
}
