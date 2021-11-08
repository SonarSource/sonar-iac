resource "aws_msk_cluster" "sensitive_data_cluster_1" {
  encryption_info {
    encryption_in_transit {
      client_broker = "PLAINTEXT" # Noncompliant
    }
  }
}

resource "aws_msk_cluster" "sensitive_data_cluster_2" {
  encryption_info {
    encryption_in_transit {
      client_broker = "TLS_PLAINTEXT" # Noncompliant {{Using TLS_PLAINTEXT protocol is insecure. Use TLS instead}}
      #               ^^^^^^^^^^^^^^^
    }
  }
}

resource "aws_msk_cluster" "sensitive_data_cluster_3" {
  encryption_info {
    encryption_in_transit {
      in_cluster = false # Noncompliant {{Communication among nodes of a cluster should be encrypted}}
      #            ^^^^^
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

resource "not_an_aws_msk_cluster" "for_coverage" {
}
