resource "aws_s3_bucket" "mynoncompliantbuckets6245" {
  #      ^^^^^^^^^^^^^^^> {{Related bucket}}
  bucket = "mynoncompliantbuckets6245name"
  acl    = "public-read-write" # Noncompliant {{Make sure granting access to AllUsers group is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_s3_bucket" "mynoncompliantbuckets6245" {
  #      ^^^^^^^^^^^^^^^> {{Related bucket}}
  bucket = "mynoncompliantbuckets6245name"
  acl    = "public-read" # Noncompliant {{Make sure granting access to AllUsers group is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_s3_bucket" "mynoncompliantbuckets6245" {
  #      ^^^^^^^^^^^^^^^> {{Related bucket}}
  bucket = "mynoncompliantbuckets6245name"
  acl    = "authenticated-read" # Noncompliant {{Make sure granting access to AuthenticatedUsers group is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_s3_bucket" "mycompliantprivatebuckets6265" { # Compliant
  bucket = "mycompliantprivatebuckets6265name"
  acl    = "private"
}

resource "aws_s3_bucket" "mycompliantprivatebuckets6265" { # Compliant
  bucket = "mycompliantprivatebuckets6265name"
  acl    = data.unknown
}

resource "aws_s3_bucket" "mycompliantprivatebuckets6265" { # Compliant
  bucket = "mycompliantprivatebuckets6265name"
}

resource "not_a_bucket" "foo" { # Compliant
  bucket = "foo"
  acl    = "authenticated-read"
}
