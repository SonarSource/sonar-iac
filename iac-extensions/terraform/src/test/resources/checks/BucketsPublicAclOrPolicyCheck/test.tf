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

# Noncompliant@+1 {{No Public Access Block configuration prevents public ACL/policies to be set on this S3 bucket. Make sure it is safe here.}}
resource "aws_s3_bucket" "mynoncompliantfirstbuckets6281default" {
  bucket = "mynoncompliantfirstbuckets6281defaultname"
}

resource "aws_s3_bucket" "mynoncompliantfirstbuckets6281" {
  #      ^^^^^^^^^^^^^^^> {{Related bucket}}
  bucket = "mynoncompliantfirstbuckets6281name"
}

# Noncompliant@+1 {{Make sure allowing public ACL/policies to be set is safe here.}}
resource "aws_s3_bucket_public_access_block" "mynoncompliants6281_publicaccess_1" {
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

# Noncompliant@+1 {{Make sure allowing public ACL/policies to be set is safe here.}}
resource "aws_s3_bucket_public_access_block" "mynoncompliants6234_missing" {
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

# Noncompliant@+1 {{Make sure allowing public ACL/policies to be set is safe here.}}
resource "aws_s3_bucket_public_access_block" "mynoncompliants6231_multiple_missing" {
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

# Example with `count` meta-argument and indexing with `count.index`
resource "aws_s3_bucket" "mycompliants6281" {
  count = 2 # Defines that 2 needs to be deployed
  bucket = "mycompliants6281myname"
}

resource "aws_s3_bucket_public_access_block" "mycompliants6281_publicaccess" {
  count = 2
  bucket = aws_s3_bucket.mycompliants6281[count.index].id # References the right bucket ID through this "count.index"

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Non compliant example with `count` meta-argument and indexing with `count.index`
resource "aws_s3_bucket" "mynoncompliants6281" {
#        ^^^^^^^^^^^^^^^>
  count = 2
  bucket = "mycompliants6281myname"
}

resource "aws_s3_bucket_public_access_block" "mynoncompliants6281_publicaccess" { # Noncompliant
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  count = 2
  bucket = aws_s3_bucket.mynoncompliants6281[count.index].id
}

# Example with `count` meta-argument and indexing with numbers
resource "aws_s3_bucket" "log_bucket_with_count" {
  count  = var.bucket_logs_enabled || var.bucket_trails_enabled? 1 : 0
  bucket = join("-", [local.bucket_name, "logs"])
  force_destroy = true
  tags          = local.tags

}

resource "aws_s3_bucket_public_access_block" "log_bucket_pab" {
  count  = var.bucket_logs_enabled || var.bucket_trails_enabled? 1 : 0
  bucket = aws_s3_bucket.log_bucket_with_count[0].id # Compliant, references correct bucket
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Example with `for_each` meta-argument and indexing with `each.key`
resource "aws_s3_bucket" "mycompliants6281" {
  for_each = local.buckets
  bucket = each.key
  tags = each.value.tags
}

resource "aws_s3_bucket_public_access_block" "mycompliants6281_publicaccess" {
  for_each = local.buckets
  bucket = aws_s3_bucket.mycompliants6281[each.key].id # References the right bucket ID through this "each.key"

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}