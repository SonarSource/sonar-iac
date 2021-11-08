resource "aws_api_gateway_domain_name" "weak_ssl_protocol" {
  domain_name = "api.example.com"
  security_policy = "TLS_1_0" # Noncompliant {{Change this code to use a stronger protocol.}}
  #                 ^^^^^^^^^
}

resource "aws_api_gateway_domain_name" "no_security_policy" { # Noncompliant
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  domain_name = "api.example.com"
}

resource "aws_api_gateway_domain_name" "strong_ssl_protocol" {
  domain_name = "api.example.com"
  security_policy = "TLS_1_2"
}

resource "aws_apigatewayv2_domain_name" "weak_ssl_protocol" {
  domain_name = "api.example.com"
  domain_name_configuration {
    security_policy = "TLS_1_0" # Noncompliant
  }
}

resource "aws_apigatewayv2_domain_name" "no_security_policy" {
  domain_name = "api.example.com"
  domain_name_configuration { # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_apigatewayv2_domain_name" "no_domain_name_configuration" { # Noncompliant
      #  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  domain_name = "api.example.com"
}

resource "aws_apigatewayv2_domain_name" "strong_ssl_protocol" {
  domain_name = "api.example.com"
  domain_name_configuration {
    security_policy = "TLS_1_2"
  }
}

resource "aws_elasticsearch_domain" "elastic-no_domain_endpoint_options" {  # Noncompliant
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^
  domain_name = "api.example.com"
}

resource "aws_elasticsearch_domain" "elastic_no_policy" {
  domain_name = "api.example.com"

  domain_endpoint_options { # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_elasticsearch_domain" "elastic_weak_policy" {
  domain_name = "api.example.com"

  domain_endpoint_options {
    tls_security_policy = "Policy-Min-TLS-1-0-2019-07"  # Noncompliant
    #                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_elasticsearch_domain" "elastic_strong_policy" {
  domain_name = "api.example.com"

  domain_endpoint_options {
    tls_security_policy = "Policy-Min-TLS-1-2-2019-07"
  }
}

resource "random_resource" "example" {
}
