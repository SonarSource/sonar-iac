resource "google_compute_region_backend_service" "k1" {
  name     = "k1"
  protocol = "HTTP"  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
}

resource "google_compute_region_backend_service" "k2" {
  name     = "k2"
  protocol = "HTTPS"
}

resource "google_compute_region_backend_service" "k3" { # Noncompliant {{Omitting "protocol" enables clear-text traffic. Make sure it is safe here.}}
  name     = "k3"
}

resource "unrelated_resource_type" "k4" {
  name     = "k4"
  protocol = "HTTP"
}
