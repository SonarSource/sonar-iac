resource "aws_api_gatewayv2_stage" "missing_access_log_settings" {  # Noncompliant
}

resource "aws_api_gatewayv2_stage" "safe_access_log_settings" {
  access_log_settings {
    destination_arn = ""
    format = ""
  }
}
