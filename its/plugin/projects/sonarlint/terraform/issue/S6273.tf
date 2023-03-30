resource "aws_s3_bucket" "myawsbucket" {
  tags = {
    "anycompany:cost-center" = ""
  }
}
