resource "aws_iam_policy" "non_compliant_policy1" { # Noncompliant {{This policy is vulnerable to the "Create Policy Version" privilege escalation vector. Remove permissions or restrict the set of resources they apply to.}}
  #      ^^^^^^^^^^^^^^^^
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = [ "iam:CreatePolicyVersion" ]
        #          ^^^^^^^^^^^^^^^^^^^^^^^^^< {{This permission enables the "Create Policy Version" escalation vector.}}
        Resource = "*"
        #          ^^^< {{Permissions are granted on all resources.}}
      }
    ]
  })
}

resource "aws_iam_policy" "non_compliant_policy2" { # Noncompliant
  #      ^^^^^^^^^^^^^^^^
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = [ "iam:CreatePolicyVersion" ]
        #          ^^^^^^^^^^^^^^^^^^^^^^^^^<
        Resource = "arn:foo:bar:baz:bax:user/*"
      }
    ]
  })
}

resource "aws_iam_policy" "non_compliant_policy2" { # Noncompliant {{This policy is vulnerable to the "Update Lambda code" privilege escalation vector. Remove permissions or restrict the set of resources they apply to.}}
  #      ^^^^^^^^^^^^^^^^
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = [ "lambda:*" ]
        #          ^^^^^^^^^^< {{This permission enables the "Update Lambda code" escalation vector.}}
        Resource = "arn:foo:bar:baz:bax:user/*"
      }
    ]
  })
}

resource "aws_iam_policy" "non_compliant_policy3" { # Noncompliant {{This policy is vulnerable to the "Put Role Policy" privilege escalation vector. Remove permissions or restrict the set of resources they apply to.}}
  #      ^^^^^^^^^^^^^^^^
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = [ "iam:PutRolePolicy", "sts:AssumeRole" ]
        #          ^^^^^^^^^^^^^^^^^^^< ^^^^^^^^^^^^^^^^< {{When combined with others, this permission enables the "Put Role Policy" escalation vector.}}
        Resource = "*"
        #          ^^^< {{Permissions are granted on all resources.}}
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

resource "aws_iam_policy" "non_compliant_policy7" {
  policy = jsonencode({
    Statement = [
      {
        Effect = "Allow"
        Action = [ "sts:AssumeRole" ]
        Resource = "*"
      }
    ]
  })
}

resource "non_aws_iam_policy" "coverage" {
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
