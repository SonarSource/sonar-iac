# Noncompliant@+1 {{Omitting logging_info.broker_logs makes logs incomplete. Make sure it is safe here.}}
resource "aws_msk_cluster" "sensitive_msk" {
  cluster_name = "sensitive_msk"
}

resource "aws_msk_cluster" "sensitive_msk" {
  cluster_name = "sensitive_msk"
  logging_info { # Noncompliant {{Omitting broker_logs makes logs incomplete. Make sure it is safe here.}}

  }
}

resource "aws_msk_cluster" "sensitive_msk" {
  cluster_name = "sensitive_msk"
  logging_info {
    # Noncompliant@+1 {{Omitting cloudwatch_logs, firehose or s3 makes logs incomplete. Make sure it is safe here.}}
    broker_logs {
  # ^^^^^^^^^^^

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
