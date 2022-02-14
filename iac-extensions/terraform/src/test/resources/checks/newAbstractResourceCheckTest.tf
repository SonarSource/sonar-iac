// Check reportIfAbsent on AttributeSymbol

resource "missing_attribute" "noncompliant" { // Noncompliant {{attribute is missing}}
//       ^^^^^^^^^^^^^^^^^^^
}

resource "missing_attribute" "compliant" {
  expected_attribute = true
}

// Check reportIfAbsent on BlockSymbol

resource "missing_block" "noncompliant" { // Noncompliant {{block is missing}}
  //     ^^^^^^^^^^^^^^^
}

resource "missing_block" "compliant" {
  expected_block {}
}

// Check reportIf with isNotEquals on AttributeSymbol

resource "attribute_reportIf" "noncompliant" {
  attribute = "not_expected_value" // Noncompliant {{attribute has not expected value}}
}

resource "attribute_reportIf" "compliant" {
  attribute = "expected_value"
}

// Coverage

resource "not_relevant_resource" "coverage" {
}
