
resource "google_bigquery_dataset_access" "nc1" {
  role  = "OwNer" # Noncompliant  {{Make sure it is safe to grant that member full access to the resource.}}
# ^^^^^^^^^^^^^^^
}

resource "google_storage_bucket_access_control" "nc2" {
  role = "owner" # Noncompliant
# ^^^^^^^^^^^^^^
}

# Compliant:
resource "google_storage_default_object_access_control" "c1" {
  role  = "roles/OWNER" # Compliant
}

resource "google_storage_object_access_control" "c2" {
  role = "roles/owner" # Compliant
}

resource "google_storage_object_access_control" "c3" {
  not_a_role = "OWNER" # Compliant
}
