# Noncompliant@+1 {{Omitting "encrypt_at_rest.enabled" disables OpenSearch domains encryption. Make sure it is safe here.}}
resource "aws_opensearch_domain" "domain1" {
}

resource "aws_opensearch_domain" "domain2" {
#        ^^^^^^^^^^^^^^^^^^^^^^^> {{Related domain}}
  # Noncompliant@+1 {{Omitting "encrypt_at_rest.enabled" disables OpenSearch domains encryption. Make sure it is safe here.}}
  encrypt_at_rest {
# ^^^^^^^^^^^^^^^
  }
}

resource "aws_opensearch_domain" "domain3" {
#        ^^^^^^^^^^^^^^^^^^^^^^^> {{Related domain}}
  encrypt_at_rest {
    enabled = false # Noncompliant {{Make sure that using unencrypted OpenSearch domains is safe here.}}
#   ^^^^^^^^^^^^^^^
  }
}

resource "aws_opensearch_domain" "domain4" {
  encrypt_at_rest {
    enabled = true # Compliant
  }
}

# Noncompliant@+1 {{Omitting "encrypt_at_rest.enabled" disables Elasticsearch domains encryption. Make sure it is safe here.}}
resource "aws_elasticsearch_domain" "domain5" {
}

resource "aws_elasticsearch_domain" "domain6" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^> {{Related domain}}
  # Noncompliant@+1 {{Omitting "encrypt_at_rest.enabled" disables Elasticsearch domains encryption. Make sure it is safe here.}}
  encrypt_at_rest {
# ^^^^^^^^^^^^^^^
  }
}

resource "aws_elasticsearch_domain" "domain7" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^> {{Related domain}}
  encrypt_at_rest {
    enabled = false # Noncompliant {{Make sure that using unencrypted Elasticsearch domains is safe here.}}
#   ^^^^^^^^^^^^^^^
  }
}

resource "aws_elasticsearch_domain" "domain8" {
  encrypt_at_rest {
    enabled = true # Compliant
  }
}

resource "some_other_domain" "not_elasticsearch_domain" {
}
