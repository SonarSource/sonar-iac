resource "google_compute_instance" "example1" { # Noncompliant
#        ^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_compute_instance" "example2" {
  metadata = true # Noncompliant {{Object value expected.}}
# ^^^^^^^^^^^^^^^
}

resource "google_compute_instance" "example2" {
  metadata = { # Noncompliant
# ^[el=+2;ec=3]
  }
}

resource "google_compute_instance" "example3" {
  metadata = {
    block-project-ssh-keys = false # Noncompliant {{Make sure that enabling project-wide SSH keys is safe here.}}
#                            ^^^^^
  }
}

resource "google_compute_instance" "example4" {
  metadata = {
    block-project-ssh-keys = true # Compliant
  }
}

### template cases ###

