resource "google_container_cluster" "s6409-cc-noncompliant1" {
  enable_legacy_abac = true  # Noncompliant {{Make sure that enabling attribute-based access control is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_container_cluster" "s6409-cc-compliant1" {
  enable_legacy_abac = false
}

resource "google_container_cluster" "s6409-cc-compliant2" {

}

resource "other_resource" "coverage" {
  enable_legacy_abac = true
}
