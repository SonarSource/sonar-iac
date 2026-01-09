resource "aws_alb_listener" "weak_policy" {
  protocol    = "HTTPS"
  ssl_policy  = "ELBSecurityPolicy-2016-08" # Noncompliant {{Change this code to disable support of older TLS versions.}}
#               ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_alb_listener" "missing_ssl_policy" { # Noncompliant {{Set "ssl_policy" to disable support of older TLS versions.}}
  protocol    = "HTTPS"
  port        = 443
}

resource "aws_alb_listener" "strong_policy" {
  protocol    = "HTTPS"
  ssl_policy  = "ELBSecurityPolicy-TLS13-1-2-2021-06"
}

resource "aws_alb_listener" "http_protocol" {
  protocol    = "HTTP"
  port        = 80
}
