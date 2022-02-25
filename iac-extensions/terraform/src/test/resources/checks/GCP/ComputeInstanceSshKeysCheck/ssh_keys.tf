resource "google_compute_instance" "example1" { # Noncompliant {{Omitting metadata.block-project-ssh-keys enables project-wide SSH keys. Make sure it is safe here.}}
#        ^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_compute_instance" "example2" {
  metadata = true # Noncompliant {{metadata of type Object expected.}}
# ^^^^^^^^^^^^^^^
}

resource "google_compute_instance" "example3" {
  metadata = { ssh-keys = true } # Noncompliant {{Omitting metadata.block-project-ssh-keys enables project-wide SSH keys. Make sure it is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_compute_instance" "example4" {
  metadata = {
    block-project-ssh-keys = false # Noncompliant {{Make sure that enabling project-wide SSH keys is safe here.}}
#                            ^^^^^
  }
}

resource "google_compute_instance" "example4" {
  metadata = {
    block-project-ssh-keys = 1 # Noncompliant {{Make sure that enabling project-wide SSH keys is safe here.}}
#                            ^
  }
}

resource "google_compute_instance" "example5" {
  metadata = {
    block-project-ssh-keys = true # Compliant
  }
}

####### various-stages-of-missing metadata.block-project-ssh-keys

resource "google_compute_instance" "various-stages-of-missing-1" { # Noncompliant {{Omitting metadata.block-project-ssh-keys enables project-wide SSH keys. Make sure it is safe here.}}
#        ^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_compute_instance" "various-stages-of-missing-2" {
  metadata = true # Noncompliant {{metadata of type Object expected.}}
# ^^^^^^^^^^^^^^^
}

resource "google_compute_instance" "various-stages-of-missing-3" {
  metadata = {} # Noncompliant {{Omitting metadata.block-project-ssh-keys enables project-wide SSH keys. Make sure it is safe here.}}
# ^^^^^^^^^^^^^
}

resource "google_compute_instance" "various-stages-of-missing-4" {
  metadata = { block-project-ssh-keys = 1 } # Noncompliant {{Make sure that enabling project-wide SSH keys is safe here.}}
#                                       ^
}

resource "google_compute_instance" "various-stages-of-missing-5" {
  metadata = { block-project-ssh-keys = false } # Noncompliant {{Make sure that enabling project-wide SSH keys is safe here.}}
#                                       ^^^^^
}

resource "google_compute_instance" "various-stages-of-missing-6" {
  metadata = { block-project-ssh-keys = true } # Compliant
}
