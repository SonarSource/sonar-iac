resource "aws_msk_cluster" "sensitive_data_cluster_1" {
  encryption_info {
    encryption_in_transit {
      client_broker = "PLAINTEXT" # Noncompliant
    # ^^^^^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource "aws_msk_cluster" "sensitive_data_cluster_2" {
  encryption_info {
    encryption_in_transit {
      client_broker = "TLS_PLAINTEXT" # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
    # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource "aws_msk_cluster" "sensitive_data_cluster_3" {
  encryption_info {
    encryption_in_transit {
      in_cluster = false # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
    # ^^^^^^^^^^^^^^^^^^
    }
  }
}

resource "aws_msk_cluster" "compliant_data_cluster_1" {
  encryption_info {
    encryption_in_transit {
      client_broker = "TLS" # Compliant
    }
  }
}

resource "aws_msk_cluster" "compliant_data_cluster_2" {
  encryption_info {
    encryption_in_transit {
      in_cluster = true # Compliant
    }
  }
}

resource "aws_msk_cluster" "compliant_data_cluster_3" {
  encryption_info {
    encryption_in_transit {
      client_broker = "TLS" # Compliant
      in_cluster = true # Compliant
    }
  }
}

resource "aws_msk_cluster" "compliant_data_cluster_4" {
  encryption_info {
    encryption_in_transit {
    }
  }
}

resource "not_an_aws_msk_cluster" "for_coverage" {
}
