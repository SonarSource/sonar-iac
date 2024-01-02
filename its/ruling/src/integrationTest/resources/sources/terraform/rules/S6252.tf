resource "aws_s3_bucket" "mynoncompliantbucket" { # Insecure
  bucket = "mynoncompliantbucketname"
}
