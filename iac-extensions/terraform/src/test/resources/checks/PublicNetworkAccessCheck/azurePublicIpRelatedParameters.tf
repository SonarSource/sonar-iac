###### application_gateway ######

resource "azurerm_application_gateway" "noncompliant" {
  frontend_ip_configuration {
  # Noncompliant@+1 {{Make sure allowing public network access is safe here.}}
    public_ip_address_id = azurerm_public_ip.production.id
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "azurerm_application_gateway" "compliant" {
  frontend_ip_configuration {
    public_ip_address_id = foo.production.id
  }
}

resource "azurerm_application_gateway" "compliant" {
  frontend_ip_configuration {
    public_ip_address_id = 123456789
  }
}

resource "azurerm_application_gateway" "compliant" {
  frontend_ip_configuration {
  }
}

resource "azurerm_application_gateway" "compliant" {
}

###### dev_test_linux_virtual_machine ######

resource "azurerm_dev_test_linux_virtual_machine" "noncompliant" {
  # Noncompliant@+1 {{Make sure allowing public network access is safe here.}}
  disallow_public_ip_address = false
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting disallow_public_ip_address allows network access from the Internet. Make sure it is safe here.}}
resource "azurerm_dev_test_linux_virtual_machine" "noncompliant" {
       # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_dev_test_linux_virtual_machine" "compliant" {
  disallow_public_ip_address = true
}

###### dev_test_virtual_network ######

resource "azurerm_dev_test_virtual_network" "noncompliant" {
  subnet {
    # Noncompliant@+1 {{Make sure allowing public network access is safe here.}}
    use_public_ip_address = "Allow"
  }
}

resource "azurerm_dev_test_virtual_network" "noncompliant" {
  # Noncompliant@+1 {{Omitting use_public_ip_address allows network access from the Internet. Make sure it is safe here.}}
  subnet { }
# ^^^^^^
}

resource "azurerm_dev_test_virtual_network" "compliant" {
  subnet {
    use_public_ip_address = "Deny"
  }
}

resource "azurerm_dev_test_virtual_network" "compliant_with_reference" {
  subnet {
    use_public_ip_address = var.subnet.behavior
  }
}
