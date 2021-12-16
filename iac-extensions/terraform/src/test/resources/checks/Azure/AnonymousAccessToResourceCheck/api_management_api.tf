# Noncompliant@+1 {{Omitting 'openid_authentication' disables authentication. Make sure it is safe here.}}
resource "azurerm_api_management_api" "sensitive" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name                = "example-api"
}

resource "azurerm_api_management_api" "compliant" { # Compliant
  name                = "example-api"
  openid_authentication {
    openid_provider_name = "example-provider"
    bearer_token_sending_methods = ["authorizationHeader"]
  }
}

resource "other_resource" "coverage" {
}
