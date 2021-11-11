resource "aws_redshift_cluster" "redshift_cluster_disabling_logs" {
  cluster_identifier = "redshift-cluster"
  logging {
    enable = false # Noncompliant
  }
}

resource "aws_redshift_cluster" "redshift_cluster_empty" {
  cluster_identifier = "redshift-cluster"
  logging { # Noncompliant
  }
}


resource "aws_redshift_cluster" "redshift_cluster_missing" { # Noncompliant
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
