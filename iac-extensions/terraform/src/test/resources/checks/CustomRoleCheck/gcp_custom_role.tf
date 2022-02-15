resource "google_organization_iam_custom_role" "s6408_examples1" {
  permissions = [
    "resourcemanager.projects.get",
    "resourcemanager.projects.list",
    "iam.serviceAccounts.getAccessToken", # Noncompliant {{Make sure that using a permission that allows privilege escalation is safe here.}}
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    "iam.serviceAccounts.getOpenIdToken", # Noncompliant
    "iam.serviceAccounts.actAs", # Noncompliant
    "run.services.create", # Noncompliant
    "run.services.delete",
    "run.services.get",
    "run.services.getIamPolicy",
    "run.services.list",
    "run.services.update",
  ]
}

resource "google_project_iam_custom_role" "s6408_examples2" {
  permissions = [
    "resourcemanager.projects.get",
    "resourcemanager.projects.list",
    "iam.serviceAccounts.signJwt", # Noncompliant
    "deploymentmanager.deployments.create", # Noncompliant
    "CLOUDBUILD.BUILDS.CREATE", # Noncompliant
    "run.services.get",
    "run.services.getIamPolicy",
    "run.services.list",
    "run.services.setIamPolicy", # Noncompliant {{Make sure that using a permission that allows privilege escalation is safe here.}}
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  ]
}

resource "non_google_organization_iam_custom_role" "s6408_coverage" {
  permissions = [
    "iam.serviceAccounts.getOpenIdToken"
  ]
}
