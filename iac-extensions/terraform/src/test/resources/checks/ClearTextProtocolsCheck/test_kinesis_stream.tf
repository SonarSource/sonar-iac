resource "aws_kinesis_stream" "sensitive_stream" {
  encryption_type = "NONE" # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
  #                 ^^^^^^
}

resource "aws_kinesis_stream" "missing_encryption_type" { # Noncompliant
  #      ^^^^^^^^^^^^^^^^^^^^
}

resource "aws_kinesis_stream" "safe_encryption_type" {
  encryption_type = "FOOBAR"
}

resource "not_an_aws_msk_cluster" "for_coverage" {
}
