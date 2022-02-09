resource "google_apigee_environment_iam_binding" "binding" {
  org_id = google_apigee_environment.apigee_environment.org_id
  env_id = google_apigee_environment.apigee_environment.name
  role  = "roles/ml.jobOwner" # NonCompliant
  members = [
    "user:jane@example.com",
  ]
}

resource "google_apigee_environment_iam_member" "member" {
  org_id = google_apigee_environment.apigee_environment.org_id
  env_id = google_apigee_environment.apigee_environment.name
  role = "roles/apigee.admin" # NonCompliant
  member = "user:jane@example.com"
}

resource "google_tags_tag_value_iam_binding" "binding2" {
  org_id = google_apigee_environment.apigee_environment.org_id
  env_id = google_apigee_environment.apigee_environment.name
  role  = "roles/ml.SuperUser.v2" # NonCompliant
  members = [
    "user:jane@example.com",
  ]
}

resource "google_tags_tag_value_iam_member" "member2" {
  org_id = google_apigee_environment.apigee_environment.org_id
  env_id = google_apigee_environment.apigee_environment.name
  role = "roles/tags.manager.v3" # NonCompliant
  member = "user:jane@example.com"
}


# Compliant:
resource "google_apigee_environment_iam_binding" "binding3" {
  org_id = google_apigee_environment.apigee_environment.org_id
  env_id = google_apigee_environment.apigee_environment.name
  role  = "roles/ml.dev" # Compliant
}

resource "google_apigee_environment_iam_member" "member3" {
  org_id = google_apigee_environment.apigee_environment.org_id
  env_id = google_apigee_environment.apigee_environment.name
  role = "roles/apigee.group" # Compliant
  member = "user:jane@example.com"
}

resource "google_tags_tag_value_iam_binding_xyz" "binding4" {
  org_id = google_apigee_environment.apigee_environment.org_id
  env_id = google_apigee_environment.apigee_environment.name
  role  = "roles/ml.SuperUser.v2"
  members = [
    "user:jane@example.com",
  ]
}

resource "google_tags_tag_value_iam_member_xyz" "member4" {
  org_id = google_apigee_environment.apigee_environment.org_id
  env_id = google_apigee_environment.apigee_environment.name
  role = "roles/tags.manager.v3"
  member = "user:jane@example.com"
}
