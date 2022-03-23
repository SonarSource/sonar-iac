resource "azurerm_mssql_server_extended_auditing_policy" "s6413-mssql-policy-nc1" {
  retention_in_days  = 13  # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
}

resource "azurerm_mssql_server_extended_auditing_policy" "s6413-mssql-policy-c1" {
  retention_in_days  = 14
}

resource "azurerm_mssql_server_extended_auditing_policy" "s6413-mssql-policy-c2" {
  retention_in_days  = 0
}

resource "azurerm_mssql_server_extended_auditing_policy" "s6413-mssql-policy-c3" {
}

resource "non_azurerm_mssql_server_extended_auditing_policy" "s6413-mssql-policy-cov" {
  retention_in_days  = 13
}
