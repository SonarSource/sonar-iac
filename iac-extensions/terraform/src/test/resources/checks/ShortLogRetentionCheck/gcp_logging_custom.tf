# Noncompliant@+1 {{Make sure that defining a short log retention duration is safe here.}}
resource "google_logging_project_bucket_config" "noncompliant-project-level-bucket" {
}

resource "google_logging_project_bucket_config" "compliant-project-level-bucket" {
  retention_days = 300
}

resource "google_logging_project_bucket_config" "noncompliant-project-level-bucket" {
  retention_days = 14 # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
}

resource "google_logging_project_bucket_config" "noncompliant-project-level-bucket" {
  retention_days = 0 # Noncompliant
}
