resource "azurerm_storage_account" "sa_noncompliant_outdated" {
  min_tls_version = "TLS1_1"  # Noncompliant {{Change this configuration to use a stronger protocol.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting min_tls_version disables traffic encryption. Make sure it is safe here.}}
resource "azurerm_storage_account" "sa_noncompliant_missing" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_storage_account" "sa_compliant" {
  min_tls_version = "TLS1_2"
}

resource "not_azurerm_storage_account" "sa_coverage" {
  min_tls_version = "TLS1_1"
}
