resource "google_project_iam_audit_config" "noncompliant" {
  audit_log_config {
    exempted_members = [ # Noncompliant {{Make sure excluding members activity from audit logs is safe here.}}
  # ^^^^^^^^^^^^^^^^
      "user:eric.therond@sonarsource.com",
    ]
  }
}

resource "google_project_iam_audit_config" "compliant" {
  audit_log_config {
    exempted_members = [] # Compliant empty list
  }

  audit_log_config { # Compliant missing equals empty
  }

  audit_log_config {
    exempted_members = var.audit_members
  }

  audit_log_config {
    exempted_members = "user:eric.therond@sonarsource.com" # Invalid semantic
  }
}

resource "non_google_project_iam_audit_config" "coverage" {
  audit_log_config {
    exempted_members = [
      "user:eric.therond@sonarsource.com",
    ]
  }
}
