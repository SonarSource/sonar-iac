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
        # SONARIAC-1803: condition value "true" inverts the protection — statement is no longer an HTTPS-only attempt;
        # only the primary issue is raised, with no per-field secondaries.
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

# === SONARIAC-1804: consistency with CloudFormation ===

resource "aws_s3_bucket" "compliant_effect_unusual_value" {
  bucket = "compliant-effect-unusual-value"
}
resource "aws_s3_bucket_policy" "compliant_effect_unusual_value" {
  bucket = aws_s3_bucket.compliant_effect_unusual_value.id
  # SONARIAC-1804: an Effect with an unresolved/unusual value (here a Terraform variable interpolation)
  # is no longer flagged as insecure. With CFN-aligned semantics, only the explicit literal "Allow" is.
  # Before SONARIAC-1804 the Terraform check would have flagged this (effect != "Deny" → insecure).
  policy = jsonencode({
    Statement = [
      {
        Effect    = "${var.effect}"
        Principal = "*"
        Action    = "s3:*"
        Resource  = ["${aws_s3_bucket.compliant_effect_unusual_value.arn}/*"]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

# === SONARIAC-1803: no contradictory secondary messages ===

resource "aws_s3_bucket" "noncompliant_unrelated_condition" { # Noncompliant {{No bucket policy enforces HTTPS-only access to this bucket.}}
  bucket = "noncompliant-unrelated-condition"
}
resource "aws_s3_bucket_policy" "noncompliant_unrelated_condition" {
  bucket = aws_s3_bucket.noncompliant_unrelated_condition.id
  # Deny statement with a Condition that has nothing to do with aws:SecureTransport — not an HTTPS-only attempt.
  # The bucket is still Noncompliant (no HTTPS-only enforcement) but NO per-field secondaries (e.g. "All S3 actions
  # should be restricted") should be raised — that would contradict the actual intent of the statement.
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource  = ["${aws_s3_bucket.noncompliant_unrelated_condition.arn}/sensitive"]
        Condition = { IpAddress = { "aws:SourceIp" = "1.2.3.4/32" } }
      }
    ]
  })
}

# === SONARIAC-1801: non-explicit AWS principal ===

resource "aws_s3_bucket" "compliant_string_principal_wildcard" {
  bucket = "compliant-string-principal-wildcard"
}
resource "aws_s3_bucket_policy" "compliant_string_principal_wildcard" {
  bucket = aws_s3_bucket.compliant_string_principal_wildcard.id
  # Compliant: Principal as a bare string "*" (covers all principals — equivalent to { AWS = "*" })
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = "*"
        Action    = "s3:*"
        Resource  = ["${aws_s3_bucket.compliant_string_principal_wildcard.arn}/*"]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_s3_bucket" "noncompliant_string_principal_arn" { # Noncompliant {{No bucket policy enforces HTTPS-only access to this bucket.}}
  bucket = "noncompliant-string-principal-arn"
}
resource "aws_s3_bucket_policy" "noncompliant_string_principal_arn" {
  bucket = aws_s3_bucket.noncompliant_string_principal_arn.id
  # Principal as a bare ARN string — does not cover all principals (FN before SONARIAC-1801)
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = "arn:aws:iam::123456789012:root"
        Action    = "s3:*"
        Resource  = ["${aws_s3_bucket.noncompliant_string_principal_arn.arn}/*"]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_s3_bucket" "compliant_string_principal_list_wildcard" {
  bucket = "compliant-string-principal-list-wildcard"
}
resource "aws_s3_bucket_policy" "compliant_string_principal_list_wildcard" {
  bucket = aws_s3_bucket.compliant_string_principal_list_wildcard.id
  # Compliant: Principal as a list-of-strings containing "*"
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = ["*"]
        Action    = "s3:*"
        Resource  = ["${aws_s3_bucket.compliant_string_principal_list_wildcard.arn}/*"]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

resource "aws_s3_bucket" "noncompliant_string_principal_list_arn" { # Noncompliant {{No bucket policy enforces HTTPS-only access to this bucket.}}
  bucket = "noncompliant-string-principal-list-arn"
}
resource "aws_s3_bucket_policy" "noncompliant_string_principal_list_arn" {
  bucket = aws_s3_bucket.noncompliant_string_principal_list_arn.id
  # Principal as a list-of-ARN-strings (no wildcard) — does not cover all principals
  policy = jsonencode({
    Statement = [
      {
        Effect    = "Deny"
        Principal = ["arn:aws:iam::123456789012:root"]
        Action    = "s3:*"
        Resource  = ["${aws_s3_bucket.noncompliant_string_principal_list_arn.arn}/*"]
        Condition = { Bool = { "aws:SecureTransport" = "false" } }
      }
    ]
  })
}

# === SONARIAC-1800: POLICY heredoc ===

resource "aws_s3_bucket" "compliant_heredoc_policy" {
  bucket = "compliant-heredoc-policy"
}
resource "aws_s3_bucket_policy" "compliant_heredoc_policy" {
  bucket = aws_s3_bucket.compliant_heredoc_policy.id
  # Compliant: full HTTPS-only Deny policy expressed via <<POLICY heredoc
  policy = <<POLICY
{
  "Version": "2012-10-17",
  "Id": "HTTPSOnly",
  "Statement": [
    {
      "Sid": "HTTPSOnly",
      "Effect": "Deny",
      "Principal": "*",
      "Action": "s3:*",
      "Resource": ["arn:aws:s3:::compliant-heredoc-policy/*"],
      "Condition": { "Bool": { "aws:SecureTransport": "false" } }
    }
  ]
}
POLICY
}

resource "aws_s3_bucket" "compliant_heredoc_eof_marker" {
  bucket = "compliant-heredoc-eof-marker"
}
resource "aws_s3_bucket_policy" "compliant_heredoc_eof_marker" {
  bucket = aws_s3_bucket.compliant_heredoc_eof_marker.id
  # Compliant: same body but with the alternative <<EOF marker — verifies marker-agnostic stripping
  policy = <<EOF
{
  "Statement": [
    {
      "Effect": "Deny",
      "Principal": "*",
      "Action": "s3:*",
      "Resource": ["arn:aws:s3:::compliant-heredoc-eof-marker/*"],
      "Condition": { "Bool": { "aws:SecureTransport": "false" } }
    }
  ]
}
EOF
}

resource "aws_s3_bucket" "noncompliant_heredoc_wrong_effect" { # Noncompliant
  bucket = "noncompliant-heredoc-wrong-effect"
}
resource "aws_s3_bucket_policy" "noncompliant_heredoc_wrong_effect" {
  bucket = aws_s3_bucket.noncompliant_heredoc_wrong_effect.id
  # Effect = Allow on what otherwise looks like an HTTPS-only Deny policy
  policy = <<POLICY
{
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:*",
      "Resource": ["arn:aws:s3:::noncompliant-heredoc-wrong-effect/*"],
      "Condition": { "Bool": { "aws:SecureTransport": "false" } }
    }
  ]
}
POLICY
}

