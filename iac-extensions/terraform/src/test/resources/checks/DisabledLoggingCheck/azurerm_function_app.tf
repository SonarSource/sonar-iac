resource "azurerm_function_app" "s6258_fa_nc" {
  enable_builtin_logging = false # Noncompliant {{Make sure that disabling built-in logging is safe here.}}
}

resource "azurerm_function_app_slot" "s6258_fas_nc" {
  enable_builtin_logging = false # Noncompliant
}

resource "azurerm_function_app" "s6258_fa_c" {
  enable_builtin_logging = true
}

resource "azurerm_function_app" "s6258_fa_c" {
  # enabaled by default
}

resource "non_azurerm_function_app" "s6258_fa_cov" {
  enable_builtin_logging = false
}
