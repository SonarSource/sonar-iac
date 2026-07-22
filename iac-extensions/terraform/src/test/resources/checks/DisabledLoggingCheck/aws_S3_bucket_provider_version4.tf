# Noncompliant@+1 {{Make sure that disabling logging is safe here.}}
resource "aws_s3_bucket" "bucket_with_unrelated_acl" {
#        ^^^^^^^^^^^^^^^
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

# -------
variable "enabled" {
  default = true
}

# Compliant: logging resource links to a count-created bucket through an indexed reference.
resource "aws_s3_bucket" "count_logged_bucket" {
  count  = var.enabled ? 1 : 0
  bucket = "count-logged-bucket"
}
resource "aws_s3_bucket_logging" "count_logged_bucket" {
  count         = var.enabled ? 1 : 0
  bucket        = aws_s3_bucket.count_logged_bucket[0].id
  target_bucket = "log-bucket"
  target_prefix = "log/"
}

# Compliant: logging enabled through a bucket policy whose document is an indexed data source.
resource "aws_s3_bucket" "count_policy_logbucket" {
  count  = var.enabled ? 1 : 0
  bucket = "count-policy-logbucket"
}
data "aws_iam_policy_document" "count_log_delivery" {
  count = var.enabled ? 1 : 0
  statement {
    principals {
      type        = "Service"
      identifiers = ["logging.s3.amazonaws.com"]
    }
  }
}
resource "aws_s3_bucket_policy" "count_policy_logbucket" {
  count  = var.enabled ? 1 : 0
  bucket = aws_s3_bucket.count_policy_logbucket[0].id
  policy = data.aws_iam_policy_document.count_log_delivery[0].json
}

# Noncompliant@+1
resource "aws_s3_bucket" "count_bucket_no_logging" {
  count  = var.enabled ? 1 : 0
  bucket = "count-bucket-no-logging"
}

variable "logged_bucket_names" {
  default = ["count-idx-logged-a", "count-idx-logged-b"]
}

# Compliant: multiple buckets created with count, each linked to its own logging resource via count.index.
resource "aws_s3_bucket" "count_idx_logged_bucket" {
  count  = length(var.logged_bucket_names)
  bucket = var.logged_bucket_names[count.index]
}
resource "aws_s3_bucket_logging" "count_idx_logged_bucket" {
  count         = length(var.logged_bucket_names)
  bucket        = aws_s3_bucket.count_idx_logged_bucket[count.index].id
  target_bucket = "log-bucket"
  target_prefix = "log/"
}

variable "logged_bucket_keys" {
  default = ["a", "b"]
}

# Compliant: buckets created with for_each, each linked to its own logging resource via a string key.
resource "aws_s3_bucket" "foreach_logged_bucket" {
  for_each = toset(var.logged_bucket_keys)
  bucket   = "foreach-logged-bucket-${each.key}"
}
resource "aws_s3_bucket_logging" "foreach_logged_bucket" {
  for_each      = toset(var.logged_bucket_keys)
  bucket        = aws_s3_bucket.foreach_logged_bucket[each.key].id
  target_bucket = "log-bucket"
  target_prefix = "log/"
}

# Noncompliant@+1
resource "aws_s3_bucket" "foreach_bucket_no_logging" {
  for_each = toset(var.logged_bucket_keys)
  bucket   = "foreach-bucket-no-logging-${each.key}"
}
