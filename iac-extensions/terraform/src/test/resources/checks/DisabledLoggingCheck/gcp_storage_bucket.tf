# Noncompliant@+1 {{Omitting logging makes logs incomplete. Make sure it is safe here.}}
resource "google_storage_bucket" "bucket-noncompliant" {
}

resource "google_storage_bucket" "bucket-compliant" {
  logging {
    log_bucket = google_storage_bucket.bucket-log.name
  }
}

resource "google_storage_bucket" "bucket-compliant" {
  logging {
  }
}

resource "non_google_storage_bucket" "bucket-noncompliant" {
}
