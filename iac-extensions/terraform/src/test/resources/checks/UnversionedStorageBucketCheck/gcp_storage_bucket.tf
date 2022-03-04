# Noncompliant@+1 {{Omitting versioning will disable versioning for GCS bucket. Ensure it is safe here.}}
resource "google_storage_bucket" "noncompliant1" {
      #  ^^^^^^^^^^^^^^^^^^^^^^^

  # no check required if `versioning.enabled` is missing due to it's a required attribute in `versioning
}

resource "google_storage_bucket" "noncompliant2" {

  versioning {
    enabled = "false" # Noncompliant {{Make sure using an unversioned GCS bucket is safe here.}}
  # ^^^^^^^^^^^^^^^^^
  }
}

resource "google_storage_bucket" "compliant1" {
  versioning {
    enabled = "true"
  }
}

resource "google_storage_bucket" "compliant2" {

  versioning {
    enabled = "false"
  }

  retention_policy {
    retention_period = "3600"
  }
}

resource "google_storage_bucket" "compliant3" {
  versioning {
    enabled = var.gcs_versioning
  }
}

resource "non_google_storage_bucket" "coverage" {
}
