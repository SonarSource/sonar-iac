resource "azurerm_machine_learning_workspace" "noncompliant" {
  public_network_access_enabled = true # Noncompliant {{Make sure allowing public network access is safe here.}}
}

resource "azurerm_machine_learning_workspace" "compliant_value" {
  public_network_access_enabled = false
}

resource "azurerm_machine_learning_workspace" "compliant_missing" {
  # defaults to false
}

resource "non_azurerm_machine_learning_workspace" "coverage" {
  public_network_access_enabled = false
}
