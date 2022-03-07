resource "google_container_cluster" "container-noncompliant" {
  logging_service = "none" # Noncompliant {{Make sure that disabling logging is safe here.}}
}

resource "google_container_cluster" "container-compliant" {
  logging_service = "logging.googleapis.com"
}

resource "non_google_container_cluster" "container-noncompliant" {
  logging_service = var.logging
}

resource "google_container_cluster" "container-compliant" {
}

resource "non_google_container_cluster" "container-noncompliant" {
  logging_service = "none"
}
