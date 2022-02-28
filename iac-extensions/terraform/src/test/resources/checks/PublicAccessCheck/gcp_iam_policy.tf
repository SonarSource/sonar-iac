data "google_iam_policy" "noncompliant" {

  binding {
    members = ["safeUser"]
  }

  binding {
    members = ["allAuthenticatedUsers"]
             # ^^^^^^^^^^^^^^^^^^^^^^^> {{Excessive granting of permissions.}}
  }

  binding {
    members = [
      "superUser",
      "allUsers"
    # ^^^^^^^^^^> {{Excessive granting of permissions.}}
    ]
  }
}

resource "google_api_gateway_api_config_iam_policy" "example" {
  policy_data = data.google_iam_policy.noncompliant.policy_data # Noncompliant {{Ensure that granting public access to this resource is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

data "google_iam_policy" "compliant_unused" {

  binding {
    members = ["allAuthenticatedUsers"]
  }
}

resource "google_api_gateway_api_config_iam_policy" "no_reference" {
  policy_data = data.google_iam_policy.unkown.policy_data
}

resource "google_api_gateway_api_config_iam_policy" "not_resolved" {
  policy_data = "${data.google_iam_policy.compliant_unused.policy_data}"
}

# data block without name
data "google_iam_policy" {

}

data "non_google_iam_policy" "coverage" {

}

top_level_attribute = "Syntactical correct HCl, but not valid Terraform"
