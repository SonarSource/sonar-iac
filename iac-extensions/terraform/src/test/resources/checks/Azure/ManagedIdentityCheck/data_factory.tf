resource "azurerm_data_factory_linked_service_kusto" "sensitive" {
  name                 = "example"
  use_managed_identity = false # Noncompliant {{Make sure that disabling Azure Managed Identities is safe here.}}
  #                      ^^^^^
}

resource "azurerm_data_factory_linked_service_kusto" "missing" {
  name                 = "example"
}

resource "azurerm_data_factory_linked_service_kusto" "compliant" {
  name                 = "example"
  use_managed_identity = true
}

resource "some_other_type" "coverage" {
}
