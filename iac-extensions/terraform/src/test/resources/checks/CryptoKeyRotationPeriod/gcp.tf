resource "google_kms_crypto_key" "k1" {
  name            = "k1"
  key_ring        = google_kms_key_ring.keyring.id
  rotation_period = "100000s"
}

resource "google_kms_crypto_key" "k2" { # Noncompliant
  name            = "k2"
  key_ring        = google_kms_key_ring.keyring.id
}

resource "unrelated_resource_type" "k3" {
  name            = "k3"
  key_ring        = google_kms_key_ring.keyring.id
}
