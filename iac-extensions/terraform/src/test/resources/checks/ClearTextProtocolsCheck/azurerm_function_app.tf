# Noncompliant@+1 {{Omitting "https_only" enables clear-text traffic. Make sure it is safe here.}}
resource "azurerm_function_app" "noncompliant_https_missing" {
}

resource "azurerm_function_app" "noncompliant_https_disabled" {
  https_only = false  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
# ^^^^^^^^^^^^^^^^^^
}

resource "azurerm_function_app" "safe_https_value" {
  https_only = true
}

resource "not_an_azurerm_function_app" "for_coverage" {
}

# Noncompliant@+1 {{Omitting "https_only" enables clear-text traffic. Make sure it is safe here.}}
resource "azurerm_function_app_slot" "noncompliant_https_missing" {
}

resource "azurerm_function_app_slot" "noncompliant_https_disabled" {
  https_only = false  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
# ^^^^^^^^^^^^^^^^^^
}

resource "azurerm_function_app_slot" "safe_https_value" {
  https_only = true
}
