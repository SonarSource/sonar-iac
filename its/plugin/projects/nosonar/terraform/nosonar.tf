resource "aws_s3_bucket" "myawsbucket" {
  tags = {
    "anycompany:CostCenter" = "" # NOSONAR
    "anycompany:CostCenter" = "" # raise issue S6273
  }
}
