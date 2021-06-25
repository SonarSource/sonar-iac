resource "aws_s3_bucket" "no_versioning" { # Noncompliant
  bucket = "mycompliantbuckets6255"
}

resource "aws_s3_bucket" "mynoncompliantbuckets6255" { # Noncompliant
  bucket = "mynoncompliantbuckets6255"

  versioning {
  }
}

resource "aws_s3_bucket" "mycompliantbuckets6255" { # Noncompliant {{Make sure allowing object deletion of a S3 versioned bucket without MFA is safe here.}}
  #      ^^^^^^^^^^^^^^^
  bucket = "mycompliantbuckets6255"

  versioning {
    mfa_delete = false
  # ^^^^^^^^^^^^^^^^^^< {{Should be true}}
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
