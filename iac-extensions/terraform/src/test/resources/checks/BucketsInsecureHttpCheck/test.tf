resource "aws_s3_bucket" "mynoncompliantbucket" { # Noncompliant {{Make sure authorizing HTTP requests is safe here.}}
  bucket = "mynoncompliantbucketname"
}

resource "aws_s3_bucket" "mynoncompliantallowbuckets6245" { # Noncompliant
  bucket = "mynoncompliantallowbucketrspecs6245myname"
}
resource "aws_s3_bucket_policy" "mynoncompliantallowpolicys6249" {
  bucket = "mynoncompliantallowbucketrspecs6245myname"

  # aws:SecureTransport is not set to false

  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "mynoncompliantallowpolicys6249"
    Statement = [
      {
        Sid       = "HTTPSOnly"
        Effect    = "Deny"
        Principal = {
          "AWS": "*"
        }
        Action    = "s3:*"
        Resource = [
          aws_s3_bucket.mynoncompliantallowbuckets6245.arn,
          "${aws_s3_bucket.mynoncompliantallowbuckets6245.arn}/*",
        ]
        Condition = {
          Bool = {
            "aws:SecureTransport" = "true" # secondary location (https request are denied)
          }
        }
      },
    ]
  })
}

resource "aws_s3_bucket" "mynoncompliantallowbuckets62451" { # Noncompliant
  bucket = "mynoncompliantallowbucketrspecs62451myname"
}
resource "aws_s3_bucket_policy" "mynoncompliantallowpolicys6249" {
  bucket = "mynoncompliantallowbucketrspecs62451myname"

  # Effect is not set to Deny

  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "mynoncompliantallowpolicys6249"
    Statement = [
      {
        Sid       = "HTTPSOnly"
        Effect    = "Allow"
        Principal = {
          "AWS": "*"
        }
        Action    = "s3:*"
        Resource = [
          aws_s3_bucket.mynoncompliantallowbuckets6245.arn,
          "${aws_s3_bucket.mynoncompliantallowbuckets6245.arn}/*",
        ]
        Condition = {
          Bool = {
            "aws:SecureTransport" = "false" # secondary location (https request are denied)
          }
        }
      },
    ]
  })
}

resource "aws_s3_bucket" "mynoncompliantallowbuckets62452" { # Noncompliant
  bucket = "mynoncompliantallowbucketrspecs62452myname"
}
resource "aws_s3_bucket_policy" "mynoncompliantallowpolicys6249" {
  bucket = "mynoncompliantallowbucketrspecs62452myname"

  # Action is not set to * or s3:*

  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "mynoncompliantallowpolicys6249"
    Statement = [
      {
        Sid       = "HTTPSOnly"
        Effect    = "Deny"
        Principal = {
          "AWS": "*"
        }
        Action    = "somethingElse"
        Resource = [
          aws_s3_bucket.mynoncompliantallowbuckets6245.arn,
          "${aws_s3_bucket.mynoncompliantallowbuckets6245.arn}/*",
        ]
        Condition = {
          Bool = {
            "aws:SecureTransport" = "false" # secondary location (https request are denied)
          }
        }
      },
    ]
  })
}

resource "aws_s3_bucket" "mynoncompliantallowbuckets62453" { # Noncompliant
  bucket = "mynoncompliantallowbucketrspecs62453myname"
}
resource "aws_s3_bucket_policy" "mynoncompliantallowpolicys6249" {
  bucket = "mynoncompliantallowbucketrspecs62453myname"

  # Principal.AWS is not set to *

  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "mynoncompliantallowpolicys6249"
    Statement = [
      {
        Sid       = "HTTPSOnly"
        Effect    = "Deny"
        Principal = {
          "AWS": [
            "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
          ]
        }
        Action    = "s3:*"
        Resource = [
          aws_s3_bucket.mynoncompliantallowbuckets6245.arn,
          "${aws_s3_bucket.mynoncompliantallowbuckets6245.arn}/*",
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

resource "aws_s3_bucket" "mynoncompliantallowbuckets62454" { # Noncompliant
  bucket = "mynoncompliantallowbucketrspecs62454myname"
}
resource "aws_s3_bucket_policy" "mynoncompliantallowpolicys6249" {
  bucket = "mynoncompliantallowbucketrspecs62454myname"

  # Resource identifier not ending with *

  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "mynoncompliantallowpolicys6249"
    Statement = [
      {
        Sid       = "HTTPSOnly"
        Effect    = "Deny"
        Principal = {
          "AWS": "*"
        }
        Action    = "s3:*"
        Resource = [
          "${aws_s3_bucket.mynoncompliantallowbuckets6245.arn}/foo",
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
