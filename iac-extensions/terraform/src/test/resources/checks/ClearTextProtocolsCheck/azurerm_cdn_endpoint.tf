# Noncompliant@+1 {{Omitting is_http_allowed enables clear-text traffic. Make sure it is safe here.}}
resource "azurerm_cdn_endpoint" "missing_is_http_allowed" {
}

resource "azurerm_cdn_endpoint" "false_is_http_allowed" {
  is_http_allowed = false
}

resource "azurerm_cdn_endpoint" "true_is_http_allowed" {
  is_http_allowed = true  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^
}

resource "not_an_azurerm_cdn_endpoint" "for_coverage" {
}
