resource "aws_s3_bucket" "mybuckets6245" {
}

resource "no_s3_bucket" "mybuckets1234" {

}

data "aws_s3_bucket" "mydatas1234" {

}

ephemeral "aws_secretsmanager_secret_version" "temporary" {
  secret_id = "example"
}

action "aws_lambda_invoke" "rotate" {
  config {
    function_name = "rotate"
  }
}

resource "aws_wafv2_web_acl" "acl" {
  rule {
    action {
      block {}
    }
  }
}

action "aws_s3_bucket" "same_type_as_a_resource" {
}
