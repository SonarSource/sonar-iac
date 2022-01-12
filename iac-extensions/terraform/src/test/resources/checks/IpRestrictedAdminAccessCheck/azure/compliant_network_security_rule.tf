# Direction is not Inbound
resource "azurerm_network_security_rule" "s6321compliant0" {
  direction              = "Outbound"
  access                 = "Allow"
  protocol               = "Tcp"
  destination_port_range = "*"
  source_address_prefix  = "*"
}

resource "nonazurerm_network_security_rule" "coverage" {
  direction              = "Inbound"
  access                 = "Allow"
  protocol               = "Tcp"
  destination_port_range = "*"
  source_address_prefix  = "*"
}
