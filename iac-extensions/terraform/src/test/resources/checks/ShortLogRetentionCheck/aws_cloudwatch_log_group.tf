resource "aws_cloudwatch_log_group" "noncompliant_example" {
  name = "noncompliant_example"
  retention_in_days = 3 # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_cloudwatch_log_group" "noncompliant_example_zero_days" {
  name = "noncompliant_example_zero_days"
  retention_in_days = 0 # Noncompliant {{Make sure that defining a short log retention duration is safe here.}}
}

# Noncompliant@+1 {{Omitting "retention_in_days" results in a short log retention duration. Make sure it is safe here.}}
resource "aws_cloudwatch_log_group" "noncompliant_example_omitting_days" {
  name = "noncompliant_example_omitting_days"
}

resource "aws_cloudwatch_log_group" "compliant_example" {
  name = "compliant_example"
  retention_in_days = 30
}
