resource "aws_lb_listener" "weak_policy_10" {
  protocol    = "HTTPS"
  ssl_policy  = "ELBSecurityPolicy-TLS13-1-0-2021-06" # Noncompliant {{Change this code to disable support of older TLS versions.}}
#               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_lb_listener" "weak_policy_11" {
  protocol    = "HTTPS"
  ssl_policy  = "ELBSecurityPolicy-TLS13-1-1-2021-06" # Noncompliant
}

resource "aws_lb_listener" "weak_policy_2016" {
  protocol    = "TLS"
  ssl_policy  = "ELBSecurityPolicy-2016-08" # Noncompliant
}

resource "aws_lb_listener" "weak_policy_explicit_11" {
  protocol    = "HTTPS"
  ssl_policy  = "ELBSecurityPolicy-TLS-1-1-2017-01" # Noncompliant
}

resource "aws_lb_listener" "missing_ssl_policy" { # Noncompliant {{Set "ssl_policy" to disable support of older TLS versions.}}
  protocol    = "HTTPS"
  port        = 443
}

resource "aws_lb_listener" "strong_policy_12" {
  protocol    = "HTTPS"
  ssl_policy  = "ELBSecurityPolicy-TLS13-1-2-2021-06"
}

resource "aws_lb_listener" "http_protocol" {
  protocol    = "HTTP"
  port        = 80
}

resource "aws_lb_listener" "no_protocol" {
  port        = 443
}
