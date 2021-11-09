resource "aws_elasticache_replication_group" "missing_property" {  # Noncompliant
}

resource "aws_elasticache_replication_group" "sensitive_proprty_value" {
  transit_encryption_enabled = false  # Noncompliant
}

resource "aws_elasticache_replication_group" "safe_property_value" {
  transit_encryption_enabled = true
}

resource "not_an_aws_msk_cluster" "for_coverage" {
}
