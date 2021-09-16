resource "aws_iam_policy" "non_compliant_policy1" {
  policy = jsonencode({
    Statement = [
      {
        Resource = "*" # Noncompliant {{Make sure granting access to all resources is safe here.}}
#                  ^^^
        Effect = "Allow"
#                ^^^^^^^< {{Related effect}}
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
      }
    ]
  })
}

resource "aws_iam_policy" "non_compliant_policy2" {
  policy = jsonencode({
    Statement = [
      {
        NotResource = "*" # Noncompliant {{Make sure granting access to all resources is safe here.}}
#                     ^^^
        Effect = "Deny"
#                ^^^^^^< {{Related effect}}
      }
    ]
  })
}

resource "aws_iam_policy" "non_compliant_policy3" {
  policy = jsonencode({
    Statement = [
      {
        NotResource = [
          "*" # Noncompliant {{Make sure granting access to all resources is safe here.}}
#         ^^^
        ]
        Effect = "Deny"
#                ^^^^^^< {{Related effect}}
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
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy3" {
  policy = jsonencode({
    Statement = [
      {
        Resource = "*" # Compliant
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy4" {
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow" # Compliant
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy5" {
  policy = jsonencode({
    Statement = [
      {
        "Resource" = "foo"
      }
    ]
  })
}

resource "aws_iam_policy" "non_compliant_policy3" {
  policy = jsonencode({
    Statement = [
      {
        NotResource = "foo" # Compliant
        Effect = "Deny"
      }
    ]
  })
}

resource "aws_iam_policy" "non_compliant_policy3" {
  policy = jsonencode({
    Statement = [
      {
        NotResource = "*"
        Effect = "Allow" # Compliant
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
        },
      ]
    })
  }
}
