resource "azurerm_mysql_server" "non_plaintext" {
  ssl_enforcement_enabled = true
}

resource "azurerm_mysql_server" "plaintext" {
  ssl_enforcement_enabled = false # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "something_something" "non_sensitive" {
  ssl_enforcement_enabled = false
}

###

resource "azurerm_postgresql_server" "non_plaintext" {
  ssl_enforcement_enabled = true
}

resource "azurerm_postgresql_server" "plaintext" {
  ssl_enforcement_enabled = false # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}
