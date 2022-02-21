resource "google_storage_bucket_acl" "rspecS6400_example" {
  role_entity = [
    "READER:allUsers", # Noncompliant {{Ensure that granting public access to this resource is safe here.}}
  # ^^^^^^^^^^^^^^^^^
    "READER:allAuthenticatedUsers", # Noncompliant
    "OWNER:allUsers", # Noncompliant
    "READER:adminUsers",
    "READER:serviceAccount:${google_service_account.rspecS6400_example.email}",
    "READER:serviceAccount:${google_service_account.rspecS6400_example.allAuthenticatedUsers.email}",
  ]
}

resource "google_storage_bucket_acl" "rspecS6400_example" {

}

resource "non_google_storage_bucket_acl" "rspecS6400_example" {
  role_entity = [
    "READER:allUsers",
  ]
}
