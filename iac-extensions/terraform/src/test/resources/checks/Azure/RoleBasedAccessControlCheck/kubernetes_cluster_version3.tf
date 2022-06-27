resource "azurerm_kubernetes_cluster" "rbac_disabled" {
  role_based_access_control {
    enabled = false # Noncompliant
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

resource "azurerm_kubernetes_cluster" "role_based_access_control_enabled" {
  role_based_access_control_enabled = false # Noncompliant {{Make sure that disabling role-based access control is safe here.}}
}

resource "azurerm_kubernetes_cluster" "role_based_access_control_enabled" {
  role_based_access_control_enabled = true
}

resource "azurerm_kubernetes_cluster" "noncompliant_azure_active_directory_role_based_access_control" {
  azure_active_directory_role_based_access_control {
    managed = true
    azure_rbac_enabled = false # Noncompliant {{Make sure that disabling role-based access control is safe here.}}
  }
}

resource "azurerm_kubernetes_cluster" "incomplete_azure_active_directory_role_based_access_control" {
  azure_active_directory_role_based_access_control {
    managed = true # Noncompliant {{Omitting 'azure_rbac_enabled' disables role-based access control for this resource. Make sure it is safe here.}}
  }
}

resource "azurerm_kubernetes_cluster" "compliant_azure_active_directory_role_based_access_control" {
  azure_active_directory_role_based_access_control {
    managed = false
  }
}


resource "azurerm_kubernetes_cluster" "missing_role_based_access_control" {
}
