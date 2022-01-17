# Noncompliant@+1 {{Omitting transit_encryption_enabled enables clear-text traffic. Make sure it is safe here.}}
resource "aws_elasticache_replication_group" "missing_property" {
}

resource "aws_elasticache_replication_group" "sensitive_proprty_value" {
  transit_encryption_enabled = false  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_elasticache_replication_group" "safe_property_value" {
  transit_encryption_enabled = true
}

resource "not_an_aws_msk_cluster" "for_coverage" {
}
