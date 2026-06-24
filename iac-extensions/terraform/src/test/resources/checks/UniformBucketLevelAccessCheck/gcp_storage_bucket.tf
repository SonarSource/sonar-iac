# Noncompliant@+1 {{Omitting "uniform_bucket_level_access" allows object ACLs to bypass IAM. Make sure it is safe here.}}
resource "google_storage_bucket" "noncompliant1" {
      #  ^^^^^^^^^^^^^^^^^^^^^^^
  name     = "example-bucket"
  location = "US"
}

resource "google_storage_bucket" "noncompliant2" {
  name                        = "example-bucket"
  location                    = "US"
  uniform_bucket_level_access = false # Noncompliant {{Make sure enabling object ACLs without enforcing uniform bucket-level access is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_storage_bucket" "compliant1" {
  name                        = "example-bucket"
  location                    = "US"
  uniform_bucket_level_access = true
}

resource "google_storage_bucket" "compliant2" {
  name              = "example-bucket"
  location          = "US"
  bucket_policy_only = true
}

resource "google_storage_bucket" "compliant3" {
  name                        = "example-bucket"
  location                    = "US"
  uniform_bucket_level_access = var.uniform_access
}

resource "non_google_storage_bucket" "coverage" {
}
