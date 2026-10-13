import {
  to = aws_s3_bucket.by_id
  id = "literal"
}

import {
  to = aws_s3_bucket.by_identity
  identity = {
    bucket = "my-bucket"
    region = "us-east-1"
  }
}

moved {
  from = aws_s3_bucket.old
  to = aws_s3_bucket.new
}

removed {
  from = aws_s3_bucket.retired
  lifecycle {
    destroy = false
  }
}

ephemeral "aws_secretsmanager_secret_version" "db_password" {
  secret_id = "db-password"
}

check "health_check" {
  data "http" "health" {
    url = "https://example.com/health"
  }

  assert {
    condition = data.http.health.status_code == 200
    error_message = "The health endpoint must return 200."
  }
}

action "aws_lambda_invoke" "rotate" {
  config {
    function_name = var.rotation_function_name
    payload       = jsonencode({ endpoint = "https://internal.example.com/hook" })
  }
}

action "aws_lambda_invoke" {
}

action {
}

resource "terraform_data" "rotation_trigger" {
  lifecycle {
    action_trigger {
      events     = [after_create, after_update]
      actions    = [action.aws_lambda_invoke.rotate]
      on_failure = halt
      condition  = var.rotation_enabled
    }
  }
}
