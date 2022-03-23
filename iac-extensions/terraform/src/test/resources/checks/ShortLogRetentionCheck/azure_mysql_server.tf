resource "azurerm_mysql_server" "s6413-mysqls-nc1" {
  threat_detection_policy {
    retention_days = 13  # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
  }
}

resource "azurerm_postgresql_server" "s6413-mysqls-nc2" {
  threat_detection_policy {
    retention_days = 13  # Noncompliant
  }
}

resource "azurerm_sql_server" "s6413-mysqls-nc3" {
  threat_detection_policy {
    retention_days = 13  # Noncompliant
  }
}


resource "azurerm_mysql_server" "s6413-mysqls-c1" {
  threat_detection_policy {
    retention_days = 14
  }
}

resource "azurerm_mysql_server" "s6413-mysqls-c2" {
  threat_detection_policy {
    retention_days = 0
  }
}

resource "azurerm_mysql_server" "s6413-mysqls-c3" {
  threat_detection_policy {
  }
}

resource "azurerm_mysql_server" "s6413-mysqls-c4" {
}

resource "non_azurerm_mysql_server" "s6413-mysqls-cov" {
  threat_detection_policy {
    retention_days = 13
  }
}
