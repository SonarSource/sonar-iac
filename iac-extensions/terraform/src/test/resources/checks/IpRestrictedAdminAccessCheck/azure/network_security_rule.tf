# Tcp protocol
resource "azurerm_network_security_rule" "s6321noncompliant0" {
  direction              = "Inbound"
  access                 = "Allow"
  protocol               = "Tcp"
  destination_port_range = "*"
  #                        ^^^> {{Related protocol setting.}}
  source_address_prefix  = "*"  # Noncompliant {{Restrict IP addresses authorized to access administration services.}}
  #                        ^^^
}

# Direction is not Inbound
resource "azurerm_network_security_rule" "s6321compliant0" {
  direction              = "Outbound"
  access                 = "Allow"
  protocol               = "Tcp"
  destination_port_range = "*"
  source_address_prefix  = "*"
}

# Coverage
resource "nonazurerm_network_security_rule" "coverage" {
  direction              = "Inbound"
  access                 = "Allow"
  protocol               = "Tcp"
  destination_port_range = "*"
  source_address_prefix  = "*"
}

