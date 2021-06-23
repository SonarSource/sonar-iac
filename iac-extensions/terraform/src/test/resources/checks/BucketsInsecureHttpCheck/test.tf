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

resource "aws_s3_bucket" "mycompliantrefs62491" {
  bucket = "mycompliantrefs62491myname"
}
resource "aws_s3_bucket_policy" "mycompliantrefpolicys62491" {
  bucket = aws_s3_bucket.mycompliantrefs62491.id
  # missing policy attribute
}

resource "aws_s3_bucket_policy" "mycompliantrefpolicys62492" {
  # missing bucket and policy attribute
}

resource "aws_s3_bucket" "mycompliantrefs62492" {
  bucket = "mycompliantrefs62492myname"
}
resource "aws_s3_bucket_policy" "mycompliantrefpolicys62492" {
  bucket = aws_s3_bucket.mycompliantrefs62492.id
  # policy is retrieved through file function - we don't check it
  policy = file("policy.json")
}

resource "aws_s3_bucket" "mycompliantrefs62493" {
  bucket = "mycompliantrefs62493myname"
}
resource "aws_s3_bucket_policy" "mycompliantrefpolicys62493" {
  bucket = aws_s3_bucket.mycompliantrefs62493.id
  # policy retrieved through data
  policy = data.aws_iam_policy_document.s3_bucket_policy.json
}

resource "aws_s3_bucket" "mycompliantrefs62494" {
  bucket = "mycompliantrefs62494myname"
}
resource "aws_s3_bucket_policy" "mycompliantrefpolicys62494" {
  bucket = aws_s3_bucket.mycompliantrefs62494.id
  # policy not provided
  policy = jsonencode()
}

resource "aws_s3_bucket" "mycompliantrefs62495" {
  bucket = "mycompliantrefs62495myname"
}
resource "aws_s3_bucket_policy" "mycompliantrefpolicys62495" {
  bucket = aws_s3_bucket.mycompliantrefs62495.id
  # no statement provided in policy
  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "mycompliantrefpolicys6249"
  })
}

resource "aws_s3_bucket" "mycompliantrefs62496" {
  bucket = "mycompliantrefs62496myname"
}
resource "aws_s3_bucket_policy" "mycompliantrefpolicys62496" {
  bucket = aws_s3_bucket.mycompliantrefs62496.id
  # statement pulled from data
  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "mycompliantrefpolicys6249"
    Statement = data.aws_iam_policy_document.s3_bucket_policy.statement
  })
}

resource "aws_s3_bucket" "mycompliantrefs62497" {
  bucket = "mycompliantrefs62497myname"
}
resource "aws_s3_bucket_policy" "mycompliantrefpolicys62497" {
  bucket = aws_s3_bucket.mycompliantrefs62497.id
  # statement is empty tuple
  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "mycompliantrefpolicys6249"
    Statement = []
  })
}

resource "aws_s3_bucket" "mycompliantrefs62498" {
  bucket = "mycompliantrefs62498myname"
}
resource "aws_s3_bucket_policy" "mycompliantrefpolicys62498" {
  bucket = aws_s3_bucket.mycompliantrefs62498.id
  # statement content is pulled from data
  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "mycompliantrefpolicys6249"
    Statement = [data.aws_iam_policy_document.s3_bucket_policy.statement]
  })
}
