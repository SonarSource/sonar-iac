resource "aws_iam_policy" "non_compliant_policy" {
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = [ "iam:CreatePolicyVersion" ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy" {
  policy = jsonencode({
    Statement = [
      {
        Effect = "Deny"
        Action = [ "iam:CreatePolicyVersion" ]
        Resource = "arn:foo:bar:baz:bax:user/*"
      }
    ]
  })
}
