resource "azurerm_storage_container" "sensitive" {
# Noncompliant@+1 {{Make sure that authorizing potential anonymous access is safe here.}}
  container_access_type = "blob"
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_storage_container" "compliant" {
  container_access_type = "private"
}

resource "azurerm_storage_container" "compliant_missing" {
}

resource "other_resource" "coverage" {
  container_access_type = "blob"
}
