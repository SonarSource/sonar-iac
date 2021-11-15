# Noncompliant@+1 {{Omitting logs.audit or logs.general makes logs incomplete. Make sure it is safe here.}}
resource "aws_mq_broker" "noncompliant_missing" {
  #      ^^^^^^^^^^^^^^^
}

resource "aws_mq_broker" "noncompliant_disabled" {
  logs {  # Noncompliant
# ^^^^
    audit = false
    general = false
  }
}

resource "aws_mq_broker" "noncompliant_no_logs" {
  logs {  # Noncompliant
# ^^^^
  }
}

resource "aws_mq_broker" "compliant_audit_enabled" {
  logs {
    audit = true
  }
}

resource "aws_mq_broker" "compliant_audit_enabled" {
  logs {
    general = true
  }
}

resource "aws_mq_broker" "compliant_mixed_enabled" {
  logs {
    audit = false
    general = true
  }
}

resource "non_aws_mq_broker" "for_coverage" {
}
