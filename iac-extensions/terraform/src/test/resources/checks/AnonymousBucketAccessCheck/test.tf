resource "aws_s3_bucket_policy" "sensitive_policy_with_anonymous_access_literal" {  # Noncompliant {{Make sure this S3 policy granting anonymous access is safe here.}}
#        ^^^^^^^^^^^^^^^^^^^^^^
  bucket = aws_s3_bucket
  policy = jsonencode({
    Statement = [
      {
        Effect: "Allow",
        Principal: "*",
         #         ^^^< {{Anonymous access.}}
      }
    ]
  })
}

resource "aws_s3_bucket_policy" "sensitive_policy_with_anonymous_access_object_mapping" {  # Noncompliant
#        ^^^^^^^^^^^^^^^^^^^^^^
  bucket = aws_s3_bucket
  policy = jsonencode({
    Statement = [
      {
        Effect: "Allow",
        Principal: {
          "AWS": "*"
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

resource "aws_s3_bucket" "sensitive_bucket_with_anonymous_access_literal" { # Noncompliant
  policy = jsonencode({
    Statement = [{
        Effect    = "Allow"
        Principal = "*"
    }]
  })
}

resource "aws_s3_bucket" "sensitive_bucket_with_anonymous_access_object_mapping" { # Noncompliant
  policy = jsonencode({
    Statement = [{
      Effect    = "Allow"
      Principal = {
        "AWS": "*"
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

resource "aws_s3_bucket" "sensitive_policy_with_object_mapping_principal" { # Noncompliant
#        ^^^^^^^^^^^^^^^
  policy = jsonencode({
    Statement = [{
      Effect    = "Allow"
      Principal = {
        "AWS": [
          "*"
#         ^^^<
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
