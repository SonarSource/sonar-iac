resource "google_apigee_environment_iam_binding" "binding" {
  org_id = google_apigee_environment.apigee_environment.org_id
  env_id = google_apigee_environment.apigee_environment.name
  role  = "roles/ml.jobOwner" # Noncompliant {{Make sure it is safe to give those members full access to the resource.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  members = [
    "user:jane@example.com",
  ]
}

resource "google_tags_tag_value_iam_binding" "binding2" {
  role  = "roles/ml.SuperUser.v2" # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_apigee_environment_iam_member" "non_compliant_member1" {
  role = "roles/ApiGee.AdMin" # Noncompliant {{Make sure it is safe to grant that member full access to the resource.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_tags_tag_value_iam_member" "non_compliant_member2" {
  role = "roles/tags.manager.v3" # Noncompliant
}

resource "google_secret_manager_secret_iam_binding" "non_compliant_member3" {
  role    = "roles/apigee.apiAdminV2"  # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_secret_manager_secret_iam_binding" "non_compliant_member4" {
  role    = "roles/compute.instanceAdmin.v1"  # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_secret_manager_secret_iam_binding" "non_compliant_member5" {
  role    = "roles/fleetengine.serviceSuperUser"  # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_secret_manager_secret_iam_binding" "non_compliant_member6" {
  role    = "roles/gsuiteaddons.developer"  # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_secret_manager_secret_iam_binding" "non_compliant_member7" {
  role    = "roles/iam.securityAdmin"  # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_secret_manager_secret_iam_binding" "non_compliant_member8" {
  role    = "roles/owner"  # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^
}


# Compliant:
resource "google_apigee_environment_iam_binding" "binding3" {
  role  = "roles/ml.dev" # Compliant
}

resource "google_apigee_environment_iam_member" "compliant_member1" {
  role = "roles/apigee.group" # Compliant
}

resource "google_tags_tag_value_iam_binding_xyz" "binding4" {
  role  = "roles/ml.SuperUser.v2"
}

resource "google_tags_tag_value_iam_member_xyz" "compliant_member1" {
  role = "roles/tags.manager.v3"
}

resource "google_tags_tag_value_iam_member" "compliant_member2" {
  not_a_role = "roles/tags.ADMIN.v3"
}

resource "google_secret_manager_secret_iam_binding" "compliant_member3" {
  role    = "roles/browser"
}

resource "google_secret_manager_secret_iam_binding" "compliant_member4" {
  role    = "roles/licensemanager.viewer"
}

resource "google_secret_manager_secret_iam_binding" "compliant_member5" {
  role    = "roles/mapsadmin.viewer"
}

resource "google_secret_manager_secret_iam_binding" "compliant_member6" {
  role    = "roles/privilegedaccessmanager.serviceAgent"
}

resource "google_secret_manager_secret_iam_binding" "compliant_member7" {
  role    = "roles/resourcemanager.folderEditor"
}

resource "google_secret_manager_secret_iam_binding" "compliant_member8" {
  role    = "roles/secretmanager.viewer"
}

resource "google_secret_manager_secret_iam_binding" "compliant_member9" {
  role    = "MaNaGeR.or.something"
}
