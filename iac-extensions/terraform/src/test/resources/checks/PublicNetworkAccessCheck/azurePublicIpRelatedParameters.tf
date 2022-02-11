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
    public_ip_address_id = foo.azurerm_public_ip
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

###### dev_test_windows_virtual_machine ######

resource "azurerm_dev_test_windows_virtual_machine" "noncompliant" {
  # Noncompliant@+1 {{Make sure allowing public network access is safe here.}}
  disallow_public_ip_address = false
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

###### dev_test_virtual_network ######

resource "azurerm_dev_test_virtual_network" "noncompliant" {
  subnet {
    # Noncompliant@+1 {{Make sure allowing public network access is safe here.}}
    use_public_ip_address = "Allow"
  }
}

resource "azurerm_dev_test_virtual_network" "compliant" {
  subnet { }
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

###### kubernetes_cluster_node_pool ######

resource "azurerm_kubernetes_cluster_node_pool" "noncompliant" {
  # Noncompliant@+1 {{Make sure allowing public network access is safe here.}}
  enable_node_public_ip = true
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_kubernetes_cluster_node_pool" "compliant" {
  enable_node_public_ip = false
}

resource "azurerm_kubernetes_cluster_node_pool" "compliant" {
}

###### network_interface ######

resource "azurerm_network_interface" "production" {
  ip_configuration {
  # Noncompliant@+1 {{Make sure allowing public network access is safe here.}}
    public_ip_address_id = azurerm_public_ip.production.id
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "azurerm_network_interface" "production" {
  ip_configuration {
    public_ip_address_id = foo.azurerm_public_ip
  }
}
