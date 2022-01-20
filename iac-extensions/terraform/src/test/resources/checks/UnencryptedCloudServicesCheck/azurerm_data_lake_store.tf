resource "azurerm_data_lake_store" "concompliant" {
  encryption_state = "Disabled"  # Noncompliant {{Make sure using unencrypted cloud storage is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_data_lake_store" "compliant-1" {
  encryption_state = "Enabled"
}

resource "azurerm_data_lake_store" "compliant-2" {
}

resource "non_azurerm_data_lake_store" "compliant-3" {
  encryption_state = "Disabled"
}
