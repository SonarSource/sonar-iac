resource "google_compute_instance" "example" { #Noncompliant
}

resource "google_compute_instance" "example" {
  metadata = {
    block-project-ssh-keys = false # Noncompliant
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "google_compute_instance" "example" {
  metadata = {
    block-project-ssh-keys = true # Compliant
  }
}
