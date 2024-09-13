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

# Noncompliant@+1 {{Make sure creating a public API is safe here.}}
resource "aws_apigatewayv2_route" "api_gateway_v2_auth_type_absent" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^
  api_id    = aws_apigatewayv2_api.api_gateway_v2_http.id
  route_key = "GET /example"
}

resource "aws_apigatewayv2_api" "api_gateway_v2_http" {
  name                       = "example-http-api"
  protocol_type              = "HTTP"
#                              ^^^^^^< {{Related API}}
  route_selection_expression = "$request.body.action"
}

resource "aws_apigatewayv2_route" "api_gateway_v2_auth_type_none" {
  api_id             = aws_apigatewayv2_api.api_gateway_v2_http_none.id
# Noncompliant@+1 {{Make sure creating a public API is safe here.}}
  authorization_type = "NONE"
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  route_key          = "GET /example"
}

resource "aws_apigatewayv2_api" "api_gateway_v2_http_none" {
  name                       = "example-http-api"
  protocol_type              = "HTTP"
#                              ^^^^^^< {{Related API}}
  route_selection_expression = "$request.body.action"
}

# Noncompliant@+1 {{Make sure creating a public API is safe here.}}
resource "aws_apigatewayv2_route" "aws_apigatewayv2_route_noncompliant" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^
  api_id    = aws_apigatewayv2_api.aws_apigatewayv2_api_noncompliant.id
  route_key = "$connect"
#             ^^^^^^^^^^< {{Related route_key}}
}

resource "aws_apigatewayv2_api" "aws_apigatewayv2_api_noncompliant" {
  name                       = "example-websocket-api"
  protocol_type              = "WEBSOCKET"
#                              ^^^^^^^^^^^< {{Related API}}
  route_selection_expression = "$request.body.action"
}

# Compliant; `route_key` is `message` and the related API  has a `protocol_type` equal to `WEBSOCKET`:
resource "aws_apigatewayv2_api" "aws_apigatewayv2_api_compliant" {
  name                       = "example-websocket-api"
  protocol_type              = "WEBSOCKET"
  route_selection_expression = "$request.body.action"
}

resource "aws_apigatewayv2_route" "aws_apigatewayv2_route_compliant" {
  api_id    = aws_apigatewayv2_api.aws_apigatewayv2_api_compliant.id
  route_key = "message"
}
