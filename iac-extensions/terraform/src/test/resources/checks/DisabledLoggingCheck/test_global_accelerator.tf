resource "aws_globalaccelerator_accelerator" "noncompliant_missing1" {  # Noncompliant
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_globalaccelerator_accelerator" "noncompliant_missing2" {
  attributes {  # Noncompliant
# ^^^^^^^^^^
  }
}

resource "aws_globalaccelerator_accelerator" "noncompliant_disabled" {
  attributes {
    flow_logs_enabled = false  # Noncompliant
                    #   ^^^^^
  }
}

resource "aws_globalaccelerator_accelerator" "compliant" {
  attributes {
    flow_logs_enabled = true
  }
}

resource "non_aws_globalaccelerator_accelerator" "for_coverage" {
}
