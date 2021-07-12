resource "aws_s3_bucket" "no_versioning" { # Noncompliant {{Make sure allowing object deletion of a S3 versioned bucket without MFA is safe here.}}
  #      ^^^^^^^^^^^^^^^
  bucket = "mycompliantbuckets6255"
}

resource "aws_s3_bucket" "mynoncompliantbuckets6255" {
   #     ^^^^^^^^^^^^^^^> {{Related bucket}}
  bucket = "mynoncompliantbuckets6255"

  versioning {} # Noncompliant
# ^^^^^^^^^^ {{Make sure allowing object deletion of a S3 versioned bucket without MFA is safe here.}}
}

resource "aws_s3_bucket" "mycompliantbuckets6255" {
  #      ^^^^^^^^^^^^^^^> {{Related bucket}}
  bucket = "mycompliantbuckets6255"

  versioning {
    mfa_delete = false # Noncompliant {{Make sure allowing object deletion of a S3 versioned bucket without MFA is safe here.}}
#   ^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_s3_bucket" "mycompliantbuckets6255" { # Compliant
  bucket = "mycompliantbuckets6255"

  versioning {
    enabled = true
    mfa_delete = true
  }
}

resource "not_a_bucket" "name" {
  bucket = "mycompliantbuckets6255"
}
