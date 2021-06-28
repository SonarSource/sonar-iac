resource "aws_s3_bucket" "mycompliantloggingbuckets6258" {
  bucket = "mycompliantloggingbuckets6258name"
  acl    = "log-delivery-write"
}

resource "aws_s3_bucket" "bucket_with_unrelated_acl" { # Noncompliant
  bucket = "bucket_with_unrelated_acl_name"
  acl    = "xxx"
}

resource "aws_s3_bucket" "bucket_with_unrelated_acl" {
  bucket = "bucket_with_unrelated_acl_name"
  acl    = var.acl
}

resource "aws_s3_bucket" "mycompliantbuckets6258" {
  bucket = "mycompliantbuckets6258name"

  logging {
      target_bucket = "mycompliantloggingbuckets6258name"
      target_prefix = "log/"
  }
}
resource "aws_s3_bucket" "mynoncompliantbuckets6258" { # Noncompliant
#        ^^^^^^^^^^^^^^^
  bucket = "mynoncompliantbuckets6258name"
}

resource "non_s3_bucket" "non_s3_bucket1" {
  bucket = "non_s3_bucket1_name"
}
