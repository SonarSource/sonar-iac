resource "azurerm_api_management" "sensitive" {
  sign_in {
  # Noncompliant@+1 {{Make sure that giving anonymous access without enforcing sign-in is safe here.}}
    enabled = false
  # ^^^^^^^^^^^^^^^
  }
}

# Noncompliant@+1 {{Omitting 'sign_in' authorizes anonymous access. Make sure it is safe here.}}
resource "azurerm_api_management" "sensitive_missing" {
}

resource "azurerm_api_management" "compliant" {
  sign_in {
    enabled = true
  }
}

resource "other_resource" "coverage" {
  sign_in {
    enabled = false
  }
}

