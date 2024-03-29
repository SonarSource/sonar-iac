resource "aws_db_instance" "too_low_period" {
  name                 = "non_compliant_db"
  backup_retention_period = 2 # Noncompliant {{Make sure that defining a short backup retention duration is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting "backup_retention_period" results in a short backup retention duration. Make sure it is safe here.}}
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

resource "aws_db_instance" "exception_engine" {
  name                 = "compliant_db"
  engine               = "aurora"
  backup_retention_period = 2
}

resource "aws_db_instance" "not_exception_engine" {
  name                 = "non_compliant_db"
  engine               = "not_aurora"
  backup_retention_period = 2 # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_rds_cluster" "too_low_period" {
  name                 = "non_compliant_db"
  backup_retention_period = 2 # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_rds_cluster" "no_engine_exception_on_cluster" {
  name                 = "non_compliant_db"
  backup_retention_period = 2 # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  engine               = "aurora"
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
