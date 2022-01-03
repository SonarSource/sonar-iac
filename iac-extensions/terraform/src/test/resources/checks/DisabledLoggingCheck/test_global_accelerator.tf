# Noncompliant@+1 {{Omitting attributes.flow_logs_enabled makes logs incomplete. Make sure it is safe here.}}
resource "aws_globalaccelerator_accelerator" "noncompliant_missing1" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_globalaccelerator_accelerator" "noncompliant_missing2" {
  attributes {  # Noncompliant {{Make sure that disabling logging is safe here.}}
# ^^^^^^^^^^
  }
}

resource "aws_globalaccelerator_accelerator" "noncompliant_disabled" {
  attributes {
    flow_logs_enabled = false  # Noncompliant
  # ^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_globalaccelerator_accelerator" "compliant" {
  attributes {
    flow_logs_enabled = true
  }
}

resource "non_aws_globalaccelerator_accelerator" "for_coverage" {
}
