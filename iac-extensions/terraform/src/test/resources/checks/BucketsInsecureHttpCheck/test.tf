resource "aws_s3_bucket" "mynoncompliantbucket" { # Noncompliant {{No bucket policy enforces HTTPS-only access to this bucket.}}
  bucket = "mynoncompliantbucketname"
}

resource "aws_s3_bucket" "mynoncompliantallowbuckets6245" { # Noncompliant {{No bucket policy enforces HTTPS-only access to this bucket.}}
       # ^^^^^^^^^^^^^^^
  bucket = "mynoncompliantallowbucketrspecs6245myname"
}
resource "aws_s3_bucket_policy" "mynoncompliantallowpolicys6249" {
  bucket = "mynoncompliantallowbucketrspecs6245myname"

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
        Resource = [aws_s3_bucket.mynoncompliantallowbuckets6245.arn, "${aws_s3_bucket.mynoncompliantallowbuckets6245.arn}/*",]
        Condition = { Bool = { "aws:SecureTransport" = "true" } }
      #             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{HTTPS requests are denied.}}
      },
    ]
  })
}

resource "aws_s3_bucket" "mynoncompliantallowbuckets62451" { # Noncompliant
     #   ^^^^^^^^^^^^^^^
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
        #           ^^^^^^^< {{Non-conforming requests should be denied.}}
        Principal = {
          "AWS": "*"
        }
        Action    = "s3:*"
        Resource = [aws_s3_bucket.mynoncompliantallowbuckets6245.arn, "${aws_s3_bucket.mynoncompliantallowbuckets6245.arn}/*",]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      },
    ]
  })
}

resource "aws_s3_bucket" "mynoncompliantallowbuckets62452" { # Noncompliant
  #      ^^^^^^^^^^^^^^^
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
        Principal = { "AWS": "*" }
        Action    = "somethingElse"
        #           ^^^^^^^^^^^^^^^< {{All S3 actions should be restricted.}}
        Resource = [aws_s3_bucket.mynoncompliantallowbuckets6245.arn, "${aws_s3_bucket.mynoncompliantallowbuckets6245.arn}/*",]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      },
    ]
  })
}

resource "aws_s3_bucket" "mynoncompliantallowbuckets62453" { # Noncompliant
  #      ^^^^^^^^^^^^^^^
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
        Principal = { "AWS": ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"] }
          #         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{All principals should be restricted.}}
        Action    = "s3:*"
        Resource = [aws_s3_bucket.mynoncompliantallowbuckets6245.arn, "${aws_s3_bucket.mynoncompliantallowbuckets6245.arn}/*",]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      },
    ]
  })
}

resource "aws_s3_bucket" "mynoncompliantallowbuckets62454" { # Noncompliant
  #      ^^^^^^^^^^^^^^^
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
        Principal = { "AWS": "*" }
        Action    = "s3:*"
        Resource = ["${aws_s3_bucket.mynoncompliantallowbuckets6245.arn}/foo"]
        #          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^<
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
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
        Resource = [aws_s3_bucket.mycompliantharcodeds6249.arn, "${aws_s3_bucket.mycompliantharcodeds6249.arn}/*",]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      },
    ]
  })
}

resource "aws_s3_bucket" "compliant_policy_part_of_resource" {
  bucket = "compliant_policy_part_of_resource"

  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "mycomplianthardcodedpolicys6249"
    Statement = [
      {
        Sid       = "HTTPSOnly"
        Effect    = "Deny"
        Principal = "*"
        Action    = "s3:*"
        Resource = [aws_s3_bucket.compliant_policy_part_of_resource.arn, "${aws_s3_bucket.compliant_policy_part_of_resource.arn}/*",]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
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
        Resource = [aws_s3_bucket.mycompliantrefs6249.arn, "${aws_s3_bucket.mycompliantrefs6249.arn}/*",]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      },
    ]
  })
}

