################# NONCOMPLIANT #################

data "google_iam_policy" "s6302-noncompliant0" {
  binding {
    role = "roles/actions.Admin"  # Noncompliant {{Make sure it is safe to give all members full access.}}
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

# Also applies to: google_organization_iam_policy, google_service_account_iam_policy, google_folder_iam_policy
resource "google_project_iam_policy" "s6302-noncompliant1" {
  policy_data = data.google_iam_policy.s6302-noncompliant0.policy_data
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{The policy is used here.}}
}

# Also applies to: google_organization_iam_binding, google_service_account_iam_binding, google_folder_iam_binding
resource "google_project_iam_binding" "s6302-noncompliant2" {
  role    = "roles/actions.Admin"  # Noncompliant {{Make sure it is safe to give all members full access.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Also applies to: google_organization_iam_member, google_service_account_iam_member, google_folder_iam_member
resource "google_project_iam_member" "s6302-noncompliant3" {
  role    = "roles/actions.Admin"  # Noncompliant {{Make sure it is safe to grant that member full access.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_project_iam_member" "s6302-noncompliant4" {
  # Catching also custom roles containing the sensitve roles
  role    = "myOrganisation/my_parent/roles/actions.Superadmin"  # Noncompliant {{Make sure it is safe to grant that member full access.}}
}

resource "google_project_iam_binding" "s6302-noncompliant5" {
  role    = "roles/apigee.apiAdminV2"  # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_project_iam_binding" "s6302-noncompliant6" {
  role    = "roles/compute.instanceAdmin.v1"  # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_project_iam_binding" "s6302-noncompliant7" {
  role    = "roles/fleetengine.serviceSuperUser"  # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_project_iam_binding" "s6302-noncompliant8" {
  role    = "roles/gsuiteaddons.developer"  # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_project_iam_binding" "s6302-noncompliant9" {
  role    = "roles/iam.securityAdmin"  # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_project_iam_binding" "s6302-noncompliant10" {
  role    = "roles/owner"  # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^
}

################# FALSE NEGATIVE #################

# Reference to policy can be done via template invocation which we can not resolve properly

data "google_iam_policy" "s6302-noncompliant1" {
  binding {
    role = "roles/actions.Manager"  # False Negative
  }
}
resource "google_project_iam_policy" "s6302-noncompliant4" {
  policy_data = "${data.google_iam_policy.s6302-noncompliant1.policy_data}"
}

################# COMPLIANT #################

data "google_iam_policy" "s6302-compliant0-unused" {
  binding {
    role = "roles/actions.Admin"  # Compliant because not used
  }
}

data "google_iam_policy" "s6302-compliant0" {
  binding {
    role = "roles/actions.Viewer" # Compliant because used but safe
  }
}

# Also applies to: google_organization_iam_policy, google_service_account_iam_policy, google_folder_iam_policy
resource "google_project_iam_policy" "s6302-compliant1" {
  policy_data = data.google_iam_policy.s6302-compliant0.policy_data
}

resource "google_project_iam_binding" "s6302-compliant2" {
  role    = "roles/browser"
}

resource "google_project_iam_binding" "s6302-compliant3" {
  role    = "roles/licensemanager.viewer"
}

resource "google_project_iam_binding" "s6302-compliant4" {
  role    = "roles/mapsadmin.viewer"
}

resource "google_project_iam_binding" "s6302-compliant5" {
  role    = "roles/privilegedaccessmanager.serviceAgent"
}

resource "google_project_iam_binding" "s6302-compliant6" {
  role    = "roles/resourcemanager.folderEditor"
}

resource "google_project_iam_binding" "s6302-compliant7" {
  role    = "roles/secretmanager.viewer"
}

data "not_google_iam_policy" "coverage1" {
  binding {
    role = "roles/actions.Admin"
  }
}

resource "google_project_iam_policy" "coverage1" {
  policy_data = data.not_google_iam_policy.coverage1.policy_data
}
