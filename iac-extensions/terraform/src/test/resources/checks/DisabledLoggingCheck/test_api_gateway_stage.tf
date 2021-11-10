resource "aws_api_gateway_stage" "missing_tracing" { # Noncompliant
}

resource "aws_api_gateway_stage" "sensitive_gateway" {
  xray_tracing_enabled = false # Noncompliant
}

resource "aws_api_gateway_stage" "safe_gateway" {
  xray_tracing_enabled = true
}

resource "non_aws_api_gateway_stage" "for_coverage" {
}
