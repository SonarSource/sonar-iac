resource "aws_s3_bucket_policy" "sensitive_policy_with_anonymous_access_literal" {
  bucket = aws_s3_bucket
  policy = jsonencode({
    Statement = [
      {
        Effect: "Allow",
        #       ^^^^^^^> {{Related effect.}}
        Principal: "*", # Noncompliant {{Make sure granting public access is safe here.}}
        #          ^^^
      }
    ]
  })
}

resource "aws_s3_bucket_policy" "sensitive_policy_with_anonymous_access_object_mapping" {
  bucket = aws_s3_bucket
  policy = jsonencode({
    Statement = [
      {
        Effect: "Allow",
        #       ^^^^^^^> {{Related effect.}}
        Principal: {
          "AWS": "*" # Noncompliant
          #      ^^^
        }
      }
    ]
  })
}

resource "aws_s3_bucket_policy" "sensitive_policy_with_anonymous_access_object_mapping_with_deny" {
  bucket = aws_s3_bucket
  policy = jsonencode({
    Statement = [
      {
        Effect: "Deny",
        #       ^^^^^^> {{Related effect.}}
        NotPrincipal: {
          "AWS": "*" # Noncompliant
          #      ^^^
        }
      }
    ]
  })
}

resource "aws_s3_bucket_policy" "compliant_policy_with_denying_effect" {
  bucket = aws_s3_bucket
  policy = jsonencode({
    Statement = [
      {
        Effect: "Deny",
        Principal: "*",
      }
    ]
  })
}

resource "aws_s3_bucket_policy" "compliant_policy_without_aws_principal" {
  bucket = aws_s3_bucket
  policy = jsonencode({
    Statement = [{
      Effect: "Allow",
      Principal: {
        "Foo": "*"
      }
    }]
  })
}

resource "aws_s3_bucket" "sensitive_bucket_with_anonymous_access_literal" {
  policy = jsonencode({
    Statement = [{
        Effect    = "Allow"
        Principal = "*" # Noncompliant
    }]
  })
}

resource "aws_s3_bucket" "sensitive_bucket_with_anonymous_access_object_mapping" {
  policy = jsonencode({
    Statement = [{
      Effect    = "Allow"
      Principal = {
        "AWS": "*" # Noncompliant
      }
    }]
  })
}

resource "aws_s3_bucket" "compliant_bucket_with_denying_effect" {
  policy = jsonencode({
    Statement = [{
      Effect    = "Deny"
      Principal = "*"
    }]
  })
}

resource "aws_s3_bucket" "sensitive_policy_with_object_mapping_principal" {
  policy = jsonencode({
    Statement = [{
      Effect    = "Allow"
      #           ^^^^^^^>
      Principal = {
        "AWS": [
          "*" # Noncompliant
#         ^^^
        ]
      }
    }]
  })
}

resource "not_a_bucket" "compliant_resource" { # Compliant
}

resource "aws_s3_bucket" "compliant_bucket_without_principal" { # Compliant
  policy = jsonencode({
    Statement = [{
      Effect    = "Deny"
    }]
  })
}

resource "aws_s3_bucket" "compliant_bucket_without_effect" { # Compliant
  policy = jsonencode({
    Statement = [{
      Principal = "*"
    }]
  })
}

resource "aws_s3_bucket" "compliant_bucket_with_effect_but_different_principal" { # Compliant
  policy = jsonencode({
    Statement = [{
      Effect    = "Allow"
      Principal = "something"
    }]
  })
}

resource "aws_s3_bucket_policy" "compliant_policy_with_no_valid_principal" {
  bucket = aws_s3_bucket
  policy = jsonencode({
    Statement = [
      {
        Effect: "Allow",
        Principal: {
          "AWS": {
            "not_valid" = "*"
          }
        }
      }
    ]
  })
}

resource "aws_s3_bucket_policy" "invlid_policy" {
  bucket = aws_s3_bucket
  policy = jsonencode()
}

data "aws_iam_policy_document" "example_with_statements" {
  statement {
    sid = "1"

    actions = [
      "s3:ListAllMyBuckets",
      "s3:GetBucketLocation",
    ]

    resources = [
      "arn:aws:s3:::*",
    ]
  }

  statement {
    actions = [
      "s3:ListBucket",
    ]

    resources = [
      "arn:aws:s3:::${var.s3_bucket_name}",
    ]

    condition {
      test     = "StringLike"
      variable = "s3:prefix"

      values = [
        "",
        "home/",
        "home/&{aws:username}/",
      ]
    }
  }

  statement {
    sid = "3"
    effect = "Allow"
    #        ^^^^^^^>

   principals {
     type = "AWS"
     identifiers = [
       "*"  # Noncompliant {{Make sure granting public access is safe here.}}
     # ^^^
     ]
    }

    resources = [
      "arn:aws:s3:::*",
    ]
  }

  statement {
    sid = "3"
    effect = "Deny"
    #        ^^^^^^>

   not_principals {
     type = "AWS"
     identifiers = [
       "*"  # Noncompliant {{Make sure granting public access is safe here.}}
     # ^^^
     ]
    }

    resources = [
      "arn:aws:s3:::*",
    ]
  }

  statement {
    actions = [
      "s3:*",
    ]

    resources = [
      "arn:aws:s3:::${var.s3_bucket_name}/home/&{aws:username}",
      "arn:aws:s3:::${var.s3_bucket_name}/home/&{aws:username}/*",
    ]
  }
}
