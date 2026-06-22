# aws_db_instance with publicly_accessible = true -> Issue
resource "aws_db_instance" "noncompliant" {
  publicly_accessible = true # Noncompliant {{Make sure allowing public network access is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# aws_db_instance with publicly_accessible = false -> No issue
resource "aws_db_instance" "compliant_false" {
  publicly_accessible = false
}

# aws_db_instance without the attribute -> No issue (defaults differ by resource, avoid noise)
resource "aws_db_instance" "compliant_absent" {
  engine         = "postgres"
  instance_class = "db.t3.micro"
}

# Interpolated / unresolvable value -> No issue
resource "aws_db_instance" "compliant_unresolved" {
  publicly_accessible = var.is_public
}

# aws_redshift_cluster with publicly_accessible = true -> Issue
resource "aws_redshift_cluster" "noncompliant" {
  publicly_accessible = true # Noncompliant {{Make sure allowing public network access is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# aws_rds_cluster_instance with publicly_accessible = true -> Issue
resource "aws_rds_cluster_instance" "noncompliant" {
  publicly_accessible = true # Noncompliant {{Make sure allowing public network access is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Unrelated resource -> No issue
resource "aws_instance" "other" {
  publicly_accessible = true
}
