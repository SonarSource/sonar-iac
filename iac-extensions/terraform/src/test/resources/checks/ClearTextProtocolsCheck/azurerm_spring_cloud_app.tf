# Noncompliant@+1 {{Omitting https_only enables clear-text traffic. Make sure it is safe here.}}
resource "azurerm_spring_cloud_app" "noncompliant_https_missing" {
}

resource "azurerm_spring_cloud_app" "noncompliant_https_disabled" {
  https_only = false  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
# ^^^^^^^^^^^^^^^^^^
}

resource "azurerm_spring_cloud_app" "safe_https_value" {
  https_only = ABRACADABRA
}

resource "not_an_azurerm_spring_cloud_app" "for_coverage" {
}
