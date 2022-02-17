data "google_iam_policy" "noncompliant_policy1" {
  binding {
    role    = "roles/ml.jobOwner" # Compliant nobody references this
    members = []
  }
}

data "google_iam_policy" "noncompliant_policy2" {
  binding {
    role    = "roles/autoscaling.sitesAdmin" # Compliant nobody references this
    members = []
  }
}

data "google_iam_policy" "noncompliant_policy3" {
  binding {
    role = "roles/fleetengine.serviceSuperUser" # NonCompliant
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    members = []
  }

  binding {
    role = "roles/fleetengine.serviceSuperMario" # Compliant
    members = []
  }
}

resource "google_api_gateway_api_config_iam_policy" "ref1" {
  policy_data = data.google_iam_policy.noncompliant_policy3.policy_data
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{The policy is used here.}}
}

resource "google_iap_web_iam_policy" "ref2" {
  policy_data = data.google_iam_policy.noncompliant_policy3.policy_data
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{The policy is used here.}}
}


data "google_iam_policy" "compliant_policy1" {

  binding {
    role = "something_safe" # Compliant
    members = []
  }
}

data "google_iam_policy" "compliant_policy2" {

  binding {
    role = "Admin+Owner" # Compliant because it's not referenced
    members = []
  }
}

data "non_google_iam_policy" "compliant_policy3" {

  binding {
    role = "Admin+Owner" # Compliant
    members = []
  }
}


### resources ###

resource "google_iap_web_iam_policy" "compliant_ref3" {
  policy_data = data.google_iam_policy.compliant_policy1.policy_data
}

resource "google_iap_web_iam_policy" "compliant_ref4" {
  policy_data = data.google_iam_policy.compliant_policy3.policy_data
}
