
resource "google_logging_project_bucket_config" "compliant-project-level-bucket" {
  retention_days = 7
}

resource "google_logging_project_bucket_config" "noncompliant-project-level-bucket" {
  retention_days = 6 # Noncompliant
}
