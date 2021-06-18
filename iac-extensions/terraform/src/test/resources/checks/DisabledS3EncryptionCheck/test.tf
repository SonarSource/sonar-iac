resource "aws_s3_bucket" "mynoncompliantbuckets6245" { # Noncompliant {{Make sure not using server-side encryption is safe here.}}
  #      ^^^^^^^^^^^^^^^
  bucket = "mynoncompliantbuckets6245"
}

resource "aws_s3_bucket" "mynoncompliantbuckets6245" { # Noncompliant
}

resource "aws_s3_bucket" "mynoncompliantbuckets6245" { # Noncompliant
  bucket = "mycompliantbuckets6245"

  not_server_side_encryption {
  }
}

resource "aws_s3_bucket" "mycompliantbuckets6245" { # Compliant
  bucket = "mycompliantbuckets6245"

  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        sse_algorithm     = "AES256"
      }
    }
  }
}

resource "not_a_bucket" "mynoncompliantbuckets6245" {}

resource {} # no labels
