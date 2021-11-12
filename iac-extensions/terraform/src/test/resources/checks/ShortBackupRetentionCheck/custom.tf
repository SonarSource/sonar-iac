resource "aws_db_instance" "too_low_period" {
  name                 = "non_compliant_db"
  backup_retention_period = 2
}

resource "aws_db_instance" "missing_period" { # Noncompliant
  #      ^^^^^^^^^^^^^^^^^
  name                 = "non_compliant_db"
}

resource "aws_rds_cluster" "too_low_period" {
  name                 = "non_compliant_db"
  backup_retention_period = 2
}

resource "aws_rds_cluster" "missing_period" { # Noncompliant
  name                 = "non_compliant_db"
}
