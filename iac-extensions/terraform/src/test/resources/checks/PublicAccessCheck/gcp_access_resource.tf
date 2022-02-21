resource "google_bigquery_dataset_access" "noncompliant" {
  special_group = "allUsers" # Noncompliant {{Ensure that granting public access to this resource is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_bigquery_dataset_access" "noncompliant" {
  special_group = "allAuthenticatedUsers" # Noncompliant
}

resource "google_storage_default_object_access_control" "noncompliant" {
  entity = "allUsers" # Noncompliant
}

resource "google_storage_object_access_control" "noncompliant" {
  entity = "allUsers" # Noncompliant
}


resource "google_bigquery_dataset_access" "compliant" {
  special_group = "otherUser"
}


resource "google_storage_object_access_control" "compliant" {
  entity = "otherUser"
}

resource "non_google_bigquery_dataset_access" "coverage" {
  special_group = "allUsers"
}
