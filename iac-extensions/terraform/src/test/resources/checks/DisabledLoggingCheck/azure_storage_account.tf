resource "azurerm_storage_account" "s6258_sa_nc1" {
  account_kind = "AnythingElse"
  queue_properties {
    logging { # Noncompliant {{Make sure that disabling logging is safe here.}}
      delete = false
      read   = false
      write  = false
    }
  }
}

resource "azurerm_storage_account" "s6258_sa_nc2" {
  account_kind = "AnythingElse"
  queue_properties {
    logging { # Noncompliant {{Make sure that partially enabling logging is safe here.}}
      delete = true
      read   = false
      write  = false
    }
  }
}

resource "azurerm_storage_account" "s6258_sa_nc3" {
  account_kind = "AnythingElse"
  queue_properties {
    logging { # Noncompliant {{Make sure that partially enabling logging is safe here.}}
      delete = false
      read   = true
      write  = false
    }
  }
}

resource "azurerm_storage_account" "s6258_sa_nc4" {
  account_kind = "AnythingElse"
  queue_properties {
    logging { # Noncompliant {{Make sure that partially enabling logging is safe here.}}
      delete = false
      read   = false
      write  = true
    }
  }
}

resource "azurerm_storage_account" "s6258_sa_nc4" {
  account_kind = "AnythingElse"
  queue_properties {
    logging {
      delete = false # Noncompliant {{Make sure that partially enabling logging is safe here.}}
      read   = true
      write  = true
    }
  }
}

resource "azurerm_storage_account" "s6258_sa_nc5" {
  account_kind = "AnythingElse"
  queue_properties {
    logging {
      delete = true
      read   = false # Noncompliant
      write  = true
    }
  }
}

resource "azurerm_storage_account" "s6258_sa_nc6" {
  account_kind = "AnythingElse"
  queue_properties {
    logging {
      delete = true
      read   = true
      write  = false # Noncompliant
    }
  }
}

resource "azurerm_storage_account" "s6258_sa_nc7" {
  account_kind = "AnythingElse"
  queue_properties { # Noncompliant {{Make sure that omitting to log is safe here.}}
  }
}

# Noncompliant@+1 {{Make sure that omitting to log is safe here.}}
resource "azurerm_storage_account" "s6258_sa_nc8" {
  account_kind = "AnythingElse"
}


resource "azurerm_storage_account" "s6258_sa_c1" {
  account_kind = "AnythingElse"
  queue_properties {
    logging {
      delete = true
      read   = true
      write  = true
    }
  }
}

resource "azurerm_storage_account" "s6258_sa_c2" {
  account_kind = "BlobStorage"
}

resource "non_azurerm_storage_account" "s6258_sa_cov" {
  account_kind = "AnythingElse"
}
