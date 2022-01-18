resource "azurerm_redis_enterprise_database" "non_plaintext" {
  client_protocol = "aes256"
}

resource "azurerm_redis_enterprise_database" "plaintext" {
  client_protocol = "PlainText" # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "not_an_azurerm_redis_enterprise_database" "for_coverage" {
}
