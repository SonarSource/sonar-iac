# Noncompliant@+1 {{Omitting "versioning" disables S3 bucket versioning. Make sure it is safe here.}}
resource "aws_s3_bucket" "mynoncompliantbucket" {
  #      ^^^^^^^^^^^^^^^
  bucket = "mynoncompliantbucketname"
}

resource "aws_s3_bucket" "mycompliantbucket" { # Compliant
  bucket = "mycompliantbucketname"

  versioning {
    enabled = true
  }
}

resource "aws_s3_bucket" "mynoncompliantbucket123454" {
  #      ^^^^^^^^^^^^^^^> {{Related bucket}}
  bucket = "mycompliantbucketname"

  versioning { } # Noncompliant {{Make sure using unversioned S3 bucket is safe here.}}
  # ^^^^^^^^^^
}

resource "aws_s3_bucket" "mynoncompliantfalsebuckets6252" {
  #      ^^^^^^^^^^^^^^^> {{Related bucket}}
  bucket = "mynoncompliantfalsebuckets6252name"

  versioning {
    enabled = false # Noncompliant {{Make sure using suspended versioned S3 bucket is safe here.}}
    # ^^^^^^^^^^^^^^^
  }
}

resource "not_a_bucket" "mynoncompliantbuckets6245" {}

resource {} # no labels

resource "aws_s3_bucket" "mynoncompliantbuckets6245" {
  versioning = {
    enabled = true
  }
}

resource "aws_s3_bucket" "mynoncompliantbuckets6245" {
  #      ^^^^^^^^^^^^^^^> {{Related bucket}}
  versioning = {
    enabled = false # Noncompliant {{Make sure using suspended versioned S3 bucket is safe here.}}
    # ^^^^^^^^^^^^^^^
  }
}

resource "aws_s3_bucket" "mynoncompliantbucket123454" {
  #      ^^^^^^^^^^^^^^^> {{Related bucket}}
  bucket = "mycompliantbucketname"

  versioning = { } # Noncompliant {{Make sure using unversioned S3 bucket is safe here.}}
  # ^^^^^^^^^^
}

locals {
  versioning = {
    enabled = false
  }
}

resource "aws_s3_bucket" "mynoncompliantbuckets6245" { # FN
  versioning     = "${local.versioning}"
}
