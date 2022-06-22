resource "aws_s3_bucket" "mynoncompliantbucket" {
  bucket = "mynoncompliantbucketname"
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
