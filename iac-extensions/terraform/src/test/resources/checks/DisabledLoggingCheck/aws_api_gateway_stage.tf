# Noncompliant@+1 {{Omitting xray_tracing_enabled makes logs incomplete. Make sure it is safe here.}}
resource "aws_api_gateway_stage" "missing_tracing" {
  access_log_settings {
    destination_arn = ""
    format = ""
  }
}

resource "aws_api_gateway_stage" "sensitive_tracing" {
  xray_tracing_enabled = false # Noncompliant
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  access_log_settings {
    destination_arn = ""
    format = ""
  }
}

# Noncompliant@+1 {{Omitting access_log_settings makes logs incomplete. Make sure it is safe here.}}
resource "aws_api_gateway_stage" "missing_access_log_settings" {
  xray_tracing_enabled = true
}

resource "aws_api_gateway_stage" "safe_tracing_and_access_log" {
  xray_tracing_enabled = true
  access_log_settings {
    destination_arn = ""
    format = ""
  }
}

resource "non_aws_api_gateway_stage" "for_coverage" {
}
