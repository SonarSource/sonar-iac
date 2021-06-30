resource "aws_s3_bucket" "mynoncompliantbucket" { # Noncompliant {{Make sure using unversioned S3 bucket is safe here.}}
  #      ^^^^^^^^^^^^^^^
  bucket = "mynoncompliantbucketname"
}

resource "aws_s3_bucket" "mycompliantbucket" { # Compliant
  bucket = "mycompliantbucketname"

  versioning {
    enabled = true
  }
}

resource "aws_s3_bucket" "mynoncompliantbucket123454" { # Noncompliant
  bucket = "mycompliantbucketname"

  versioning {
  }
}

resource "aws_s3_bucket" "mynoncompliantfalsebuckets6252" { # Noncompliant {{Make sure using suspended versioned S3 bucket is safe here.}}
  #      ^^^^^^^^^^^^^^^
  bucket = "mynoncompliantfalsebuckets6252name"

  versioning {
    enabled = false
  #           ^^^^^< {{Suspended versioning.}}
  }
}

resource "not_a_bucket" "mynoncompliantbuckets6245" {}

resource {} # no labels

resource "aws_s3_bucket" "mynoncompliantbuckets6245" {
  versioning = {
    enabled = true
  }
}

resource "aws_s3_bucket" "mynoncompliantbuckets6245" { # Noncompliant
  versioning = {
    enabled = false
  }
}

locals {
  versioning = {
    enabled = false
  }
}

resource "aws_s3_bucket" "mynoncompliantbuckets6245" { # FN
  versioning     = "${local.versioning}"
}
