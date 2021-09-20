resource "aws_iam_role_policy" "non_compliant_policy1" {
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Resource = "*" # Noncompliant
      }
    ]
  })
}

resource "aws_iam_role_policy" "non_compliant_policy1" {
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect      = "Deny"
        NotResource = "*" # Noncompliant
      }
    ]
  })
}

resource "aws_iam_role_policy" "compliant_policy1" {
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Resource = "foo" # Compliant
      }
    ]
  })
} 

resource "aws_iam_role_policy" "compliant_policy2" {
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect      = "Deny"
        NotResource = "foo" # Compliant
      }
    ]
  })
}
