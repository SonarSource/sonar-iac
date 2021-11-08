resource "aws_elasticsearch_domain" "noncompliant_enabled_false" {
  domain_endpoint_options {
    enforce_https = false # Noncompliant {{Using HTTP protocol is insecure. Use HTTPS instead}}
  }

  node_to_node_encryption {
    enabled = false # Noncompliant {{Communication among nodes of a cluster should be encrypted}}
  }
}

resource "aws_elasticsearch_domain" "noncompliant_no_node_to_node_encryption" { # Noncompliant {{Communication among nodes of a cluster should be encrypted}}
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^
  domain_endpoint_options {
    enforce_https = true
  }
}

resource "aws_elasticsearch_domain" "compliant" {
  domain_endpoint_options {
    enforce_https = true
  }

  node_to_node_encryption {
    enabled = true
  }
}
