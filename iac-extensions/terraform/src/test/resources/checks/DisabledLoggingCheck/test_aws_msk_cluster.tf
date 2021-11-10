resource "aws_msk_cluster" "sensitive_msk" { # Noncompliant
  cluster_name = "sensitive_msk"
}

resource "aws_msk_cluster" "sensitive_msk" {
  cluster_name = "sensitive_msk"
  logging_info { # Noncompliant

  }
}

resource "aws_msk_cluster" "sensitive_msk" {
  cluster_name = "sensitive_msk"
  logging_info {
    broker_logs { # Noncompliant

    }
  }
}

resource "aws_msk_cluster" "sensitive_msk" {
  cluster_name = "sensitive_msk"
  logging_info {
    broker_logs { # Noncompliant
      cloudwatch_logs {
        enabled = false
      }
    }
  }
}

resource "aws_msk_cluster" "sensitive_msk" {
  cluster_name = "sensitive_msk"
  logging_info {
    broker_logs {
      cloudwatch_logs {
        enabled   = false
      }
      firehose {
        enabled = false
      }
      s3 {
        enabled = true
      }
    }
  }
}

resource "aws_msk_cluster" "sensitive_msk" {
  cluster_name = "sensitive_msk"
  logging_info {
    broker_logs {
      cloudwatch_logs {
        enabled   = false
      }
      firehose {
        enabled = true
      }
    }
  }
}

resource "aws_msk_cluster" "sensitive_msk" {
  cluster_name = "sensitive_msk"
  logging_info {
    broker_logs {
      cloudwatch_logs {
        enabled   = true
      }
    }
  }
}

resource "non_msk_cluster" "for_coverage" {
  bucket = "non_msk_cluster_name"
}
