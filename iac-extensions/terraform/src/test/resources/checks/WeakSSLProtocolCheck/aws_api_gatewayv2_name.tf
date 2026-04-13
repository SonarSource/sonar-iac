resource "aws_apigatewayv2_domain_name" "weak_ssl_protocol" {
  domain_name = "api.example.com"
  domain_name_configuration {
    security_policy = "TLS_1_0" # Noncompliant {{Change this code to disable support of older TLS versions.}}
  }
}

resource "aws_apigatewayv2_domain_name" "no_security_policy" {
  domain_name = "api.example.com"
  # Noncompliant@+1 {{Set "security_policy" to disable support of older TLS versions.}}
  domain_name_configuration {
# ^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

# Noncompliant@+1 {{Set "domain_name_configuration.security_policy" to disable support of older TLS versions.}}
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

resource "aws_apigatewayv2_domain_name" "strong_tls13_pq" {
  domain_name = "api.example.com"
  domain_name_configuration {
    security_policy = "SecurityPolicy_TLS13_1_2_PQ_2025_09"
  }
}

resource "aws_apigatewayv2_domain_name" "strong_tls13_1_3" {
  domain_name = "api.example.com"
  domain_name_configuration {
    security_policy = "SecurityPolicy_TLS13_1_3_2025_09"
  }
}

resource "aws_apigatewayv2_domain_name" "strong_tls12_pfs" {
  domain_name = "api.example.com"
  domain_name_configuration {
    security_policy = "SecurityPolicy_TLS12_PFS_2025_EDGE"
  }
}

resource "aws_apigatewayv2_domain_name" "strong_tls12_2018" {
  domain_name = "api.example.com"
  domain_name_configuration {
    security_policy = "SecurityPolicy_TLS12_2018_EDGE"
  }
}

resource "random_resource" "example" {
}
