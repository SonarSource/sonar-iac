resource "aws_iam_policy" "non_compliant_policy1" {
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = [ "iam:CreatePolicyVersion" ]
        Resource = "*" # Noncompliant {{Narrow these permissions to a smaller set of resources to avoid privilege escalation.}}
#                  ^^^
      }
    ]
  })
}

resource "aws_iam_policy" "non_compliant_policy2" {
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = [ "iam:CreatePolicyVersion" ]
        Resource = "arn:foo:bar:baz:bax:user/*" # Noncompliant
#                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      }
    ]
  })
}

resource "aws_iam_policy" "non_compliant_policy2" {
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = [ "lambda:*" ]
        Resource = "arn:foo:bar:baz:bax:user/*" # Noncompliant
#                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy1" {
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

resource "aws_iam_policy" "compliant_policy2" {
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = [ "foo" ]
        Resource = "arn:foo:bar:baz:bax:user/*"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy3" {
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = [ "iam:CreatePolicyVersion" ]
        Resource = "foo"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy4" {
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = [ "iam:CreatePolicyVersion" ]
        Resource = "*"
        Condition = "foo"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy5" {
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = [ "iam:CreatePolicyVersion" ]
        Resource = "*"
        Principal = "foo"
      }
    ]
  })
}

resource "aws_iam_policy" "compliant_policy6" {
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = "foo"
        Resource = "*"
      }
    ]
  })
}
