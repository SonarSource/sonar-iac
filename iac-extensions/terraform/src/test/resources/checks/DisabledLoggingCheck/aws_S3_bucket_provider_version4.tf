resource "aws_s3_bucket" "bucket_with_unrelated_acl" {
  bucket = "bucket_with_unrelated_acl_name"
  acl    = "xxx"
}

resource "aws_s3_bucket" "mycompliantbuckets6258" {
  bucket = "mycompliantbuckets6258name"

  logging {
      target_bucket = "mycompliantloggingbuckets6258name"
      target_prefix = "log/"
  }
}
