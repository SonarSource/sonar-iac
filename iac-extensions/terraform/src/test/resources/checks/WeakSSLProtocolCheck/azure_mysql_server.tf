resource "azurerm_mysql_server" "mysql_noncompliant_old" {
  ssl_minimal_tls_version_enforced  = "TLS1_0"  # Noncompliant {{Change this code to disable support of older TLS versions.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Set "ssl_minimal_tls_version_enforced" to disable support of older TLS versions.}}
resource "azurerm_mysql_server" "mysql_noncompliant_missing" {
  #      ^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_mysql_server" "mysql_compliant" {
  ssl_minimal_tls_version_enforced  = "TLS1_2"
}

resource "not_azurerm_mysql_server" "mysql_coverage" {
  ssl_minimal_tls_version_enforced  = "TLS1_0"
}
