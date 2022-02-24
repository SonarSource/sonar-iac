resource "google_compute_instance_from_template" "from-template-1" {
  source_instance_template = google_compute_instance_template.noncompliant-template.id # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_compute_instance_template" "noncompliant-template" {
  metadata = {
    block-project-ssh-keys = false
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{specified here}}
  }
}

resource "google_compute_instance_from_template" "from-template-2" {
  source_instance_template = google_compute_instance_template.compliant-template.id
}

resource "google_compute_instance_template" "compliant-template" {
  metadata = {
    block-project-ssh-keys = true
  }
}

resource "google_compute_instance_from_template" "vmfromtpl3" {
  metadata = {
    block-project-ssh-keys = true
  }
}
