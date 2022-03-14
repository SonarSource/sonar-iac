resource "aws_api_gateway_domain_name" "weak_ssl_protocol" {
  domain_name = "api.example.com"
  security_policy = "TLS_1_0" # Noncompliant {{Change this code to disable support of older TLS versions.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Set "security_policy" to disable support of older TLS versions.}}
resource "aws_api_gateway_domain_name" "no_security_policy" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  domain_name = "api.example.com"
}

resource "aws_api_gateway_domain_name" "strong_ssl_protocol" {
  domain_name = "api.example.com"
  security_policy = "TLS_1_2"
}

resource "random_resource" "example" {
}
