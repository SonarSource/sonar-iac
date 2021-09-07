resource "aws_elasticsearch_domain" "domain1" { # Noncompliant {{Make sure that using unencrypted Elasticsearch domains is safe here.}}
}

resource "aws_elasticsearch_domain" "domain2" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^> {{Related domain}}
  encrypt_at_rest { # Noncompliant {{Make sure that using unencrypted Elasticsearch domains is safe here.}}
# ^^^^^^^^^^^^^^^
  }
}

resource "aws_elasticsearch_domain" "domain3" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^> {{Related domain}}
  encrypt_at_rest {
    enabled = false # Noncompliant {{Make sure that using unencrypted Elasticsearch domains is safe here.}}
#   ^^^^^^^^^^^^^^^
  }
}

resource "aws_elasticsearch_domain" "domain4" {
  encrypt_at_rest {
    enabled = true # Compliant
  }
}

resource "some_other_domain" "not_elasticsearch_domain" {
}
