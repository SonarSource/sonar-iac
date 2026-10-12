variable "example" { # Noncompliant {{Missing expected attribute}}
  nested { # Noncompliant {{Missing nested attribute}}
  }
}

locals { # Noncompliant {{Missing expected attribute}}
  nested { # Noncompliant {{Missing nested attribute}}
  }
}

module "example" { # Noncompliant {{Missing expected attribute}}
  nested { # Noncompliant {{Missing nested attribute}}
  }
}

provider "example" { # Noncompliant {{Missing expected attribute}}
  nested { # Noncompliant {{Missing nested attribute}}
  }
}

output "example" { # Noncompliant {{Missing expected attribute}}
  nested { # Noncompliant {{Missing nested attribute}}
  }
}

terraform { # Noncompliant {{Missing expected attribute}}
  nested { # Noncompliant {{Missing nested attribute}}
  }
}

resource "example" "example" {
  variable "nested" {}
  locals {}
  module "nested" {}
  provider "nested" {}
  output "nested" {}
  terraform {}
  dynamic "output" {
    content {}
  }
}

data "example" "example" {}
ephemeral "example" "example" {}
check "example" {
  output "nested" {}
}
unknown "example" {}
