resource "google_logging_project_bucket_config" "compliant-project-level-bucket" {
  retention_days = 14
}

resource "google_logging_project_bucket_config" "compliant-project-level-bucket-zero" {
  retention_days = 0 # the default time of 30 days will be used
}

resource "google_logging_project_bucket_config" "compliant-variable" {
  retention_days = var.logging
}

resource "google_logging_project_bucket_config" "compliant-invalid-value" {
  retention_days = "long"
}

resource "google_logging_project_bucket_config" "compliant-project-level-bucket-default" {
}

resource "google_logging_billing_account_bucket_config" "compliant-billing-account-level-bucket" {
  retention_days = 14
}

resource "google_logging_billing_account_bucket_config" "compliant-billing-account-level-bucket-default" {
}

resource "google_logging_organization_bucket_config" "compliant-organization-level-bucket" {
  retention_days = 14
}

resource "google_logging_organization_bucket_config" "compliant-organization-level-bucket-default" {
}

resource "google_logging_folder_bucket_config" "compliant-folder-level-bucket" {
  retention_days = 14
}

resource "google_logging_folder_bucket_config" "compliant-folder-level-bucket-default" {
}

resource "google_logging_project_bucket_config" "noncompliant-project-level-bucket" {
  retention_days = 7 # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
}

resource "google_logging_billing_account_bucket_config" "noncompliant-billing-account-level-bucket" {
  retention_days = 7 # Noncompliant
}

resource "google_logging_organization_bucket_config" "noncompliant-organization-level-bucket" {
  retention_days = 7 # Noncompliant
}

resource "google_logging_folder_bucket_config" "noncompliant-folder-level-bucket" {
  retention_days = 7 # Noncompliant
}
