# Noncompliant@+1 {{Omitting no_public_ip allows network access from the Internet. Make sure it is safe here.}}
resource "google_notebooks_instance" "s6329-ni-noncompliant1" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_notebooks_instance" "s6329-ni-noncompliant2" {
  no_public_ip = false  # Noncompliant {{Make sure allowing public network access is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^
}

resource "google_notebooks_instance" "s6329-ni-compliant1" {
  no_public_ip = true
}

resource "non_google_notebooks_instance" "s6329-ni-coverage" {
  no_public_ip = false
}