resource "aws_s3_bucket" "mycompliantrefs62491" { # Noncompliant
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

resource "aws_s3_bucket" "compliant_action_list" {
  bucket = "compliant-action-list"
}
resource "aws_s3_bucket_policy" "compliant_action_list" {
  bucket = aws_s3_bucket.compliant_action_list.id
  # Compliant, action as a list containing s3:*
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = { "AWS" = "*" }
        Action    = ["s3:*"]
        Resource  = ["${aws_s3_bucket.compliant_action_list.arn}/*", aws_s3_bucket.compliant_action_list.arn]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_s3_bucket" "noncompliant_action_list" { # Noncompliant
  bucket = "noncompliant-action-list"
}
resource "aws_s3_bucket_policy" "noncompliant_action_list" {
  bucket = aws_s3_bucket.noncompliant_action_list.id
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = { "AWS" = "*" }
        Action    = ["s3:GetObject"]
        Resource  = [aws_s3_bucket.noncompliant_action_list.arn, "${aws_s3_bucket.noncompliant_action_list.arn}/*"]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_s3_bucket" "compliant_principal_list" {
  bucket = "compliant-principal-list"
}
resource "aws_s3_bucket_policy" "compliant_principal_list" {
  bucket = aws_s3_bucket.compliant_principal_list.id
  # Compliant, Principal.AWS as a list containing *
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = { "AWS" = ["*"] }
        Action    = "s3:*"
        Resource  = ["${aws_s3_bucket.compliant_principal_list.arn}/*", aws_s3_bucket.compliant_principal_list.arn]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_s3_bucket" "noncompliant_principal_list_arn" { # Noncompliant
  bucket = "noncompliant-principal-list-arn"
}
resource "aws_s3_bucket_policy" "noncompliant_principal_list_arn" {
  bucket = aws_s3_bucket.noncompliant_principal_list_arn.id
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = { "AWS" = ["arn:aws:iam::123456789012:root"] }
        Action    = "s3:*"
        Resource  = [aws_s3_bucket.noncompliant_principal_list_arn.arn, "${aws_s3_bucket.noncompliant_principal_list_arn.arn}/*"]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_s3_bucket" "noncompliant_empty_resource" { # Noncompliant
  bucket = "noncompliant-empty-resource"
}
resource "aws_s3_bucket_policy" "noncompliant_empty_resource" {
  bucket = aws_s3_bucket.noncompliant_empty_resource.id
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = { "AWS" = "*" }
        Action    = "s3:*"
        Resource  = []
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_s3_bucket" "noncompliant_empty_action" { # Noncompliant
  bucket = "noncompliant-empty-action"
}
resource "aws_s3_bucket_policy" "noncompliant_empty_action" {
  bucket = aws_s3_bucket.noncompliant_empty_action.id
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = { "AWS" = "*" }
        Action    = []
        Resource  = [aws_s3_bucket.noncompliant_empty_action.arn, "${aws_s3_bucket.noncompliant_empty_action.arn}/*"]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_s3_bucket" "compliant_template_action" {
  bucket = "compliant-template-action"
}
resource "aws_s3_bucket_policy" "compliant_template_action" {
  bucket = aws_s3_bucket.compliant_template_action.id
  # Compliant, Action value cannot be resolved at analysis time
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = { "AWS" = "*" }
        Action    = "${var.action}"
        Resource  = [aws_s3_bucket.compliant_template_action.arn, "${aws_s3_bucket.compliant_template_action.arn}/*"]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_s3_bucket" "compliant_template_principal" {
  bucket = "compliant-template-principal"
}
resource "aws_s3_bucket_policy" "compliant_template_principal" {
  bucket = aws_s3_bucket.compliant_template_principal.id
  # Compliant, Principal.AWS value cannot be resolved at analysis time
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = { "AWS" = "${var.principal}" }
        Action    = "s3:*"
        Resource  = [aws_s3_bucket.compliant_template_principal.arn, "${aws_s3_bucket.compliant_template_principal.arn}/*"]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_s3_bucket" "noncompliant_resource_no_wildcard" { # Noncompliant
  bucket = "noncompliant-resource-no-wildcard"
}
resource "aws_s3_bucket_policy" "noncompliant_resource_no_wildcard" {
  bucket = aws_s3_bucket.noncompliant_resource_no_wildcard.id
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = { "AWS" = "*" }
        Action    = "s3:*"
        Resource  = aws_s3_bucket.noncompliant_resource_no_wildcard.arn
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_s3_bucket" "noncompliant_principal_list_template" { # Noncompliant
  bucket = "noncompliant-principal-list-template"
}
resource "aws_s3_bucket_policy" "noncompliant_principal_list_template" {
  bucket = aws_s3_bucket.noncompliant_principal_list_template.id
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = { "AWS" = ["${var.principal}"] }
        Action    = "s3:*"
        Resource  = [aws_s3_bucket.noncompliant_principal_list_template.arn, "${aws_s3_bucket.noncompliant_principal_list_template.arn}/*"]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_s3_bucket" "compliant_multi_statement" {
  bucket = "compliant_multi_statement"
}
resource "aws_s3_bucket_policy" "compliant_multi_statement" {
  bucket = aws_s3_bucket.compliant_multi_statement.id
  # Valid Deny-HTTPS enforcement plus a legitimate Allow (e.g. S3 log delivery)
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = "*"
        Action    = "s3:*"
        Resource  = ["${aws_s3_bucket.compliant_multi_statement.arn}/*"]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      },
      {
        Effect    = "Allow"
        Principal = { Service = "logging.s3.amazonaws.com" }
        Action    = "s3:PutObject"
        Resource  = "${aws_s3_bucket.compliant_multi_statement.arn}/*"
      },
    ]
  })
}

resource "aws_s3_bucket" "noncompliant_multi_statement" { # Noncompliant
  bucket = "noncompliant_multi_statement"
}
resource "aws_s3_bucket_policy" "noncompliant_multi_statement" {
  bucket = aws_s3_bucket.noncompliant_multi_statement.id
  # Deny statement has wrong condition — should still be flagged despite Allow sibling
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = "*"
        Action    = "s3:*"
        Resource  = ["${aws_s3_bucket.noncompliant_multi_statement.arn}/*"]
        Condition = { Bool = { "aws:SecureTransport" = "true" } }
      },
      {
        Effect    = "Allow"
        Principal = { Service = "logging.s3.amazonaws.com" }
        Action    = "s3:PutObject"
        Resource  = "${aws_s3_bucket.noncompliant_multi_statement.arn}/*"
      },
    ]
  })
}
