resource "google_compute_instance_from_template" "from-noncompliant-template-1" {
  source_instance_template = google_compute_instance_template.noncompliant-template.id # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_compute_instance_template" "noncompliant-template" {
  metadata = {
    block-project-ssh-keys = false
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{specified here}}
  }
}

#####

resource "google_compute_instance_template" "compliant-template" {
  metadata = {
    block-project-ssh-keys = true
  }
}

resource "google_compute_instance_from_template" "from-compliant-template-2" {
  source_instance_template = google_compute_instance_template.compliant-template.id # Compliant
}

resource "google_compute_instance_from_template" "from-compliant-template-3" {
  source_instance_template = google_compute_instance_template.compliant-template.id # Compliant

  metadata = {
    block-project-ssh-keys = false # Noncompliant {{Make sure that enabling project-wide SSH keys is safe here.}}
#                            ^^^^^
  }
}

#####

resource "google_compute_instance_from_template" "missing-template-reference" { # Noncompliant {{Missing source_instance_template reference.}}
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  metadata = {
    block-project-ssh-keys = true
  }
}

resource "google_compute_instance_from_template" "referring-to-missing-template-0" {
  source_instance_template = google_compute_instance_template.missing-template.id # Noncompliant {{Reference to missing template.}}
  metadata = {
    block-project-ssh-keys = true
  }
}

resource "google_compute_instance_from_template" "reference-of-unexpected-type-1" {
  source_instance_template = 1 # Noncompliant {{Template reference of unexpected type.}}
  metadata = {
    block-project-ssh-keys = true
  }
}

#######

resource "google_compute_instance_from_template" "referring-to-missing-template-1" { # Noncompliant {{Omitting metadata.block-project-ssh-keys enables project-wide SSH keys. Make sure it is safe here.}}
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  source_instance_template = google_compute_instance_template.missing-template.id # Noncompliant {{Reference to missing template.}}
}

resource "google_compute_instance_from_template" "referring-to-missing-template-2" {
  source_instance_template = google_compute_instance_template.missing-template.id # Noncompliant {{Reference to missing template.}}
  metadata = true # Noncompliant {{metadata of type Object expected.}}
# ^^^^^^^^^^^^^^^
}

resource "google_compute_instance_from_template" "referring-to-missing-template-3" {
  source_instance_template = google_compute_instance_template.missing-template.id # Noncompliant {{Reference to missing template.}}
  metadata = {} # Noncompliant {{Omitting metadata.block-project-ssh-keys enables project-wide SSH keys. Make sure it is safe here.}}
# ^^^^^^^^^^^^^
}

resource "google_compute_instance_from_template" "referring-to-missing-template-4" {
  source_instance_template = google_compute_instance_template.missing-template.id # Noncompliant {{Reference to missing template.}}
  metadata = { block-project-ssh-keys = 1 } # Noncompliant {{Make sure that enabling project-wide SSH keys is safe here.}}
#                                       ^
}

resource "google_compute_instance_from_template" "referring-to-missing-template-5" {
  source_instance_template = google_compute_instance_template.missing-template.id # Noncompliant {{Reference to missing template.}}
  metadata = { block-project-ssh-keys = false } # Noncompliant {{Make sure that enabling project-wide SSH keys is safe here.}}
#                                       ^^^^^
}

resource "google_compute_instance_from_template" "referring-to-missing-template-6" {
  source_instance_template = google_compute_instance_template.missing-template.id # Noncompliant {{Reference to missing template.}}
  metadata = { block-project-ssh-keys = true } # Compliant
}
