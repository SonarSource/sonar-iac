resource "aws_api_gateway_method" "restapimethodnoncompliant" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^> {{Related method}}
  authorization = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
  #               ^^^^^^
}

resource "aws_api_gateway_method" "restapimethodcompliant1" {
  authorization = "AWS_IAM"
}

resource "aws_api_gateway_method" "restapimethodcompliant2" {
  name = "mycompliantapigateway"
}

resource "unrelated_resource_type" "unrelatedresource" {
  authorization = "NONE"
}
