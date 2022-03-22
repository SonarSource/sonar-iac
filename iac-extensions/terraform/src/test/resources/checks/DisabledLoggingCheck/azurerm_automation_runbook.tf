resource "azurerm_automation_runbook" "s6258_ar_nc" {
  log_progress = "false" # Noncompliant {{Make sure that disabling progress logging is safe here.}}
}

resource "azurerm_automation_runbook" "s6258_ar_nc" {
  log_progress = false # Noncompliant
}

resource "azurerm_automation_runbook" "s6258_ar_c" {
  log_progress = "true"
}

# Noncompliant@+1 {{Make sure that omitting the activation of progress logging is safe here.}}
resource "azurerm_automation_runbook" "s6258_ar_nc_missing" {
  # disabled by default
}

resource "non_azurerm_automation_runbook" "s6258_ar_cov" {
  log_progress = "false"
}
