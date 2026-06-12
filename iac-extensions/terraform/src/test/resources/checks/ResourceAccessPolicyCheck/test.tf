resource "aws_iam_policy" "non_compliant_policy1" {
  policy = jsonencode({
    Statement = [
      {
        Resource = "*" # Noncompliant {{Make sure granting access to all resources is safe here.}}
#                  ^^^
        Effect = "Allow"
#                ^^^^^^^< {{Related effect}}
        Action = "iam:CreatePolicyVersion"
#                ^^^^^^^^^^^^^^^^^^^^^^^^^< {{Related action}}
      }
    ]
  })
}

resource "aws_iam_policy" "non_compliant_policy2" {
  policy = jsonencode({
    Statement = [
      {
        Resource = [
          "*" # Noncompliant {{Make sure granting access to all resources is safe here.}}
#         ^^^
        ]
        Effect = "Allow"
#                ^^^^^^^< {{Related effect}}
        Action = "iam:CreatePolicyVersion"
#                ^^^^^^^^^^^^^^^^^^^^^^^^^< {{Related action}}
      }
    ]
  })
}

resource "aws_iam_policy" "non_compliant_policy2" {
  policy = jsonencode({
    Statement = [
      {
        NotResource = "*" # Noncompliant
        Effect = "Deny"
        Action = "iam:CreatePolicyVersion"
      }
    ]
  })
}

resource "aws_iam_policy" "non_compliant_policy3" {
  policy = jsonencode({
    Statement = [
      {
        NotResource = [
          "*" # Noncompliant
        ]
        Effect = "Deny"
        Action = "iam:CreatePolicyVersion"
      }
    ]
  })
}

resource "aws_iam_role" "non_compliant_policy4" {
  inline_policy {
    policy = jsonencode({
      Statement = [
        {
          Resource = "*" # Noncompliant {{Make sure granting access to all resources is safe here.}}
#                    ^^^
          Effect = "Allow"
#                  ^^^^^^^< {{Related effect}}
          Action = "iam:CreatePolicyVersion"
#                  ^^^^^^^^^^^^^^^^^^^^^^^^^< {{Related action}}
        },
      ]
    })
  }
}

resource "aws_iam_policy" "compliant_policy1" {
  policy = jsonencode({
    Statement = [
      {
        Resource = "foo" # Compliant
        Effect = "Allow"
        Action = "iam:CreatePolicyVersion"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy2" {
  policy = jsonencode({
    Statement = [
      {
        Resource = "*"
        Effect = "Deny" # Compliant
        Action = "iam:CreatePolicyVersion"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy3" {
  policy = jsonencode({
    Statement = [
      {
        Resource = "*" # Compliant
        Action = "iam:CreatePolicyVersion"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy4" {
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow" # Compliant
        Action = "iam:CreatePolicyVersion"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy5" {
  policy = jsonencode({
    Statement = [
      {
        "Resource" = "foo"
        Action = "iam:CreatePolicyVersion"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_missing_action" {
  policy = jsonencode({
    Statement = [
      {
        Resource = "*"
        Effect = "Allow"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_unresolved_action" {
  policy = jsonencode({
    Statement = [
      {
        Resource = "*"
        Effect = "Allow"
        Action = "${var.policy_action}"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_secure_action" {
  policy = jsonencode({
    Statement = [
      {
        Resource = "*"
        Effect = "Allow"
        Action = "foo:bar"
      }
    ]
  })
}


resource "aws_iam_policy" "non_compliant_single_insecure_action" {
  policy = jsonencode({
    Statement = [
      {
        Resource = "*" # Noncompliant
#                  ^^^
        Effect = "Allow"
#                ^^^^^^^<
        Action = [
          "foo:bar",
          "iam:CreatePolicyVersion"
#         ^^^^^^^^^^^^^^^^^^^^^^^^^<
        ]
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy3" {
  policy = jsonencode({
    Statement = [
      {
        NotResource = "foo" # Compliant
        Effect = "Deny"
        Action = "iam:CreatePolicyVersion"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy3" {
  policy = jsonencode({
    Statement = [
      {
        NotResource = "*"
        Effect = "Allow" # Compliant
        Action = "iam:CreatePolicyVersion"
      }
    ]
  })
}

resource "aws_iam_role" "compliant_policy4" {
  inline_policy {
    policy = jsonencode({
      Statement = [
        {
          Resource = "foo" # Compliant
          Effect = "Allow"
          Action = "iam:CreatePolicyVersion"
        },
      ]
    })
  }
}

resource "aws_kms_key" "a_key" { # The resource type for KMS keys
  description             = "KMS key "
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [ # The key policy statement
      {
        Action = [
          "kms:*"
        ]
        Principal = {
          "AWS": [
            "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
          ]
        }
        Effect   = "Allow"
        Resource = "*" # Compliant
      }
    ]
  })
}

resource "aws_iam_policy" "step_function" {
  name        = "Step-Policy"
  description = "Policy for Step Function"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:DescribeLogGroups", // Requires a wildcard following the AWS doc
          "logs:CreateLogDelivery"
        ]
        Resource = "*" // Should be compliant because of the Action above
      }
    ]
  })
}

# === SONARIAC-1800: POLICY heredoc — verify rule still sees policies passed through HeredocLiteralTree ===
# JSON nodes inside the heredoc body have precise text ranges, so the issue lands on the Resource value's
# line inside the body. The `# Noncompliant@+N` markers count forward from where they sit to that body line.

resource "aws_iam_policy" "noncompliant_heredoc_wildcard_resource" {
  # Noncompliant@+7 {{Make sure granting access to all resources is safe here.}}
  policy = <<POLICY
{
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "iam:CreatePolicyVersion",
      "Resource": "*"
    }
  ]
}
POLICY
}

resource "aws_iam_policy" "noncompliant_indented_heredoc_not_resource" {
  # Indented <<- form with NotResource = "*" + Deny — exercises the second insecure branch
  # Noncompliant@+7 {{Make sure granting access to all resources is safe here.}}
  policy = <<-POLICY
    {
      "Statement": [
        {
          "Effect": "Deny",
          "Action": "iam:CreatePolicyVersion",
          "NotResource": "*"
        }
      ]
    }
  POLICY
}

resource "aws_iam_policy" "compliant_heredoc_specific_resource" {
  # Compliant: a non-wildcard Resource keeps the policy safe
  policy = <<POLICY
{
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "iam:CreatePolicyVersion",
      "Resource": "arn:aws:iam::123456789012:policy/MyPolicy"
    }
  ]
}
POLICY
}
