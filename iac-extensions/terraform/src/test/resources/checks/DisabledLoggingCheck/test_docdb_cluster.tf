resource "aws_docdb_cluster" "sensitive_exports_value" {
  cluster_identifier = "DB Cluster With Logs"
  enabled_cloudwatch_logs_exports = ["profiler"] # Noncompliant {{Make sure that disabling logging is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting enabled_cloudwatch_logs_exports makes logs incomplete. Make sure it is safe here.}}
resource "aws_docdb_cluster" "missing_exports_value" {
  cluster_identifier = "DB Cluster Without Logs"
}

resource "aws_docdb_cluster" "compliant_exports_value" {
  cluster_identifier = "DB Cluster With Logs"
  enabled_cloudwatch_logs_exports = ["audit"]
}

resource "aws_docdb_cluster" "compliant_exports_value" {
  cluster_identifier = "DB Cluster With Logs"
  enabled_cloudwatch_logs_exports = ["profiler", "audit"]
}

resource "aws_docdb_cluster" "compliant_exports_value_by_ref" {
  cluster_identifier = "DB Cluster With Logs"
  enabled_cloudwatch_logs_exports = XXXX.arn
}

resource "non_aws_docdb_cluster" "for_coverage" {
}

