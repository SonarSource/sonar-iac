resource "google_compute_ssl_policy" "compatible-ssl-policy" {
  name            = "${var.name}-compatible-ssl-policy"
  profile         = "COMPATIBLE" # Noncompliant {{Change this code to disable support of weak cipher suites.}}
}

resource "google_compute_ssl_policy" "modern-ssl-policy" {
  name            = "${var.name}-modern-ssl-policy"
  profile         = "MODERN" # Noncompliant
}

resource "google_compute_ssl_policy" "restricted-ssl-policy" {
  name            = "${var.name}-restricted-ssl-policy"
  profile         = "RESTRICTED" # Compliant
}

resource "google_compute_ssl_policy" "default-profile-ssl-policy" { # Noncompliant {{Set profile to disable support of weak cipher suites.}}
  name            = "${var.name}-default-profile-ssl-policy"
}

resource "unrelated-type" "abc" {
  name            = "${var.name}-default-profile-ssl-policy"
}
