resource "azurerm_mssql_server" "mssql_noncompliant_old" {
  minimum_tls_version = "1.0"  # Noncompliant {{Change this code to disable support of older TLS versions.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_mssql_server" "mssql_noncompliant_old" {
  minimum_tls_version = "Disabled"  # Noncompliant
}

resource "azurerm_mssql_server" "mssql_noncompliant_old" {
  minimum_tls_version = "invalid value"  # Noncompliant
}

resource "azurerm_mssql_server" "mssql_compliant_missing" {
}

resource "azurerm_mssql_server" "mssql_compliant" {
  minimum_tls_version  = "1.2"
}

resource "not_azurerm_mssql_server" "mssql_coverage" {
  minimum_tls_version  = "TLS1_0"
}
