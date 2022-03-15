# Noncompliant@+1 {{Omitting "https_only" enables clear-text traffic. Make sure it is safe here.}}
resource "azurerm_app_service" "noncompliant_https_missing" {
}

resource "azurerm_app_service" "noncompliant_https_disabled" {
  https_only = false  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
# ^^^^^^^^^^^^^^^^^^
}

resource "azurerm_app_service" "safe_https_value" {
  https_only = true
}

resource "not_an_azurerm_app_service" "for_coverage" {
}

######

# Noncompliant@+1 {{Omitting "https_only" enables clear-text traffic. Make sure it is safe here.}}
resource "azurerm_app_service" "noncompliant_all_allowed" {
  site_config {
    ftps_state = "AllAllowed"  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
#   ^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "azurerm_app_service" "noncompliant_all_allowed" {
  https_only = false  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
# ^^^^^^^^^^^^^^^^^^
  site_config {
    ftps_state = "AllAllowed"  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
#   ^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "azurerm_app_service" "noncompliant_all_allowed" {
  https_only = true
  site_config {
    ftps_state = "AllAllowed"  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
#   ^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

######

# Noncompliant@+1 {{Omitting "https_only" enables clear-text traffic. Make sure it is safe here.}}
resource "azurerm_app_service" "compliant_some_allowed" {
  site_config {
    ftps_state = "SomeAllowed"  # Compliant
  }
}

resource "azurerm_app_service" "compliant_some_allowed" {
  https_only = false  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
# ^^^^^^^^^^^^^^^^^^
  site_config {
    ftps_state = "SomeAllowed"  # Compliant
  }
}

resource "azurerm_app_service" "compliant_some_allowed" {
  https_only = true
  site_config {
    ftps_state = "SomeAllowed"  # Compliant
  }
}