resource "aws_s3_bucket" "noncompliant_heredoc_narrow_action" { # Noncompliant
  bucket = "noncompliant-heredoc-narrow-action"
}
resource "aws_s3_bucket_policy" "noncompliant_heredoc_narrow_action" {
  bucket = aws_s3_bucket.noncompliant_heredoc_narrow_action.id
  # Action narrowed to s3:GetObject — does not cover all S3 actions
  policy = <<POLICY
{
  "Statement": [
    {
      "Effect": "Deny",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": ["arn:aws:s3:::noncompliant-heredoc-narrow-action/*"],
      "Condition": { "Bool": { "aws:SecureTransport": "false" } }
    }
  ]
}
POLICY
}

resource "aws_s3_bucket" "heredoc_malformed_json" {
  bucket = "heredoc-malformed-json"
}
resource "aws_s3_bucket_policy" "heredoc_malformed_json" {
  bucket = aws_s3_bucket.heredoc_malformed_json.id
  # Malformed JSON — falls back to UNKNOWN_POLICY (safe-by-default), so no issue is raised
  policy = <<POLICY
{ not valid json
POLICY
}

resource "aws_s3_bucket" "compliant_indented_heredoc_policy" {
  bucket = "compliant-indented-heredoc-policy"
}
resource "aws_s3_bucket_policy" "compliant_indented_heredoc_policy" {
  bucket = aws_s3_bucket.compliant_indented_heredoc_policy.id
  # Compliant: indented <<- form. Closing marker may be indented, leading whitespace inside the body
  # is irrelevant to JSON parsing.
  policy = <<-POLICY
    {
      "Statement": [
        {
          "Effect": "Deny",
          "Principal": "*",
          "Action": "s3:*",
          "Resource": ["arn:aws:s3:::compliant-indented-heredoc-policy/*"],
          "Condition": { "Bool": { "aws:SecureTransport": "false" } }
        }
      ]
    }
  POLICY
}

resource "aws_s3_bucket" "noncompliant_indented_heredoc_policy" { # Noncompliant
  bucket = "noncompliant-indented-heredoc-policy"
}
resource "aws_s3_bucket_policy" "noncompliant_indented_heredoc_policy" {
  bucket = aws_s3_bucket.noncompliant_indented_heredoc_policy.id
  # Indented <<- heredoc with Action narrowed to a single operation — does not cover all S3 actions
  policy = <<-POLICY
    {
      "Statement": [
        {
          "Effect": "Deny",
          "Principal": "*",
          "Action": "s3:GetObject",
          "Resource": ["arn:aws:s3:::noncompliant-indented-heredoc-policy/*"],
          "Condition": { "Bool": { "aws:SecureTransport": "false" } }
        }
      ]
    }
  POLICY
}

resource "aws_s3_bucket" "compliant_heredoc_multiple_resources" {
  bucket = "compliant-heredoc-multiple-resources"
}
resource "aws_s3_bucket_policy" "compliant_heredoc_multiple_resources" {
  bucket = aws_s3_bucket.compliant_heredoc_multiple_resources.id
  # Compliant: the Resource list covers both the bucket ARN and all its objects (the "/*" entry),
  # so every relevant resource is restricted.
  policy = <<POLICY
{
  "Statement": [
    {
      "Effect": "Deny",
      "Principal": "*",
      "Action": "s3:*",
      "Resource": [
        "arn:aws:s3:::compliant-heredoc-multiple-resources",
        "arn:aws:s3:::compliant-heredoc-multiple-resources/*"
      ],
      "Condition": { "Bool": { "aws:SecureTransport": "false" } }
    }
  ]
}
POLICY
}

resource "aws_s3_bucket" "noncompliant_heredoc_multiple_resources" { # Noncompliant
  bucket = "noncompliant-heredoc-multiple-resources"
}
resource "aws_s3_bucket_policy" "noncompliant_heredoc_multiple_resources" {
  bucket = aws_s3_bucket.noncompliant_heredoc_multiple_resources.id
  # None of the listed resources ends with "*", so the bucket's objects are not covered.
  policy = <<POLICY
{
  "Statement": [
    {
      "Effect": "Deny",
      "Principal": "*",
      "Action": "s3:*",
      "Resource": [
        "arn:aws:s3:::noncompliant-heredoc-multiple-resources",
        "arn:aws:s3:::noncompliant-heredoc-multiple-resources/object"
      ],
      "Condition": { "Bool": { "aws:SecureTransport": "false" } }
    }
  ]
}
POLICY
}
