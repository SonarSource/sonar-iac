# Noncompliant@+1 {{Omitting access_log_settings makes logs incomplete. Make sure it is safe here.}}
resource "aws_api_gatewayv2_stage" "missing_access_log_settings" {
}

resource "aws_api_gatewayv2_stage" "safe_access_log_settings" {
  access_log_settings {
    destination_arn = ""
    format = ""
  }
}
