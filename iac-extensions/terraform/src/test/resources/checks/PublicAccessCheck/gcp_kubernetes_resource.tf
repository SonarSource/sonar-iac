# Noncompliant@+1 {{Omitting private_cluster_config grants public access to parts of this cluster. Make sure it is safe here.}}
resource "google_container_cluster" "rspecS6404_sensitive_omission_1" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_container_cluster" "rspecS6404_sensitive_omission_2" {
  # Noncompliant@+1 {{Omitting enable_private_nodes and enable_private_endpoint grants public access to parts of this cluster. Make sure it is safe here.}}
  private_cluster_config {
# ^^^^^^^^^^^^^^^^^^^^^^
  }
}


resource "google_container_cluster" "rspecS6404_sensitive_explicit" {
  private_cluster_config {
    enable_private_nodes    = false # Noncompliant {{Ensure that granting public access is safe here.}}
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    enable_private_endpoint = false
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{Ensure that granting public access is safe here.}}
  }
}

resource "google_container_cluster" "rspecS6404_noncompliant_halfexplicit_1" {
  private_cluster_config {
    enable_private_nodes    = false # Noncompliant {{Ensure that granting public access is safe here.}}
    enable_private_endpoint = true
  }
}

resource "google_container_cluster" "rspecS6404_noncompliant_halfexplicit_2" {
  # Noncompliant@+1 {{Omitting enable_private_endpoint grants public access to parts of this cluster. Make sure it is safe here.}}
  private_cluster_config {
    enable_private_nodes    = false # Noncompliant {{Ensure that granting public access is safe here.}}
  }
}

resource "google_container_cluster" "rspecS6404_noncompliant_halfexplicit_3" {
  private_cluster_config {
    enable_private_nodes    = true
    enable_private_endpoint = false # Noncompliant {{Ensure that granting public access is safe here.}}
  }
}

resource "google_container_cluster" "rspecS6404_noncompliant_halfexplicit_4" {
  # Noncompliant@+1 {{Omitting enable_private_nodes grants public access to parts of this cluster. Make sure it is safe here.}}
  private_cluster_config {
    enable_private_endpoint = false # Noncompliant {{Ensure that granting public access is safe here.}}
  }
}

resource "google_container_cluster" "rspecS6404_compliant" {
  private_cluster_config {
    enable_private_nodes    = true
    enable_private_endpoint = true
  }
}
