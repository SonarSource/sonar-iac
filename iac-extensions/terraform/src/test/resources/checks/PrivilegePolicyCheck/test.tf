resource "aws_iam_policy" "non_compliant_policy1" {
  policy = jsonencode({
    Statement = [
      {
        Action = "*" # Noncompliant {{Make sure granting all privileges is safe here.}}
#                ^^^
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
        Action = [
          "*" # Noncompliant {{Make sure granting all privileges is safe here.}}
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
        NotAction = "*" # Noncompliant {{Make sure granting all privileges is safe here.}}
#                   ^^^
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
        NotAction = [
          "*" # Noncompliant {{Make sure granting all privileges is safe here.}}
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
          Action = "*" # Noncompliant {{Make sure granting all privileges is safe here.}}
#                  ^^^
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
        Action = "foo" # Compliant
        Effect = "Allow"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy2" {
  policy = jsonencode({
    Statement = [
      {
        Action = "*"
        Effect = "Deny" # Compliant
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy3" {
  policy = jsonencode({
    Statement = [
      {
        Action = "*" # Compliant
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
        NotAction = "foo" # Compliant
        Effect = "Deny"
      }
    ]
  })
}

resource "aws_iam_policy" "non_compliant_policy3" {
  policy = jsonencode({
    Statement = [
      {
        NotAction = "*"
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
          Action = "foo" # Compliant
          Effect = "Allow"
        },
      ]
    })
  }
}
