# Noncompliant@+1 {{Omitting logging_config makes logs incomplete. Make sure it is safe here.}}
resource "aws_cloudfront_distribution" "cloudfront_distribution" {
      #  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  default_root_object = "index.html"
}

resource "aws_cloudfront_distribution" "cloudfront_distribution" {
  default_root_object = "index.html"
  logging_config {
    bucket          = "mycompliantbucketname"
    prefix          = "log/cloudfront-"
  }
}

resource "non_aws_cloudfront_distribution" "for_coverage" {
}
