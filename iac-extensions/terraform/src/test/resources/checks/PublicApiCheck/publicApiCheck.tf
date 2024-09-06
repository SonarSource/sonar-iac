resource "aws_api_gateway_method" "restapimethodnoncompliant" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^> {{Related method}}
  authorization = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^
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

resource "aws_apigatewayv2_api" "api_gateway_v2_http" {
  name                       = "example-http-api"
  protocol_type              = "HTTP"
  route_selection_expression = "$request.body.action"
}

resource "aws_apigatewayv2_route" "api_gateway_v2_auth_type_absent" {
  api_id    = aws_apigatewayv2_api.api_gateway_v2_http.id
  route_key = "GET /example"
}

resource "aws_apigatewayv2_api" "api_gateway_v2_http_none" {
  name                       = "example-http-api"
  protocol_type              = "HTTP"
  route_selection_expression = "$request.body.action"
}

resource "aws_apigatewayv2_route" "api_gateway_v2_auth_type_none" {
  api_id             = aws_apigatewayv2_api.api_gateway_v2_http_none.id
  authorization_type = "NONE"
  route_key          = "GET /example"
}
