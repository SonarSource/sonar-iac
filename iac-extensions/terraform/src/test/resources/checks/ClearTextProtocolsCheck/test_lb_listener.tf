resource "aws_lb_listener" "sensitive_redirect" {
  protocol = "HTTP" # Noncompliant
  default_action {
    type = "redirect"
    redirect {
      protocol = "HTTP"
    }
  }
}

resource "aws_lb_listener" "safe_redirect" {
  protocol = "HTTP"
  default_action {
    type = "redirect"
    redirect {
      protocol = "HTTPS"
    }
  }
}

resource "aws_lb_listener" "sensitive_forward" {
  protocol = "HTTP" # Noncompliant
  default_action {
    type = "forward"
    target_group_arn = XXXXX.arn
  }
}

resource "aws_lb_listener" "sensitive_fixed-response" {
  protocol = "HTTP" # Noncompliant
  default_action {
    type = "fixed-response"
    target_group_arn = XXXXX.arn
  }
}

resource "aws_lb_listener" "no_default_action" {
  protocol = "HTTP"
}

resource "not_an_aws_msk_cluster" "for_coverage" {
  protocol = "HTTP"
  default_action {
    type = "fixed-response"
  }
}

resource "aws_lb_listener" "multiple_blocks" {
  protocol = "HTTP" # Noncompliant

  default_action {
    type = "authenticate-cognito"
  }

  default_action {
    type = "fixed-response" # multiple blocks with same name are allowed
  }
}
