###### kubernetes_cluster ######

resource "azurerm_kubernetes_cluster" "production" {

  api_server_authorized_ip_ranges = [
    "192.168.27.2",
    "51.67.288.198", # Noncompliant {{Make sure allowing public network access is safe here.}}
  # ^^^^^^^^^^^^^^^
    "172.16.0.0/12",
    "12.23.45.67" # Noncompliant
  ]

  default_node_pool {
    # Noncompliant@+1 {{Make sure allowing public network access is safe here.}}
    enable_node_public_ip = true
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "azurerm_kubernetes_cluster" "production" {

  api_server_authorized_ip_ranges = [
    "192.168.27.2",
    "0.0.0.0/32",
    "172.16.0.0/12",
    "10.9.2.4",
    var.any.ip.address
  ]

  default_node_pool {
    enable_node_public_ip = false
  }
}

resource "azurerm_kubernetes_cluster" "production" {
  default_node_pool {
    enable_node_public_ip = false
  }
}

resource "azurerm_kubernetes_cluster" "production" {
  api_server_authorized_ip_ranges = [
    "10.9.2.4"
  ]
}
