resource "aws_s3_bucket" "mycompliants6281" {
  bucket = "mycompliants6281myname"
}

resource "aws_s3_bucket_public_access_block" "mycompliants6281_publicaccess" {
  bucket = aws_s3_bucket.mycompliants6281.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket" "mynoncompliantfirstbuckets6281default" { # Noncompliant
  bucket = "mynoncompliantfirstbuckets6281defaultname"
}

resource "aws_s3_bucket" "mynoncompliantfirstbuckets6281" {  # Noncompliant
  bucket = "mynoncompliantfirstbuckets6281name"
}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6281_publicaccess_1" {
  bucket = aws_s3_bucket.mynoncompliantfirstbuckets6281.id

  block_public_acls       = false # secondary location
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket" "mynoncompliantsecondbuckets6281" {  # Noncompliant
  bucket = "mynoncompliantsecondbuckets6281name"
}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6281_publicaccess_2" {
  bucket = aws_s3_bucket.mynoncompliantsecondbuckets6281.id

  block_public_acls       = true
  block_public_policy     = false # secondary location
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket" "mynoncompliantthirdbuckets6281" {  # Noncompliant
  bucket = "mynoncompliantthirdbuckets6281name"
}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6281_publicaccess_3" {
  bucket = aws_s3_bucket.mynoncompliantthirdbuckets6281.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = false # secondary location
  restrict_public_buckets = true
}

resource "aws_s3_bucket" "mynoncompliantfourthbuckets6281" {  # Noncompliant
  bucket = "mynoncompliantfourthbuckets6281name"
}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6281_publicaccess_4" {
  bucket = "mynoncompliantfourthbuckets6281name"

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = false # secondary location
}

resource "aws_s3_bucket" "mynoncompliantmissingbuckets6234" {  # Noncompliant
  #      ^^^^^^^^^^^^^^^
  bucket = "mynoncompliantmissingbuckets6234name"
}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6234_missing" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{"restrict_public_buckets" setting is missing.}}
  bucket = "mynoncompliantmissingbuckets6234name"

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
}

resource "aws_s3_bucket" "mynoncompliantmultimissingbuckets61234" {  # Noncompliant
  #      ^^^^^^^^^^^^^^^
  bucket = "mynoncompliantmultimissingbuckets61234name"
}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6231_multiple_missing" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{"ignore_public_acls", "restrict_public_buckets" settings are missing.}}
  bucket = "mynoncompliantmultimissingbuckets61234name"

  block_public_acls       = true
  block_public_policy     = true
}

data "aws_ebs_volume" "not_a_resource" {

}

resource "aws_s3_bucket_public_access_block" "invalid_bucket_identifier" {
  bucket = ["mynoncompliantmultimissingbuckets61234name"]

  block_public_acls       = true
  block_public_policy     = true
}

resource {}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6281_invalid_attr_access" {
  bucket = aws_s3_bucket.mynoncompliantthirdbuckets6281
}

resource "aws_s3_bucket" {  # Noncompliant
  bucket = "mynoncompliantmissing6212name"
}
