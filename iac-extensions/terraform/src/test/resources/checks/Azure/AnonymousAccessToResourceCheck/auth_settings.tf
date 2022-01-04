# Noncompliant@+1 {{Omitting 'auth_settings' disables authentication. Make sure it is safe here.}}
resource "azurerm_app_service_slot" "sensitive_web_app" {
}

resource "azurerm_app_service_slot" "sensitive_web_app" {
  auth_settings {
    enabled                       = true
    # Noncompliant@+1 {{Make sure that authorizing anonymous access is safe here.}}
    unauthenticated_client_action = "AllowAnonymous"
  }
}

resource "azurerm_app_service_slot" "sensitive_web_app" {
  auth_settings { # Noncompliant
    enabled = true
  }
}

resource "azurerm_app_service_slot" "sensitive_web_app" {
  auth_settings {
    # Noncompliant@+1 {{Make sure that disabling authentication is safe here.}}
    enabled = false
    unauthenticated_client_action = "AllowAnonymous"
  }
}

resource "azurerm_app_service" "sensitive_web_app" { # Noncompliant
}
resource "azurerm_function_app" "sensitive_web_app" { # Noncompliant
}
resource "azurerm_function_app_slot" "sensitive_web_app" { # Noncompliant
}
resource "azurerm_windows_web_app" "sensitive_web_app" { # Noncompliant
}
resource "azurerm_linux_web_app" "sensitive_web_app" { # Noncompliant
}

resource "azurerm_app_service_slot" "compliant_web_app" {
  auth_settings {
    enabled                       = true
    unauthenticated_client_action = "RedirectToLoginPage"
  }
}

resource "other_resource" "coverage" {
  auth_settings {
    enabled = false
    unauthenticated_client_action = "AllowAnonymous"
  }
}
