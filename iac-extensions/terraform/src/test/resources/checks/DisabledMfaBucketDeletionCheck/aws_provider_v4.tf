resource "aws_s3_bucket" "no_versioning" {
  bucket = "mycompliantbuckets6255"
}

resource "aws_s3_bucket" "mynoncompliantbuckets6255" {
  bucket = "mynoncompliantbuckets6255"

  versioning {}
}

resource "aws_s3_bucket" "mycompliantbuckets6255" {
  #      ^^^^^^^^^^^^^^^> {{Related bucket}}
  bucket = "mycompliantbuckets6255"

  versioning {
    mfa_delete = false # Noncompliant {{Make sure allowing object deletion without MFA is safe here.}}
#   ^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_s3_bucket" "mycompliantbuckets6255" {
  bucket = "mycompliantbuckets6255"

  versioning {
    enabled = true
    mfa_delete = true
  }
}

resource "aws_s3_bucket_versioning" "non_compliant_versioning_disabled" {
  versioning_configuration {
    mfa_delete = "Disabled" # Noncompliant {{Make sure allowing object deletion without MFA is safe here.}}
#   ^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_s3_bucket_versioning" "non_compliant_versioning_missing" {
  versioning_configuration { # Noncompliant {{Make sure allowing object deletion without MFA is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_s3_bucket_versioning" "compliant_versioning" {
  versioning_configuration {
    mfa_delete = "Enabled"
  }
}
