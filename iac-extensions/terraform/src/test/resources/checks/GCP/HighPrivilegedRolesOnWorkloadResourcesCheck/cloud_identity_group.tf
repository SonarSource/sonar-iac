resource "google_cloud_identity_group" "group" {
  roles {
    name = "MANAGER" # NonCompliant
#   ^^^^^^^^^^^^^^^^
  }
  roles {
    name = "owner" # NonCompliant
#   ^^^^^^^^^^^^^^
  }
}

resource "google_cloud_identity_group" "compliant1" {
  roles {
    name = "#MANAGER" # Compliant (exact match is required)
  }
  roles {
    name = "owner#" # Compliant (exact match is required)
  }
}

resource "google_cloud_identity_group" "compliant2" {
  role {
    name = "MANAGER" # Compliant (roles block is required)
  }
  role {
    name = "owner" # Compliant (roles block is required)
  }
}

resource "google_cloud_identity_group" "compliant3" {
}
