resource "azurerm_storage_account" "sensitive" {
# Noncompliant@+1 {{Make sure that authorizing potential anonymous access is safe here.}}
  allow_blob_public_access = true
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_storage_account" "compliant" {
  allow_blob_public_access = false
}

resource "azurerm_storage_account" "compliant_missing" {
}

resource "other_resource" "coverage" {
  allow_blob_public_access = true
}
