resource "azurerm_postgresql_server" "pgsql_noncompliant_old" {
  ssl_minimal_tls_version_enforced = "TLS1_0"  # Noncompliant {{Change this code to disable support of older TLS versions.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Set "ssl_minimal_tls_version_enforced" to disable support of older TLS versions.}}
resource "azurerm_postgresql_server" "pgsql_noncompliant_missing" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_postgresql_server" "pgsql_compliant" {
  ssl_minimal_tls_version_enforced = "TLS1_2"
}

resource "not_azurerm_postgresql_server" "pgsql_coverage" {
  ssl_minimal_tls_version_enforced = "TLS1_0"
}
