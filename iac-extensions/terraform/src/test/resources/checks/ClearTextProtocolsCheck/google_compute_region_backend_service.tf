resource "google_compute_region_backend_service" "k1" {
  name     = "k1"
  protocol = "HTTP"  # Noncompliant {{Using HTTP protocol is insecure. Use HTTPS instead.}}
}

resource "google_compute_region_backend_service" "k2" {
  name     = "k2"
  protocol = "HTTPS"
}

resource "google_compute_region_backend_service" "k3" { # Noncompliant {{Omitting 'protocol' enables 'HTTP' by default. Use HTTPS instead.}}
  name     = "k3"
}

resource "unrelated_resource_type" "k4" {
  name     = "k4"
  protocol = "HTTP"
}
