resource "azurerm_mysql_server" "mysql_noncompliant_old" {
  ssl_minimal_tls_version_enforced  = "TLS1_0"  # Noncompliant {{Change this configuration to use a stronger protocol.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting ssl_minimal_tls_version_enforced disables traffic encryption. Make sure it is safe here.}}
resource "azurerm_mysql_server" "mysql_noncompliant_missing" {
  #      ^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_mysql_server" "mysql_compliant" {
  ssl_minimal_tls_version_enforced  = "TLS1_2"
}

resource "not_azurerm_mysql_server" "mysql_coverage" {
  ssl_minimal_tls_version_enforced  = "TLS1_0"
}
