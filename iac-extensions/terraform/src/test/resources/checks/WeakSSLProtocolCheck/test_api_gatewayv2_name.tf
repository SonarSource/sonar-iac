resource "aws_apigatewayv2_domain_name" "weak_ssl_protocol" {
  domain_name = "api.example.com"
  domain_name_configuration {
    security_policy = "TLS_1_0" # Noncompliant {{Change this configuration to use a stronger protocol.}}
  }
}

resource "aws_apigatewayv2_domain_name" "no_security_policy" {
  domain_name = "api.example.com"
  # Noncompliant@+1 {{Omitting security_policy disables traffic encryption. Make sure it is safe here.}}
  domain_name_configuration {
# ^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

# Noncompliant@+1 {{Omitting domain_name_configuration.security_policy disables traffic encryption. Make sure it is safe here.}}
resource "aws_apigatewayv2_domain_name" "no_domain_name_configuration" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  domain_name = "api.example.com"
}

resource "aws_apigatewayv2_domain_name" "strong_ssl_protocol" {
  domain_name = "api.example.com"
  domain_name_configuration {
    security_policy = "TLS_1_2"
  }
}

resource "random_resource" "example" {
}
