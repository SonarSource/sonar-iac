resource "azurerm_firewall_policy" "s6413-fp-nc1" {
  insights {
    retention_in_days = 13 # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
  }
}

resource "azurerm_firewall_policy" "s6413-fp-c1" {
  insights {
    retention_in_days = 14
  }
}

resource "azurerm_firewall_policy" "s6413-fp-c2" {
  insights {
    retention_in_days = 0
  }
}

resource "azurerm_firewall_policy" "s6413-fp-c3" {
  insights {
  }
}

resource "azurerm_firewall_policy" "s6413-fp-c3" {
}


resource "non_azurerm_firewall_policy" "s6413-fp-cov" {
  insights {
    retention_in_days = 13
  }
}
