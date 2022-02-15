resource "google_organization_iam_custom_role" "rspec6406_compliant_threshold" {
  permissions = [ # Noncompliant {{This role grants more than 4 sensitive permissions. Make sure they are all required.}}
    "resourcemanager.projects.create",
    "resourcemanager.projects.delete",
    "run.services.delete",
    "run.services.update",
    "resourcemanager.projects.fooLoginBar",
  ]
}
