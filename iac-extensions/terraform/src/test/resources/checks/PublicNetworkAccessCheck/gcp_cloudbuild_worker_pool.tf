resource "google_cloudbuild_worker_pool" "s6329-cbwp-noncompliant1" {
  worker_config {  # Noncompliant {{Omitting no_external_ip allows network access from the Internet. Make sure it is safe here.}}
# ^^^^^^^^^^^^^
  }
}

resource "google_cloudbuild_worker_pool" "s6329-cbwp-noncompliant2" {
  worker_config {
    no_external_ip = false  # Noncompliant {{Make sure allowing public network access is safe here.}}
  # ^^^^^^^^^^^^^^^^^^^^^^
  }
}

# Noncompliant@+1 {{Omitting worker_config allows network access from the Internet. Make sure it is safe here.}}
resource "google_cloudbuild_worker_pool" "s6329-cbwp-noncompliant3" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_cloudbuild_worker_pool" "s6329-cbwp-compliant2" {
  worker_config {
    no_external_ip = true
  }
}

resource "non_google_cloudbuild_worker_pool" "s6329-cbwp-coverage1" {
  worker_config {
    no_external_ip = false
  }
}

resource "non_google_cloudbuild_worker_pool" "s6329-cbwp-coverage2" {
  worker_config {
  }
}

resource "non_google_cloudbuild_worker_pool" "s6329-cbwp-coverage3" {
}
