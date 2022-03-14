resource "google_compute_ssl_policy" "tls11-ssl-policy" {
  min_tls_version = "TLS_1_1" # Noncompliant {{Change this code to disable support of older TLS versions.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_compute_ssl_policy" "tls11-ssl-policy" {
  min_tls_version = "TLS_1_0" # Noncompliant
}


resource "google_compute_ssl_policy" "tls12-ssl-policy" {
  min_tls_version = "TLS_1_2"
}

resource "google_compute_ssl_policy" "tls11-ssl-policy" {
  min_tls_version = var.ssl.tls-version
}

# Noncompliant@+1 {{Set "min_tls_version" to disable support of older TLS versions.}}
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
