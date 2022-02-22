resource "google_compute_ssl_policy" "tls11-ssl-policy" {
  min_tls_version = "TLS_1_1" # Noncompliant {{Change this configuration to use a stronger protocol.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_compute_ssl_policy" "tls12-ssl-policy" {
  min_tls_version = "TLS_1_2"
}

resource "google_compute_ssl_policy" "tls11-ssl-policy" {
  min_tls_version = var.ssl.tls-version
}

# Noncompliant@+1 {{Omitting min_tls_version disables traffic encryption. Make sure it is safe here.}}
resource "google_compute_ssl_policy" "default-tls-version-ssl-policy" {
      #  ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_compute_ssl_policy" "default-tls-version-restricted-profile-ssl-policy" { # Noncompliant
  profile          = "MODERN"
}

resource "google_compute_ssl_policy" "default-tls-version-restricted-profile-ssl-policy" {
  profile          = "RESTRICTED"
}

resource "google_compute_ssl_policy" "default-tls-version-restricted-profile-ssl-policy" {
  profile          = var.ssl.pofile
}
