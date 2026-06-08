resource "aws_api_gateway_method" "restapimethodnoncompliant" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^> {{Related method}}
  authorization = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^
  http_method   = "POST"
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

# Gate 1: dangerous method raises
resource "aws_api_gateway_method" "gate1_put_noncompliant" {
  authorization = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^
  http_method   = "PUT"
}

resource "aws_api_gateway_method" "gate1_delete_noncompliant" {
  authorization = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^
  http_method   = "DELETE"
}

resource "aws_api_gateway_method" "gate1_patch_noncompliant" {
  authorization = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^
  http_method   = "PATCH"
}

resource "aws_api_gateway_method" "gate1_any_noncompliant" {
  authorization = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^
  http_method   = "ANY"
}

# Gate 1: GET suppresses
resource "aws_api_gateway_method" "gate1_get_compliant" {
  authorization = "NONE" # Compliant: read-only method
  http_method   = "GET"
}

# Gate 1: HEAD suppresses
resource "aws_api_gateway_method" "gate1_head_compliant" {
  authorization = "NONE" # Compliant: read-only method
  http_method   = "HEAD"
}

# Gate 1: dynamic method suppresses
resource "aws_api_gateway_method" "gate1_dynamic_compliant" {
  authorization = "NONE" # Compliant: method not a literal
  http_method   = var.http_method
}

# Gate 1: missing http_method suppresses
resource "aws_api_gateway_method" "gate1_absent_compliant" {
  authorization = "NONE" # Compliant: method absent
}

# Stage 2 exclusion: bootstrap name suppresses even with dangerous method
resource "aws_api_gateway_method" "loginMethod" {
  authorization = "NONE" # Compliant: name matches bootstrap exclusion
  http_method   = "POST"
}

resource "aws_api_gateway_method" "healthcheck_endpoint" {
  authorization = "NONE" # Compliant: name matches bootstrap exclusion
  http_method   = "POST"
}

resource "aws_api_gateway_method" "statusCheck" {
  authorization = "NONE" # Compliant: name matches bootstrap exclusion
  http_method   = "DELETE"
}

# Stage 2: sensitive name overrides bootstrap exclusion
resource "aws_api_gateway_method" "admin_login_method" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^> {{Related method}}
  authorization = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^
  http_method   = "GET"
}

# Gate 2: sensitive name raises even with GET
resource "aws_api_gateway_method" "admin_api_method" {
  authorization = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^
  http_method   = "GET"
}

resource "aws_api_gateway_method" "internal_endpoint" {
  authorization = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^
  http_method   = "GET"
}

# Gate 2: neutral name + GET suppresses
resource "aws_api_gateway_method" "neutral_get_compliant" {
  authorization = "NONE" # Compliant: GET method, neutral name
  http_method   = "GET"
}

# Gate 2: management keyword raises even with GET
resource "aws_api_gateway_method" "management_api_method" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^> {{Related method}}
  authorization = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^
  http_method   = "GET"
}

# Gate 1 fires on v2 HTTP route with POST and absent auth
# Noncompliant@+1 {{Make sure creating a public API is safe here.}}
resource "aws_apigatewayv2_route" "api_gateway_v2_auth_type_absent" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^
  api_id    = aws_apigatewayv2_api.api_gateway_v2_http.id
  route_key = "POST /example"
}

resource "aws_apigatewayv2_api" "api_gateway_v2_http" {
  name                       = "example-http-api"
  protocol_type              = "HTTP"
#                              ^^^^^^< {{Related API}}
  route_selection_expression = "$request.body.action"
}

resource "aws_apigatewayv2_route" "api_gateway_v2_auth_type_none_post" {
  api_id             = aws_apigatewayv2_api.api_gateway_v2_http_none.id
# Noncompliant@+1 {{Make sure creating a public API is safe here.}}
  authorization_type = "NONE"
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  route_key          = "POST /example"
}

resource "aws_apigatewayv2_api" "api_gateway_v2_http_none" {
  name                       = "example-http-api"
  protocol_type              = "HTTP"
#                              ^^^^^^< {{Related API}}
  route_selection_expression = "$request.body.action"
}

# Gate 1: GET suppresses on v2 HTTP route
resource "aws_apigatewayv2_route" "v2_http_get_compliant" {
  api_id             = aws_apigatewayv2_api.api_gateway_v2_http_none.id
  authorization_type = "NONE" # Compliant: read-only method
  route_key          = "GET /example"
}

# Gate 1: $default suppresses on v2 HTTP route
resource "aws_apigatewayv2_route" "v2_http_default_compliant" {
  api_id    = aws_apigatewayv2_api.api_gateway_v2_http_none.id
  route_key = "$default" # Compliant: no statically known method
}

# Stage 2 exclusion: bootstrap name suppresses on v2 HTTP route
resource "aws_apigatewayv2_route" "login_v2_route" {
  api_id             = aws_apigatewayv2_api.api_gateway_v2_http_none.id
  authorization_type = "NONE" # Compliant: name matches bootstrap exclusion
  route_key          = "POST /login"
}

# Stage 2: sensitive name overrides bootstrap exclusion on v2 HTTP route
resource "aws_apigatewayv2_route" "admin_login_v2_route" {
  api_id             = aws_apigatewayv2_api.api_gateway_v2_http_none.id
  authorization_type = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  route_key          = "GET /items"
}

# Gate 2: sensitive name raises on v2 HTTP route with GET
resource "aws_apigatewayv2_route" "admin_v2_route" {
  api_id             = aws_apigatewayv2_api.api_gateway_v2_http_none.id
  authorization_type = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  route_key          = "GET /items"
}

# Gate 2: sensitive path segment in route key raises with neutral resource name
resource "aws_apigatewayv2_route" "route_sensitive_path_detection" {
  api_id             = aws_apigatewayv2_api.api_gateway_v2_http_none.id
  authorization_type = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  route_key          = "GET /internal/data"
}

# Gate 2: admin-portal splits on hyphen, matching admin
resource "aws_apigatewayv2_route" "admin_portal_route" {
  api_id             = aws_apigatewayv2_api.api_gateway_v2_http_none.id
  authorization_type = "NONE" # Noncompliant {{Make sure creating a public API is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  route_key          = "GET /admin-portal/data"
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

# Stage 2 exclusion: bootstrap name suppresses WebSocket $connect
resource "aws_apigatewayv2_api" "aws_apigatewayv2_api_websocket_login" {
  name                       = "example-websocket-api"
  protocol_type              = "WEBSOCKET"
  route_selection_expression = "$request.body.action"
}

resource "aws_apigatewayv2_route" "login_websocket_connect_compliant" {
  api_id    = aws_apigatewayv2_api.aws_apigatewayv2_api_websocket_login.id
  route_key = "$connect" # Compliant: name matches bootstrap exclusion
}
