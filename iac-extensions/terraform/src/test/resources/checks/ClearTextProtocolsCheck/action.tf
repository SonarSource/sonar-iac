action "aws_lambda_invoke" "rotate" {
  config {
    function_name = "rotate-db-password"
    endpoint      = "http://internal.acme.corp/hook" # Noncompliant {{Make sure that using clear-text protocols is safe here.}}
#                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

action "aws_lambda_invoke" "rotate_secure" {
  config {
    function_name = var.rotation_function_name
    endpoint      = "https://internal.acme.corp/hook"
  }
}

action "example_notify" "protocols" {
  config {
    upload   = "ftp://files.acme.corp/drop" # Noncompliant {{Make sure that using clear-text protocols is safe here.}}
#               ^^^^^^^^^^^^^^^^^^^^^^^^^^
    upper    = "HTTP://INTERNAL.ACME.CORP/hook" # Noncompliant {{Make sure that using clear-text protocols is safe here.}}
#               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    mirrors  = ["https://a.acme.corp", "http://b.acme.corp"] # Noncompliant {{Make sure that using clear-text protocols is safe here.}}
#                                       ^^^^^^^^^^^^^^^^^^
    payload  = jsonencode({ callback = "http://hooks.acme.corp/done" }) # Noncompliant {{Make sure that using clear-text protocols is safe here.}}
#                                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
    # Every unencrypted URL in a string is reported
    both     = "http://a.acme.corp http://b.acme.corp" # Noncompliant 2
    # Template parts holding a whole URL are reported, templates interpolating the host are not
    template = "http://static.acme.corp/${var.path}" # Noncompliant {{Make sure that using clear-text protocols is safe here.}}
#               ^^^^^^^^^^^^^^^^^^^^^^^^
    dynamic  = "http://${var.host}/hook"
    # Targets that are safe without TLS are not reported: loopback, documentation and single-label hosts
    local    = "http://localhost:8080/hook"
    loopback = "http://127.0.0.1/hook"
    docs     = "http://hooks.example.com/done"
    intranet = "http://intranet/hook"
    secure   = "https://hooks.acme.corp/done"
    nested {
      url = "http://nested.acme.corp" # Noncompliant {{Make sure that using clear-text protocols is safe here.}}
#            ^^^^^^^^^^^^^^^^^^^^^^^
    }
    # Noncompliant@+2 {{Make sure that using clear-text protocols is safe here.}}
    script = <<EOT
      curl http://scripts.acme.corp/run.sh
    EOT
  }
}

# Only the config block of an action holds provider arguments
action "example_notify" "meta_arguments" {
  provider = example.http
  config {}
}

action "example_notify" {}

action {}

# Literal URLs outside of action config blocks are out of scope for this part
resource "example_webhook" "hook" {
  url = "http://hooks.acme.corp/resource"
  rule {
    action {
      config {
        url = "http://hooks.acme.corp/nested-action"
      }
    }
  }
  lifecycle {
    action_trigger {
      events     = [after_create]
      actions    = [action.aws_lambda_invoke.rotate]
      on_failure = halt
    }
  }
}

variable "endpoint" {
  default = "http://hooks.acme.corp/variable"
}
