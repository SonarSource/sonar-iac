# Noncompliant@+1 {{Omitting encryption_at_host_enabled enables clear-text storage. Make sure it is safe here.}}
resource "azurerm_windows_virtual_machine_scale_set" "noncompliant" {
  # Sensitive (S6388)
}

resource "azurerm_windows_virtual_machine_scale_set" "noncompliant" {
  encryption_at_host_enabled = false  # Noncompliant {{Make sure using unencrypted cloud storage is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "azurerm_windows_virtual_machine_scale_set" "noncompliant" {
  encryption_at_host_enabled = true

# Noncompliant@+1 {{Omitting disk_encryption_set_id enables clear-text storage. Make sure it is safe here.}}
  os_disk {
    not_disk_encryption_set_id = "111"
  }

# Noncompliant@+1 {{Omitting disk_encryption_set_id enables clear-text storage. Make sure it is safe here.}}
  data_disk {
    not_disk_encryption_set_id = "222"
  }
}

resource "azurerm_windows_virtual_machine_scale_set" "compliant" {
  encryption_at_host_enabled = true

  os_disk {
    disk_encryption_set_id = "111"
  }

  data_disk {
    disk_encryption_set_id = "222"
  }
}

resource "azurerm_windows_virtual_machine_scale_set" "compliant" {
  encryption_at_host_enabled = true

  non_os_disk {
  }

  non_data_disk {
  }
}
