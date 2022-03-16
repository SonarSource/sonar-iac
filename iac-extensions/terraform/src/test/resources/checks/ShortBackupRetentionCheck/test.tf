resource "aws_db_instance" "too_low_period" {
  name                 = "non_compliant_db"
  backup_retention_period = 2 # Noncompliant {{Make sure that defining a short backup retention duration is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting "backup_retention_period" sets the backup retention period to 1 day. Make sure that defining a short backup retention duration is safe here.}}
resource "aws_db_instance" "missing_period" {
  #      ^^^^^^^^^^^^^^^^^
  name                 = "non_compliant_db"
}

resource "aws_db_instance" "read_replica_db" {
  name                 = "compliant_db"
  source_db_instance_identifier = XXXX.arn
}

resource "aws_db_instance" "compliant_period" {
  name                 = "compliant_db"
  backup_retention_period = 7
}

resource "aws_db_instance" "compliant_var_period" {
  name                 = "compliant_db"
  backup_retention_period = var.period
}

resource "aws_rds_cluster" "too_low_period" {
  name                 = "non_compliant_db"
  backup_retention_period = 2 # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_rds_cluster" "missing_period" { # Noncompliant
  name                 = "non_compliant_db"
}

resource "aws_rds_cluster" "compliant_period" {
  name                 = "compliant_db"
  backup_retention_period = 7
}

resource "aws_rds_cluster" "compliant_var_period" {
  name                 = "compliant_db"
  backup_retention_period = var.period
}


resource "non_relevant_resource" "for_coverage" {
}
