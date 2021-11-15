# Noncompliant@+1 {{Omitting domain_endpoint_options.tls_security_policy disables traffic encryption. Make sure it is safe here.}}
resource "aws_elasticsearch_domain" "elastic-no_domain_endpoint_options" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^
  domain_name = "api.example.com"
}

resource "aws_elasticsearch_domain" "elastic_no_policy" {
  domain_name = "api.example.com"

  # Noncompliant@+1 {{Omitting tls_security_policy disables traffic encryption. Make sure it is safe here.}}
  domain_endpoint_options {
# ^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_elasticsearch_domain" "elastic_weak_policy" {
  domain_name = "api.example.com"

  domain_endpoint_options {
    tls_security_policy = "Policy-Min-TLS-1-0-2019-07" # Noncompliant {{Change this configuration to use a stronger protocol.}}
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
