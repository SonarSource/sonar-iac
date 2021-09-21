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

resource "aws_s3_bucket" "mynoncompliantfirstbuckets6281default" { # Noncompliant {{Make sure not preventing permissive ACL/policies to be set is safe here.}}
  bucket = "mynoncompliantfirstbuckets6281defaultname"
}

resource "aws_s3_bucket" "mynoncompliantfirstbuckets6281" {
  #      ^^^^^^^^^^^^^^^> {{Related bucket}}
  bucket = "mynoncompliantfirstbuckets6281name"
}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6281_publicaccess_1" { # Noncompliant {{Make sure not preventing permissive ACL/policies to be set is safe here.}}
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  bucket = aws_s3_bucket.mynoncompliantfirstbuckets6281.id

  block_public_acls       = false
  #                         ^^^^^< {{Set this property to true}}
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket" "mynoncompliantsecondbuckets6281" {
  bucket = "mynoncompliantsecondbuckets6281name"
}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6281_publicaccess_2" { # Noncompliant
  bucket = aws_s3_bucket.mynoncompliantsecondbuckets6281.id

  block_public_acls       = true
  block_public_policy     = false # secondary location
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket" "mynoncompliantthirdbuckets6281" {
  bucket = "mynoncompliantthirdbuckets6281name"
}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6281_publicaccess_3" { # Noncompliant
  bucket = aws_s3_bucket.mynoncompliantthirdbuckets6281.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = false # secondary location
  restrict_public_buckets = true
}

resource "aws_s3_bucket" "mynoncompliantfourthbuckets6281" {
  bucket = "mynoncompliantfourthbuckets6281name"
}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6281_publicaccess_4" { # Noncompliant
  bucket = "mynoncompliantfourthbuckets6281name"

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = false # secondary location
}

resource "aws_s3_bucket" "mynoncompliantmissingbuckets6234" {
  #      ^^^^^^^^^^^^^^^> {{Related bucket}}
  bucket = "mynoncompliantmissingbuckets6234name"
}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6234_missing" { # Noncompliant {{Make sure not preventing permissive ACL/policies to be set is safe here.}}
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  bucket = "mynoncompliantmissingbuckets6234name"

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
}

resource "aws_s3_bucket" "mynoncompliantmultimissingbuckets61234" {
  #      ^^^^^^^^^^^^^^^> {{Related bucket}}
  bucket = "mynoncompliantmultimissingbuckets61234name"
}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6231_multiple_missing" { # Noncompliant {{Make sure not preventing permissive ACL/policies to be set is safe here.}}
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  bucket = "mynoncompliantmultimissingbuckets61234name"

  block_public_acls       = true
  block_public_policy     = true
}

data "aws_ebs_volume" "not_a_resource" {

}

resource "aws_s3_bucket_public_access_block" "invalid_bucket_identifier" { # Noncompliant
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  bucket = ["mynoncompliantmultimissingbuckets61234name"]

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = false
  #                         ^^^^^< {{Set this property to true}}
  restrict_public_buckets = false
  #                         ^^^^^< {{Set this property to true}}
}

resource {}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6281_invalid_attr_access" { # Noncompliant
  bucket = aws_s3_bucket.mynoncompliantthirdbuckets6281
}

resource "aws_s3_bucket" {  # Noncompliant
  bucket = "mynoncompliantmissing6212name"
}
