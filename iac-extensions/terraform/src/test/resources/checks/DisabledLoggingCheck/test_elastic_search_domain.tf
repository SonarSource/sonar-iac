resource "aws_elasticsearch_domain" "noncompliant_missing_options" { # Noncompliant
  domain_name = "sensitive_domain"
}

resource "aws_elasticsearch_domain" "noncompliant_enabled_false" {
  domain_name = "sensitive_domain"
  log_publishing_options {
    cloudwatch_log_group_arn = aws_cloudwatch_log_group.example.arn
    log_type                 = "AUDIT_LOGS"
    enabled                  = false # Noncompliant
                            #  ^^^^^
  }
}

# Noncompliant@+1 {{Omitting log_publishing_options of type "AUDIT_LOGS" makes logs incomplete. Make sure it is safe here.}}
resource "aws_elasticsearch_domain" "noncompliant_missing_audit_log" {
      #  ^^^^^^^^^^^^^^^^^^^^^^^^^^
  domain_name = "sensitive_domain"
  log_publishing_options {
    cloudwatch_log_group_arn = aws_cloudwatch_log_group.example.arn
    log_type                 = "ES_APPLICATION_LOGS"
    enabled                  = true
  }
}

resource "aws_elasticsearch_domain" "noncompliant_enabled_false2" {
  domain_name = "sensitive_domain"
  log_publishing_options {
    cloudwatch_log_group_arn = aws_cloudwatch_log_group.example.arn
    log_type                 = "ES_APPLICATION_LOGS"
    enabled                  = true
  }

  log_publishing_options {
    cloudwatch_log_group_arn = aws_cloudwatch_log_group.example.arn
    log_type                 = "AUDIT_LOGS"
    enabled                  = false # Noncompliant
  }
}

resource "aws_elasticsearch_domain" "compliant_enabled_true2" {
  domain_name = "sensitive_domain"
  log_publishing_options {
    cloudwatch_log_group_arn = aws_cloudwatch_log_group.example.arn
    log_type                 = "ES_APPLICATION_LOGS"
    enabled                  = true
  }

  log_publishing_options {
    cloudwatch_log_group_arn = aws_cloudwatch_log_group.example.arn
    log_type                 = "AUDIT_LOGS"
    enabled                  = true
  }
}

resource "aws_elasticsearch_domain" "compliant_enabled_true_by_default" {
  domain_name = "sensitive_domain"

  log_publishing_options {
    cloudwatch_log_group_arn = aws_cloudwatch_log_group.example.arn
    log_type                 = "AUDIT_LOGS"
  }
}

resource "aws_elasticsearch_domain" "compliant_unknown_log_type" {
  domain_name = "sensitive_domain"

  log_publishing_options {
    cloudwatch_log_group_arn = aws_cloudwatch_log_group.example.arn
    log_type                 = var.LOG_TYPE
  }
}

resource "non_aws_elasticsearch_domain" "for_coverage" {
}
