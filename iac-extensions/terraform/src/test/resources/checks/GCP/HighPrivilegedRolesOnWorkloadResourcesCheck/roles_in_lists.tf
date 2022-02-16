resource "google_storage_bucket_acl" "rspecS6400_example1" {
  bucket = google_storage_bucket.rspecS6400_example.name1

  role_entity = [
    "READER:group-mygroup",
    "OWNER:serviceAccount:google_service_account.rspecS6400_example.email"  # NonCompliant {{Make sure it is safe to grant full access to the resource.}}
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  ]
}

resource "google_storage_default_object_acl" "rspecS6400_example2" {
  role_entity = [
    "owner:serviceAccount:some.email@google.com"  # NonCompliant {{Make sure it is safe to grant full access to the resource.}}
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  ]
}

resource "google_storage_object_acl" "rspecS6400_example3" {
  role_entity = [
    "oWnEr:"  # NonCompliant {{Make sure it is safe to grant full access to the resource.}}
#   ^^^^^^^^
  ]
}

### Compliant ###

resource "google_storage_bucket_acl" "rspecS6400_example1" {
  bucket = google_storage_bucket.rspecS6400_example.name1

  role_entity = [
    "READER:group-mygroup",
    "OWNER:serviceAccount:${google_service_account.rspecS6400_example.email}"  # Compliant: references are not handled yet
  ]
}

resource "google_storage_bucket_acl" "compliant1" {
  bucket = google_storage_bucket.rspecS6400_example.name1

  role_entity = [
    "READER:group-mygroup",
    "something:OWNER:something.else"  # Compliant
  ]
}

resource "google_storage_default_object_acl" "compliant2" {
  role_entity = [
    "something:something:owner:"  # Compliant
  ]
}

resource "google_storage_object_acl" "compliant3" {
  role_entity = [ ] # Compliant
}

resource "google_storage_object_acl" "compliant4" {
  role_entity = "OWNER:" # Compliant
}

resource "non_google_storage_default_object_acl" "compliant5" {
  role_entity = [
    "OWNERSHIP"  # Compliant
  ]
}
