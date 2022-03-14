resource "azurerm_storage_account" "sa_noncompliant_outdated" {
  min_tls_version = "TLS1_1"  # Noncompliant {{Change this code to disable support of older TLS versions.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Set "min_tls_version" to disable support of older TLS versions.}}
resource "azurerm_storage_account" "sa_noncompliant_missing" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_storage_account" "sa_compliant" {
  min_tls_version = "TLS1_2"
}

resource "not_azurerm_storage_account" "sa_coverage" {
  min_tls_version = "TLS1_1"
}
