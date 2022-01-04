# Noncompliant@+1 {{Omitting the 'basic_authentication' block disables authentication. Make sure it is safe here.}}
resource "azurerm_data_factory_linked_service_odata" "sensitive" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_data_factory_linked_service_odata" "compliant" { # Compliant
  basic_authentication {
    username = local.creds.username
    password = local.creds.password
  }
}

resource "azurerm_data_factory_linked_service_odata" "compliant_empty" { # Compliant
  basic_authentication {
  }
}

resource "azurerm_data_factory_linked_service_sftp" "example2" {
  # Noncompliant@+1 {{Make sure that authorizing anonymous access is safe here.}}
  authentication_type = "Anonymous"
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_data_factory_linked_service_sftp" "compliant" { # Compliant
  authentication_type = "Basic"
  username            = local.creds.username
  password            = local.creds.password
}

resource "azurerm_data_factory_linked_service_sftp" "compliant_missing" {
}


resource "other_resource" "coverage" {
  authentication_type = "Anonymous"
}

