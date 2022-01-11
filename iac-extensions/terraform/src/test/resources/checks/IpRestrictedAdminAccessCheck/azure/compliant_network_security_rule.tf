# Direction is not Inbound
resource "azurerm_network_security_rule" "s6321compliant0" {
  direction              = "Outbound"
  access                 = "Allow"
  protocol               = "Tcp"
  destination_port_range = "*"
  source_address_prefix  = "*"
}

# Access is not Allow
resource "azurerm_network_security_rule" "s6321compliant0" {
  direction              = "Inbound"
  access                 = "Deny"
  protocol               = "Tcp"
  destination_port_range = "*"
  source_address_prefix  = "*"
}

# Protocol is not Tcp or *
resource "azurerm_network_security_rule" "s6321compliant0" {
  direction              = "Inbound"
  access                 = "Allow"
  protocol               = "Udp"
  destination_port_range = "*"
  source_address_prefix  = "*"
}

# Port range does not contain 22 nor 3389
resource "azurerm_network_security_rule" "s6321compliant0" {
  direction              = "Inbound"
  access                 = "Allow"
  protocol               = "Tcp"
  destination_port_range = "10-21"
  source_address_prefix  = "*"
}

# Source prefix is not *, 0.0.0.0/0, or ::/0
resource "azurerm_network_security_rule" "s6321compliant0" {
  direction              = "Inbound"
  access                 = "Allow"
  protocol               = "Tcp"
  destination_port_range = "*"
  source_address_prefix  = "192.168.0.1/24"
}

# Source prefixes do not contain *, 0.0.0.0/0, or ::/0
resource "azurerm_network_security_rule" "s6321compliant0" {
  direction               = "Inbound"
  access                  = "Allow"
  protocol                = "Tcp"
  destination_port_range  = "*"
  source_address_prefixes = ["192.168.0.1/24", "192.134.0.1/24"]
}

# Non readable port range
resource "azurerm_network_security_rule" "s6321compliant0" {
  direction              = "Inbound"
  access                 = "Allow"
  protocol               = "Tcp"
  destination_port_range = "22-23+28"
  source_address_prefix  = "*"
}

# No valid port in port range
resource "azurerm_network_security_rule" "s6321compliant0" {
  direction              = "Inbound"
  access                 = "Allow"
  protocol               = "Tcp"
  destination_port_range = "1-12345678901"
  source_address_prefix  = "*"
}

resource "nonazurerm_network_security_rule" "coverage" {
  direction              = "Inbound"
  access                 = "Allow"
  protocol               = "Tcp"
  destination_port_range = "*"
  source_address_prefix  = "*"
}
