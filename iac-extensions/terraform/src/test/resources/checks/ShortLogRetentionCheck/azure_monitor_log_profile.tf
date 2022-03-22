resource "azurerm_monitor_log_profile" "s6413-mlp-nc1" {
  retention_policy {
    enabled = true
    days    = 13 # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
  }
}

resource "azurerm_monitor_log_profile" "s6413-mlp-nc2" {
  retention_policy {
    enabled = false # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
    days    = 14
  }
}

resource "azurerm_monitor_log_profile" "s6413-mlp-nc3" {
  retention_policy {
    days    = 13 # Noncompliant
  }
}

resource "azurerm_monitor_log_profile" "s6413-mlp-nc4" {
  retention_policy {
    enabled = false # Noncompliant
  }
}

# Report only a single attribute instead of two for each rule violation
resource "azurerm_monitor_log_profile" "s6413-mlp-nc5" {
  retention_policy {
    enabled = false # Noncompliant
    days    = 13
  }
}

resource "azurerm_monitor_log_profile" "s6413-mlp-c1" {
  retention_policy {
    enabled = true
    days    = 14
  }
}

resource "azurerm_monitor_log_profile" "s6413-mlp-c2" {
  retention_policy {
    days    = 14
  }
}

resource "azurerm_monitor_log_profile" "s6413-mlp-c3" {
  retention_policy {
    enabled = true
  }
}

resource "azurerm_monitor_log_profile" "s6413-mlp-c4" {
  retention_policy {
  }
}

resource "azurerm_monitor_log_profile" "s6413-mlp-c5" {
  retention_policy {
    enabled = true
    days    = 0
  }
}

resource "azurerm_network_watcher_flow_log" "s6413-nwfl-nc1" {
  retention_policy {
    enabled = true
    days    = 13 # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
  }
}

resource "azurerm_network_watcher_flow_log" "s6413-nwfl-nc2" {
  retention_policy {
    enabled = false # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
    days    = 14
  }
}

resource "non_azurerm_monitor_log_profile" "s6413-mlp-cov" {
  retention_policy {
    enabled = false
    days    = 13
  }
}

