# This file defines a  policy but with the same name as file1

data "google_iam_policy" "shared_policy_name" {
  binding {
    role = "roles/owner" # Noncompliant {{Make sure it is safe to give those members full access to the resource.}}
#   ^^^^^^^^^^^^^^^^^^^^
    members = ["user:privileged@example.com"]
  }
}

# This resource references the policy defined in this file
# This should be the only secondary location for the issue above
resource "google_pubsub_topic_iam_policy" "file2_reference" {
  topic       = "my-topic-2"
  policy_data = data.google_iam_policy.shared_policy_name.policy_data
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{The policy is used here.}}
}
