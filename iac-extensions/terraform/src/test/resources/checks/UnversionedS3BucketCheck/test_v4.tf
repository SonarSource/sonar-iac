resource "aws_s3_bucket" "mynoncompliantbucket" {
  bucket = "mynoncompliantbucketname"
}

resource "aws_s3_bucket" "mycompliantbucket" {
  bucket = "mycompliantbucketname"

  versioning {
    enabled = true
  }
}

resource "aws_s3_bucket" "mynoncompliantbucket123454" {
  bucket = "mycompliantbucketname"

  versioning { } # Noncompliant
}

resource "aws_s3_bucket" "mynoncompliantfalsebuckets6252" {
  bucket = "mynoncompliantfalsebuckets6252name"

  versioning {
    enabled = false # Noncompliant
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
  versioning = {
    enabled = false # Noncompliant
  }
}

resource "aws_s3_bucket" "mynoncompliantbucket123454" {
  bucket = "mycompliantbucketname"

  versioning = { } # Noncompliant
}

locals {
  versioning = {
    enabled = false
  }
}

resource "aws_s3_bucket" "mynoncompliantbuckets6245" { # FN
  versioning     = "${local.versioning}"
}

resource "aws_s3_bucket_versioning" "non_compliant_disabled" {
  versioning_configuration {
    status = "Disabled" # Noncompliant
  }
}

resource "aws_s3_bucket_versioning" "non_compliant_suspended" {
  versioning_configuration {
    status = "Suspended" # Noncompliant
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
