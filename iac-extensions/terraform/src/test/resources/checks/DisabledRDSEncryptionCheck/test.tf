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

resource "aws_rds_cluster" "unencrypted1" {
  engine                  = "aurora-mysql"
  storage_encrypted       = false # Noncompliant {{Make sure that using an unencrypted RDS DB Cluster is safe here.}}
}

resource "aws_rds_cluster" "unencrypted2" {
  engine                  = "aurora-mysql"
  engine_mode             = "provisioned"
  storage_encrypted       = false # Noncompliant
}

resource "aws_rds_cluster" "encrypted1" {
  engine                  = "aurora-mysql"
  storage_encrypted       = true # compliant
}

resource "aws_rds_cluster" "encrypted2" {
  engine                  = "aurora-mysql"
  engine_mode             = "provisioned"
  storage_encrypted       = true # compliant
}

resource "aws_rds_cluster" "missingEncryption1" { # compliant
  engine                  = "aurora-mysql"
  engine_mode             = "serverless"
}

resource "aws_rds_cluster" "missingEncryption2" { # Noncompliant
  engine                  = "aurora-mysql"
}

resource "aws_rds_cluster" "missingEncryption3" { # Noncompliant
  engine                  = "aurora-mysql"
  engine_mode             = "provisioned"
}

resource "aws_db_instance_automated_backups_replication" "withKmsKey" {
  source_db_instance_arn  = aws_db_instance.compliant.arn
  kms_key_id              = aws_kms_key.required.arn # compliant
  provider                = aws.replica
}

resource "aws_db_instance_automated_backups_replication" "missingKmsKey" { # Noncompliant {{Make sure that using an unencrypted DB backup replication is safe here.}}
  source_db_instance_arn  = aws_db_instance.compliant.arn
  provider                = aws.replica
}
