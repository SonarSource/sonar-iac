resource "aws_redshift_cluster" "redshift_cluster_disabling_logs" {
  cluster_identifier = "redshift-cluster"
  logging {
    enable = false # Noncompliant {{Make sure that disabling logging is safe here.}}
  }
}

resource "aws_redshift_cluster" "redshift_cluster_empty" {
  cluster_identifier = "redshift-cluster"
  logging { # Noncompliant {{Make sure that disabling logging is safe here.}}
# ^^^^^^^
  }
}

# Noncompliant@+1 {{Omitting logging.enable makes logs incomplete. Make sure it is safe here.}}
resource "aws_redshift_cluster" "redshift_cluster_missing" {
  cluster_identifier = "redshift-cluster"
}

resource "aws_redshift_cluster" "redshift_cluster_compliant" {
  cluster_identifier = "redshift-cluster"
  logging {
    enable = true
  }
}

resource "non_aws_redshift_cluster" "for_coverage" {
}
