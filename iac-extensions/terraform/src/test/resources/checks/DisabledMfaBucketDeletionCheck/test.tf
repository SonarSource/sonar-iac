resource "aws_s3_bucket" "no_versioning" { # Noncompliant
  bucket = "mycompliantbuckets6255"
}

resource "aws_s3_bucket" "mynoncompliantbuckets6255" { # Noncompliant
  bucket = "mynoncompliantbuckets6255"

  versioning {
  }
}

resource "aws_s3_bucket" "mycompliantbuckets6255" { # Noncompliant
  #      ^^^^^^^^^^^^^^^
  bucket = "mycompliantbuckets6255"

  versioning {
    mfa_delete = false
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
