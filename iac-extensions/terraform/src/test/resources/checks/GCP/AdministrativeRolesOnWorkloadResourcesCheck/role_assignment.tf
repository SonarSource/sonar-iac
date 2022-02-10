resource "google_apigee_environment_iam_binding" "binding" {
  org_id = google_apigee_environment.apigee_environment.org_id
  env_id = google_apigee_environment.apigee_environment.name
  role  = "roles/ml.jobOwner" # Noncompliant {{Make sure it is safe to give those members full access to the resource.}}
  members = [
    "user:jane@example.com",
  ]
}

resource "google_tags_tag_value_iam_binding" "binding2" {
  role  = "roles/ml.SuperUser.v2" # Noncompliant
}

resource "google_apigee_environment_iam_member" "member" {
  role = "roles/apigee.admin" # Noncompliant {{Make sure it is safe to grant that member full access to the resource.}}
}

resource "google_apigee_environment_iam_member" "member2" {
  role = "roles/apigee.AdMiN" # Noncompliant
}

resource "google_tags_tag_value_iam_member" "member3" {
  role = "roles/tags.manager.v3" # Noncompliant
}


# Compliant:
resource "google_apigee_environment_iam_binding" "binding3" {
  role  = "roles/ml.dev" # Compliant
}

resource "google_apigee_environment_iam_member" "member4" {
  role = "roles/apigee.group" # Compliant
}

resource "google_tags_tag_value_iam_binding_xyz" "binding4" {
  role  = "roles/ml.SuperUser.v2"
}

resource "google_tags_tag_value_iam_member_xyz" "member5" {
  role = "roles/tags.manager.v3"
}

resource "google_tags_tag_value_iam_member" "member6" {
  not_a_role = "roles/tags.ADMIN.v3"
}
