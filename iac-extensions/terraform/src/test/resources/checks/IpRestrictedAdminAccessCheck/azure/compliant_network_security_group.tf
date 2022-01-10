resource "azurerm_network_security_group" "s6321compliant0" {

  # Direction is not Inbound
  security_rule {
    direction                  = "Outbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "*"
    source_address_prefix      = "*"
  }

  # Access is not Allow
  security_rule {
    direction                  = "Inbound"
    access                     = "Deny"
    protocol                   = "Tcp"
    destination_port_range     = "*"
    source_address_prefix      = "*"
  }

  # Protocol is not Tcp or *
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Udp"
    destination_port_range     = "*"
    source_address_prefix      = "*"
  }

  # Port range does not contain 22 nor 3389
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "10-21"
    source_address_prefix      = "*"
  }

  # Source prefix is not *, 0.0.0.0/0, or ::/0
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "*"
    source_address_prefix      = "192.168.0.1/24"
  }

  # Source prefixes do not contain *, 0.0.0.0/0, or ::/0
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "*"
    source_address_prefixes      = ["192.168.0.1/24", "192.134.0.1/24"]
  }

  # Non readable port range
  security_rule {
    direction              = "Inbound"
    access                 = "Allow"
    protocol               = "Tcp"
    destination_port_range = "22-23+28"
    source_address_prefix  = "*"
  }
}

resource "non_azurerm_network_security_group_resource" "coverage" {
  # Coverage
  security_rule {
    direction              = "Inbound"
    access                 = "Allow"
    protocol               = "Tcp"
    destination_port_range = "*"
    source_address_prefix  = "*"
  }
}
