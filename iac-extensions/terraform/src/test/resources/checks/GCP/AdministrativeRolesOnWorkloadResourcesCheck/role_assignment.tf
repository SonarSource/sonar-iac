resource "google_apigee_environment_iam_binding" "binding" {
  org_id = google_apigee_environment.apigee_environment.org_id
  env_id = google_apigee_environment.apigee_environment.name
  role  = "roles/ml.jobOwner" # Noncompliant
  members = [
    "user:jane@example.com",
  ]
}

resource "google_apigee_environment_iam_member" "member" {
  role = "roles/apigee.admin" # Noncompliant
}

resource "google_tags_tag_value_iam_binding" "binding2" {
  role  = "roles/ml.SuperUser.v2" # Noncompliant
}

resource "google_tags_tag_value_iam_member" "member2" {
  role = "roles/tags.manager.v3" # Noncompliant
}


# Compliant:
resource "google_apigee_environment_iam_binding" "binding3" {
  role  = "roles/ml.dev" # Compliant
}

resource "google_apigee_environment_iam_member" "member3" {
  role = "roles/apigee.group" # Compliant
}

resource "google_tags_tag_value_iam_binding_xyz" "binding4" {
  role  = "roles/ml.SuperUser.v2"
}

resource "google_tags_tag_value_iam_member_xyz" "member4" {
  role = "roles/tags.manager.v3"
}
