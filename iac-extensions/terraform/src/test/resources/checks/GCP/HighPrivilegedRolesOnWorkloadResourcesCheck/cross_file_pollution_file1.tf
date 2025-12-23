# This file defines a policy with a safe role; no issue here














# Define everything later than the last line number in the second file

# The policy below has the same name as the one in file2
data "google_iam_policy" "shared_policy_name" {
  binding {
    role = "roles/viewer" # Compliant - safe role
    members = ["user:safe@example.com"]
  }
}

# This resource references the policy defined above
resource "google_storage_bucket_iam_policy" "file1_reference" {
  bucket      = "my-bucket-1"
  policy_data = data.google_iam_policy.shared_policy_name.policy_data
}
