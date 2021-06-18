resource "aws_s3_bucket" "mynoncompliantbucket" { # Noncompliant {{Make sure authorizing HTTP requests is safe here.}}
  bucket = "mynoncompliantbucketname"
}

resource "aws_s3_bucket" "mycompliantharcodeds6249" {
  bucket = "mycompliantharcodeds6249myname"
}

resource "aws_s3_bucket_policy" "mycomplianthardcodedpolicys6249" {
  bucket = "mycompliantharcodeds6249myname"

  # Terraform's "jsonencode" function converts a
  # Terraform expression's result to valid JSON syntax.

  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "mycomplianthardcodedpolicys6249"
    Statement = [
      {
        Sid       = "HTTPSOnly"
        Effect    = "Deny"
        Principal = "*"
        Action    = "s3:*"
        Resource = [
          aws_s3_bucket.mycompliantharcodeds6249.arn,
          "${aws_s3_bucket.mycompliantharcodeds6249.arn}/*",
        ]
        Condition = {
          Bool = {
            "aws:SecureTransport" = "false"
          }
        }
      },
    ]
  })
}

resource "aws_s3_bucket" "mycompliantrefs6249" {
  bucket = "mycompliantrefs6249myname"
}

resource "aws_s3_bucket_policy" "mycompliantrefpolicys6249" {
  bucket = aws_s3_bucket.mycompliantrefs6249.id

  # Terraform's "jsonencode" function converts a
  # Terraform expression's result to valid JSON syntax.

  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "mycompliantrefpolicys6249"
    Statement = [
      {
        Sid       = "HTTPSOnly"
        Effect    = "Deny"
        Principal = "*"
        Action    = "s3:*"
        Resource = [
          aws_s3_bucket.mycompliantrefs6249.arn,
          "${aws_s3_bucket.mycompliantrefs6249.arn}/*",
        ]
        Condition = {
          Bool = {
            "aws:SecureTransport" = "false"
          }
        }
      },
    ]
  })
}
