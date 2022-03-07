resource "google_compute_region_backend_service" "backend-noncompliant" { # Noncompliant {{Omitting log_config makes logs incomplete. Make sure it is safe here.}}
}

resource "google_compute_region_backend_service" "backend-noncompliant" {
  log_config {
    enable = false # Noncompliant {{Make sure that disabling logging is safe here.}}
  }
}

resource "google_compute_region_backend_service" "backend-compliant" {
  log_config {
    enable = true
  }
}

resource "non_google_compute_region_backend_service" "coverage" {

}

resource "google_compute_region_backend_service" "coverage" {
  log_config { # Invalid semantic due to missing `enable` attribute
  }
}
