resource "aws_s3_bucket" "bucket" { # Noncompliant
  policy = jsonencode({
    Statement = [{
      Effect    = "Allow"
      Principal = "*"
    }]
  })
}
