# Noncompliant@+1 {{Omitting disk_encryption_set_id enables clear-text storage. Make sure it is safe here.}}
resource "azurerm_managed_disk" "non-concompliant" {
}

resource "azurerm_managed_disk" "compliant-1" {
  disk_encryption_set_id = "something"
}

resource "non_azurerm_data_lake_store" "compliant-2" {
}
