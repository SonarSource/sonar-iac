# Noncompliant@+1
resource "aws_s3_bucket" "bucket_with_unrelated_acl" {
  bucket = "bucket_with_unrelated_acl_name"
  acl    = "xxx"
}

# Noncompliant@+1
resource "aws_s3_bucket" "mycompliantbuckets6258" {
  bucket = "mycompliantbuckets6258name"

  logging {
      target_bucket = "mycompliantloggingbuckets6258name"
      target_prefix = "log/"
  }
}

# -------
# Noncompliant@+1
resource "aws_s3_bucket" "noncompliant_bucket" {
  bucket = "noncompliant_bucket"
}

# -------

resource "aws_s3_bucket" "bucket_with_bucket_logging" {
  bucket = "example"  # Compliant: aws_s3_bucket_logging resource exists
}

resource "aws_s3_bucket_logging" "bucket_logging_1" {
  bucket = aws_s3_bucket.bucket_with_bucket_logging.id
}

# -------
resource "aws_s3_bucket" "bucket_with_target_bucket_logging" {
  bucket = "example_logstorage"  # Compliant: a bucket policy is set to make this a logging bucket
}

resource "aws_s3_bucket_logging" "bucket_logging_2" {
  target_bucket = aws_s3_bucket.bucket_with_target_bucket_logging.id
  target_prefix = "testing-logs"
}

# -------

resource "aws_s3_bucket" "bucket_with_policy" {
  bucket = "example_logstorage"  # Compliant: a bucket policy is set to make this a logging bucket
}

data "aws_iam_policy_document" "policy_document" {
  statement {
    principals {
      type        = "Service"
      identifiers = ["logging.s3.amazonaws.com"]
    }
  }
}

resource "aws_s3_bucket_policy" "bucket_policy_1" {
  bucket = aws_s3_bucket.bucket_with_policy.id
  policy = data.aws_iam_policy_document.policy_document.json
}

# -------

# Noncompliant@+1
resource "aws_s3_bucket" "bucket_with_policy_wrong_type" {
  bucket = "example_logstorage"
}

data "aws_iam_policy_document" "policy_document_wrong_type" {
  statement {
    principals {
      type        = "NOT-Service" # type is "NOT-Service"
      identifiers = ["logging.s3.amazonaws.com"]
    }
  }
}

resource "aws_s3_bucket_policy" "bucket_policy_2" {
  bucket = aws_s3_bucket.bucket_with_policy_wrong_type.id
  policy = data.aws_iam_policy_document.policy_document_wrong_type.json
}

# -------

# Noncompliant@+1
resource "aws_s3_bucket" "bucket_with_policy_wrong_identifier" {
  bucket = "example_logstorage"
}

data "aws_iam_policy_document" "policy_document_wrong_identifier" {
  statement {
    principals {
      type        = "Service"
      identifiers = ["not.logging.s3.amazonaws.com"]  # do not contain "logging.s3.amazonaws.com"
    }
  }
}

resource "aws_s3_bucket_policy" "bucket_policy_2" {
  bucket = aws_s3_bucket.bucket_with_policy_wrong_identifier.id
  policy = data.aws_iam_policy_document.policy_document_wrong_identifier.json
}

# -------

# Noncompliant@+1
resource "aws_s3_bucket" "bucket_with_policy_no_identifiers" {
  bucket = "example_logstorage"
}

data "aws_iam_policy_document" "policy_document_no_identifiers" {
  statement {
    principals {
      type        = "Service"
      # identifiers = []  # do not contain "logging.s3.amazonaws.com"
    }
  }
}

resource "aws_s3_bucket_policy" "bucket_policy_2" {
  bucket = aws_s3_bucket.bucket_with_policy_no_identifiers.id
  policy = data.aws_iam_policy_document.policy_document_no_identifiers.json
}

# ------ for coverage

resource "aws_s3_bucket_policy" "no_policy" {
  bucket = aws_s3_bucket.bucket_with_policy.id
}

resource "aws_s3_bucket_policy" "no_bucket" {
  policy = data.aws_iam_policy_document.policy_document.json
}

resource "aws_s3_bucket_policy" "invalid_policy" {
  bucket = aws_s3_bucket.bucket_with_policy.id
  policy = resource.aws_iam_policy_document.policy_document.json
}

resource "aws_s3_bucket_policy" "bucket_on_id_suffix" {
  bucket = aws_s3_bucket.bucket_with_policy
  policy = data.aws_iam_policy_document.policy_document.json
}

resource "aws_s3_bucket_policy" "policy_no_json_suffix" {
  bucket = aws_s3_bucket.bucket_with_policy.id
  policy = data.aws_iam_policy_document.policy_document
}

resource "aws_s3_bucket_policy" "bucket_not_bucket" {
  bucket = not_aws_s3_bucket.bucket_with_policy.id
  policy = data.aws_iam_policy_document.policy_document.json
}

resource "aws_s3_bucket_policy" "invalid_policy_value" {
  bucket = aws_s3_bucket.bucket_with_policy.id
  policy = data
}

resource "aws_s3_bucket_policy" "invalid_policy_string" {
  bucket = aws_s3_bucket.bucket_with_policy.id
  policy = "foo"
}

resource "aws_s3_bucket_logging" "invalid_bucket_logging_no_id_suffix" {
  bucket = aws_s3_bucket.bucket_with_bucket_logging
}

resource "aws_s3_bucket_logging" "invalid_bucket_logging_no_bucket_name" {
  bucket = aws_s3_bucket
}
