resource "my_resource" "block_is_present" {
  my_block {} // Noncompliant {{my_block is present}}
}

resource "my_resource" "test" { // Noncompliant {{my_block is missing}}

  multi_block {
    my_attribute_1 = "non_sensitive_value"
    my_attribute_2 = "expected_value"
    my_attribute_3 = "no false nor true"
    my_list_1 = ["safe1", "safe2"]
  }

  multi_block {
    my_attribute_1 = "sensitive_value" // Noncompliant {{my_attribute_1 is sensitive_value}}
    my_attribute_2 = "foo.bar.baz" // Noncompliant {{my_attribute_2 contains the sensitive term 'bar'}}  {{my_attribute_2 is not expected_value}}
    my_attribute_3 = true // Noncompliant {{my_attribute_3 is true}}
    my_list_1 = [
      "safe1",
      "unsafe1", // Noncompliant {{my_list_1 contains unsafe value}}
      "safe2"
    ]
  }

  multi_block {
    my_attribute_1 = foo.bar // Noncompliant {{my_attribute_1 is a AttributeAccessTree}}
    my_attribute_2 = foo.bar // Noncompliant {{my_attribute_2 is not a TextTree}}
    my_attribute_3 = false  // Noncompliant {{my_attribute_3 is false}}
  }

  multi_block { // Noncompliant {{my_attribute_3 is missing}}
  }

}

resource "not_my_resource" "block_is_present" {
  my_block {}

  multi_block {
    my_attribute_1 = "sensitive_value"
  }
}
