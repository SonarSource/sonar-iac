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

# aws_s3_bucket_versioning is a resource introduces in AWS provider 4.0.
# However, we analyze this resource even with version 3 or no provided version
# since it is only available from version 4 anyway.

resource "aws_s3_bucket_versioning" "non_compliant_disabled" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^> {{Related bucket}}
  versioning_configuration {
    status = "Disabled" # Noncompliant {{Make sure using unversioned S3 bucket is safe here.}}
  # ^^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_s3_bucket_versioning" "non_compliant_suspended" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^> {{Related bucket}}
  versioning_configuration {
    status = "Suspended" # Noncompliant {{Make sure using suspended versioned S3 bucket is safe here.}}
  # ^^^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_s3_bucket_versioning" "compliant" {
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_versioning" "compliant" {
  versioning_configuration {
    status = foo.versioning.status
  }
}
