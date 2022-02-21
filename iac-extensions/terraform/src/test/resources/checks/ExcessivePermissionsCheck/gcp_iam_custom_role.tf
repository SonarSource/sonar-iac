resource "google_organization_iam_custom_role" "rspec6406_sensitive" {
  # Noncompliant@+1 {{This role grants more than 5 sensitive permissions. Make sure they are all required.}}
  permissions = [
# ^^^^^^^^^^^
    "resourcemanager.projects.create",
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{Sensitive permission}}
    "resourcemanager.projects.delete",
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{Sensitive permission}}
    "run.services.delete",
  # ^^^^^^^^^^^^^^^^^^^^^< {{Sensitive permission}}
    "run.services.update",
  # ^^^^^^^^^^^^^^^^^^^^^< {{Sensitive permission}}
    "resourcemanager.projects.fooLoginBar",
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{Sensitive permission}}
    "iam.serviceAccountKeys.create",
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{Sensitive permission}}
  ]
}

resource "google_organization_iam_custom_role" "rspec6406_compliant_readOnly" {
  permissions = [
    "resourcemanager.projects.create",
    "resourcemanager.projects.delete",
    "run.services.delete",
    "run.services.update",
    "resourcemanager.projects.fooLoginBar",
    "iam.serviceAccountKeys.createReadOnly", # Containts ReadOnly
  ]
}

resource "google_organization_iam_custom_role" "rspec6406_sensitive" {
  permissions = [ # Noncompliant
    "resourcemanager.projects.create",
    "resourcemanager.projects.delete",
    "run.services.delete",
    "run.services.update",
    "resourcemanager.projects.fooLoginBar",
    "",
    ".",
    "${foo.bar}",
    "iam.serviceAccountKeys.create",
  ]
}

resource "google_organization_iam_custom_role" "rspec6406_compliant_threshold" {
  permissions = [
    "resourcemanager.projects.create",
    "resourcemanager.projects.delete",
    "run.services.delete",
    "run.services.update",
    "resourcemanager.projects.fooLoginBar",
  ]
}

resource "google_organization_iam_custom_role" "rspec6406_compliant"  {
  permissions = [
    "resourcemanager.projects.get",
    "resourcemanager.projects.list",
    "run.services.get",
    "run.services.getIamPolicy",
    "run.services.list",
    "run.services.createReadOnly",
    "run.services.setIamPolicy",
    "run.services.updateStatus",
    "run.services.UpdateTag",
    "resourcemanager.projects.fooDestroy",
  ]
}
