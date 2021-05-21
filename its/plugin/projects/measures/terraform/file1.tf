// comment

provider "aws" {
  region = "us-east-1"
}

locals {
  test_description = "spin up EC2 in default VPC"
  test_name        = "TestDisallowDefaultVpcRule test - use case 1"
  cidr_block       = "10.10.0.0/16"
  region  = "us-east-1"
  /* comment
  */
}

/*
multiline
comment

*/

resource "aws_default_vpc" "default" {
  tags = {
    Name = "Default VPC" # comment
  }
}
