resource "aws_iam_role_policy" "non_compliant_policy1" {
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Resource = "*" # Noncompliant
        Action   = "cases:UpdateCase"
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
        Action   = "networkmanager:GetNetworkTelemetry"
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
        Action   = "cases:UpdateCase"
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
        Action   = "networkmanager:GetNetworkTelemetry"
      }
    ]
  })
}
