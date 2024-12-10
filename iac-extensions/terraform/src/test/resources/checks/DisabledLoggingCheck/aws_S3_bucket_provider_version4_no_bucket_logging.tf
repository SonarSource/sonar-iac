# Noncompliant@+1
resource "aws_s3_bucket" "bucket_with_unrelated_acl" {
  bucket = "bucket_with_unrelated_acl_name"
  acl    = "xxx"
}

# Noncompliant@+1
resource "aws_s3_bucket" "mycompliantbuckets6258" {
  bucket = "mycompliantbuckets6258name"

  logging {
      target_bucket = "mycompliantloggingbuckets6258name"
      target_prefix = "log/"
  }
}

# -------
# Noncompliant@+1
resource "aws_s3_bucket" "noncompliant_bucket" {
  bucket = "noncompliant_bucket"
}
