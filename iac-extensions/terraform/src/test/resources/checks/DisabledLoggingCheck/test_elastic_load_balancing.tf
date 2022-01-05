# Noncompliant@+1 {{Omitting access_logs makes logs incomplete. Make sure it is safe here.}}
resource "aws_lb" "noncompliant_missing" {
}

# Noncompliant@+1 {{Omitting access_logs makes logs incomplete. Make sure it is safe here.}}
resource "aws_elb" "noncompliant_missing" {
}

resource "aws_lb" "noncompliant_disabled" {
  access_logs {
    enabled = false # Noncompliant
  # ^^^^^^^^^^^^^^^
    bucket = "mycompliantbucket"
    bucket_prefix = "log/lb-"
  }
}

resource "aws_elb" "noncompliant_disabled" {
  access_logs {
    enabled = false # Noncompliant
    bucket = "mycompliantbucket"
    bucket_prefix = "log/lb-"
  }
}

resource "aws_lb" "noncompliant_disabled_by_default" {
  access_logs { # Noncompliant {{Make sure that disabling logging is safe here.}}
# ^^^^^^^^^^^
    bucket = "mycompliantbucket"
    bucket_prefix = "log/lb-"
  }
}

resource "aws_elb" "compliant_enabled_by_default" {
  access_logs {
    bucket = "mycompliantbucket"
    bucket_prefix = "log/lb-"
  }
}

resource "non_aws_elb" "for_coverage" {
}


resource "non_aws_lb" "for_coverage" {
}
