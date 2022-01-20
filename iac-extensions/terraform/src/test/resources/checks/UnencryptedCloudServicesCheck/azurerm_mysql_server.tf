# Noncompliant@+1 {{Omitting infrastructure_encryption_enabled enables clear-text storage. Make sure it is safe here.}}
resource "azurerm_mysql_server" "non-concompliant-1" {
}

resource "azurerm_mysql_server" "non-compliant-2" {
  infrastructure_encryption_enabled = false # Noncompliant {{Make sure using unencrypted cloud storage is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_mysql_server" "compliant-1" {
  infrastructure_encryption_enabled = true
}

resource "azurerm_mysql_server" "compliant-2" {
  infrastructure_encryption_enabled = "something"
}

resource "non_azurerm_mysql_server" "compliant-3" {
}
