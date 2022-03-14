# Noncompliant@+1 {{Set "domain_endpoint_options.tls_security_policy" to disable support of older TLS versions.}}
resource "aws_elasticsearch_domain" "elastic-no_domain_endpoint_options" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^
  domain_name = "api.example.com"
}

resource "aws_elasticsearch_domain" "elastic_no_policy" {
  domain_name = "api.example.com"

  # Noncompliant@+1 {{Set "tls_security_policy" to disable support of older TLS versions.}}
  domain_endpoint_options {
# ^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_elasticsearch_domain" "elastic_weak_policy" {
  domain_name = "api.example.com"

  domain_endpoint_options {
    tls_security_policy = "Policy-Min-TLS-1-0-2019-07" # Noncompliant {{Change this code to disable support of older TLS versions.}}
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
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
