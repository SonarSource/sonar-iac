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
