resource "azurerm_redis_cache" "sensitive" {
  name = "example-cache"

  redis_configuration {
  # Noncompliant@+1 {{Make sure that disabling authentication is safe here.}}
    enable_authentication = false
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "azurerm_redis_cache" "compliant" {
  name = "example-cache"

  redis_configuration {
    enable_authentication = true
  }
}

resource "azurerm_redis_cache" "compliant_missing" {
  name = "example-cache"

  redis_configuration {
  }
}

resource "azurerm_redis_cache" "compliant_missing_block" {
  name = "example-cache"
}

resource "other_resource" "coverage" {
  name = "example-cache"

  redis_configuration {
    enable_authentication = false
  }
}
