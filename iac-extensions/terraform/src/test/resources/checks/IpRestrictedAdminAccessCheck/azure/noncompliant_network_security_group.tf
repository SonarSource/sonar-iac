resource "azurerm_network_security_group" "s6321noncompliant0" {

  # Tcp protocol
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "*"  # Secondary location
  #                              ^^^> {{Related protocol setting.}}
    source_address_prefix      = "*"  # Noncompliant {{Restrict IP addresses authorized to access administration services.}}
  #                              ^^^
  }

  # All protocol
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "*"
    destination_port_range     = "*"  # Secondary location
    source_address_prefix      = "*"  # Noncompliant
  }

  # Port 22
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "22"  # Secondary location
    source_address_prefix      = "*"  # Noncompliant
  }

  # Port 3389
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "3389"  # Secondary location
    source_address_prefix      = "*"  # Noncompliant
  }

  # Port range starting 22
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "22-80"  # Secondary location
    source_address_prefix      = "*"  # Noncompliant
  }

  # Port range ending with 3389
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "80-3389"  # Secondary location
    source_address_prefix      = "*"  # Noncompliant
  }

  # Ports including 3389
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_ranges     = ["891", "3389", "234"]  # Secondary location
    source_address_prefix      = "*"  # Noncompliant
  }

  # Ports including 22
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_ranges     = ["891", "22", "234"]  # Secondary location
    source_address_prefix      = "*"  # Noncompliant
  }

  # Ports including 80 inside a range
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_ranges     = ["891", "10-40", "234"]  # Secondary location
    source_address_prefix      = "*"  # Noncompliant
  }

  # Source prefix is 0.0.0.0/0
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "*"  # Secondary location
    source_address_prefix      = "0.0.0.0/0"  # Noncompliant
  }

  # Source prefix is ::/0
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "*"  # Secondary location
    source_address_prefix      = "::/0"  # Noncompliant
  }

  # Source prefixes contain *
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "*"  # Secondary location
    source_address_prefixes    = ["192.168.0.1/24", "*"]  # Noncompliant
  }

  # Source prefixes contain 0.0.0.0/0
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "*"  # Secondary location
    source_address_prefixes    = ["192.168.0.1/24", "0.0.0.0/0"]  # Noncompliant
  }

  # Source prefixes contain ::/0
  security_rule {
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    destination_port_range     = "*"  # Secondary location
    source_address_prefixes    = ["192.168.0.1/24", "::/0"]  # Noncompliant
  }
}
