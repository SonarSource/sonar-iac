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

resource "something_unrelated" "x1" {
  policy_data = data.google_iam_policy.noncompliant.policy_data #Compliant
}

resource "google_api_gateway_api_config_iam_policy" "x2" {
  policy_data = data.google_iam_policy.xxx.policy_data
}

resource "google_api_gateway_api_config_iam_policy" "x3" {
  policy_data = data.google_iam_policy.noncompliant.xxx
}

# data block without name
data "google_iam_policy" {

}

data "non_google_iam_policy" "coverage" {

}

top_level_attribute = "Syntactical correct HCl, but not valid Terraform"

# A check-scoped data source must not shadow the top-level policy with the same name.
check "shadowing_policy" {
  data "google_iam_policy" "shadowed" {
    binding {
      members = ["allUsers"]
    }
  }

  assert {
    condition = length(data.google_iam_policy.shadowed.policy_data) > 0
    error_message = "The policy must not be empty."
  }
}

data "google_iam_policy" "shadowed" {
  binding {
    members = ["user:jane@example.com"]
  }
}

resource "google_api_gateway_api_config_iam_policy" "shadowed_reference" {
  policy_data = data.google_iam_policy.shadowed.policy_data # Compliant: resolves to the top-level policy
}

# Duplicate names must not prevent analysis of the other resources in the file.
data "google_iam_policy" "noncompliant" {
}
