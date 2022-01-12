resource "azurerm_data_factory" "noncompliant_enabled" {
  public_network_enabled = true # Noncompliant {{Make sure allowing public network access is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting public_network_enabled allows network access from the Internet. Make sure it is safe here.}}
resource "azurerm_data_factory" "noncompliant_missing" {

}

resource "azurerm_data_factory" "compliant" {
  public_network_enabled = false
}

resource "azurerm_purview_account" "noncompliant_missing" {} # Noncompliant

resource "non_azurerm_data_factory" "coverage" {
  public_network_enabled = true
}
