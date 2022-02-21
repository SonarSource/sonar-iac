resource "google_apigee_environment_iam_binding" "noncompliant_iam_binding" {
  members = [
    "allAuthenticatedUsers", # Noncompliant {{Ensure that granting public access to this resource is safe here.}}
  # ^^^^^^^^^^^^^^^^^^^^^^^
    "user:jane@example.com",
  ]
}

resource "nongoogle_apigee_environment_iam_binding" "compliant_iam_binding" {
  members = [
    "allAuthenticatedUsers",
    "user:jane@example.com",
  ]
}

resource "google_apigee_environment_iam_member" "iam_noncompliant_iam_member" {
  member = "allUsers" # Noncompliant {{Ensure that granting public access to this resource is safe here.}}
# ^^^^^^^^^^^^^^^^^^^
}

resource "google_apigee_environment_iam_member" "iam_compliant_iam_member" {
  member = "SuperUsers"
}

resource "non_google_apigee_environment_iam_member" "iam_compliant_iam_member" {
  member = "allUsers"
}
