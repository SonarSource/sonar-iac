resource "azurerm_container_group" "s6258_cg_c" {
  diagnostic {
  }
}

# Noncompliant@+1 {{This resource does not have diagnostic logs enabled. Make sure it is safe here.}}
resource "azurerm_container_group" "s6258_cg_nc" {
}

resource "non_azurerm_container_group" "s6258_cg_cov" {
}
