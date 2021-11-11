resource "aws_mq_broker" "noncompliant_missing" {  # Noncompliant {{Make sure that disabling logging is safe here.}}
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
