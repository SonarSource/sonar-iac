resource "aws_db_instance" "encrypted" {
  storage_encrypted = true
}

resource "aws_db_instance" "unencrypted1" {
#        ^^^^^^^^^^^^^^^^^>
  storage_encrypted = false # Noncompliant {{Make sure that using unencrypted RDS DB Instances is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting "storage_encrypted" disables databases encryption. Make sure it is safe here.}}
resource "aws_db_instance" "unencrypted2" {
#        ^^^^^^^^^^^^^^^^^
}

resource "no_aws_db_instance" "whatever" {
  storage_encrypted = false
}

resource "aws_db_instance" "default1" {  # Compliant
  engine = "aurora"
  storage_encrypted = false
}

resource "aws_db_instance" "default2" {  # Compliant
  engine = "aurora-mysql"
  storage_encrypted = false
}

resource "aws_db_instance" "default3" {  # Compliant
  engine = "aurora-postgresql"
  storage_encrypted = true
}

resource "aws_db_instance" "default4" {
  engine = "a"
  storage_encrypted = false # Noncompliant {{Make sure that using unencrypted RDS DB Instances is safe here.}}
}

resource "aws_rds_cluster" "example1" {
  engine                  = "aurora-mysql"
  storage_encrypted       = false # Noncompliant
}

resource "aws_rds_cluster" "example2" { # Noncompliant
  engine                  = "aurora-mysql"
}

resource "aws_rds_cluster" "example3" { # compliant
  engine                  = "serverless"
}

resource "aws_rds_cluster" "example3" { # compliant
  engine                  = "serverless"
  storage_encrypted       = true # compliant
}
