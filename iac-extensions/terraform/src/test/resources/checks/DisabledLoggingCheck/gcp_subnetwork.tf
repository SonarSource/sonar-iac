# Noncompliant@+1 {{Omitting log_config makes logs incomplete. Make sure it is safe here.}}
resource "google_compute_subnetwork" "subnetwork-noncompliant" {
}

resource "google_compute_subnetwork" "subnetwork-compliant" {
  log_config {

  }
}

resource "google_compute_subnetwork" "subnetwork-compliant" {
  log_config {
    aggregation_interval = "INTERVAL_10_MIN"
  }
}

resource "non_google_compute_subnetwork" "subnetwork-noncompliant" {
}
