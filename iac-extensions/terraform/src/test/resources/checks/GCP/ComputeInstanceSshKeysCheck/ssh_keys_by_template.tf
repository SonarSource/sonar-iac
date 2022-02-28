resource "google_compute_instance_from_template" "compliant1" { # Compliant
}

resource "google_compute_instance_from_template" "compliant2" {
  metadata = {
    block-project-ssh-keys = true # Compliant
  }
}

resource "google_compute_instance_from_template" "compliant3" {
  metadata = {
    block-project-ssh-keys = "True" # Compliant
  }
}

resource "google_compute_instance_from_template" "noncompliant1" {
  metadata = {
    block-project-ssh-keys = false # Noncompliant {{Make sure that enabling project-wide SSH keys is safe here.}}
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "google_compute_instance_from_template" "noncompliant2" {
  metadata = {
    block-project-ssh-keys = "False" # Noncompliant {{Make sure that enabling project-wide SSH keys is safe here.}}
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}
