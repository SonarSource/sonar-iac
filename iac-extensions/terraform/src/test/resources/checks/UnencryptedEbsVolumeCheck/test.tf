resource "aws_ebs_volume" "ebs_volume" { # Noncompliant {{Make sure that using unencrypted volumes is safe here.}}
#        ^^^^^^^^^^^^^^^^
}

resource "aws_ebs_volume" "ebs_volume" {
  encrypted = false # Noncompliant {{Make sure that using unencrypted volumes is safe here.}}
# ^^^^^^^^^^^^^^^^^
}
resource "aws_ebs_volume" "ebs_volume" {
  encrypted = true
}

resource "aws_ebs_encryption_by_default" "default_encryption" {
  enabled = false # Noncompliant
# ^^^^^^^^^^^^^^^
}
resource "aws_ebs_encryption_by_default" "default_encryption_1" { # Compliant
}

resource "aws_ebs_encryption_by_default" "default_encryption_2" { # Compliant
  enabled = true
}

resource "aws_launch_configuration" "launch_configuration" {
  root_block_device { # Noncompliant {{Make sure that using unencrypted volumes is safe here.}}
# ^^^^^^^^^^^^^^^^^
  }
  ebs_block_device { # Noncompliant
# ^^^^^^^^^^^^^^^^
  }
}

resource "aws_launch_configuration" "launch_configuration" {
  root_block_device {
    encrypted = false # Noncompliant {{Make sure that using unencrypted volumes is safe here.}}
#   ^^^^^^^^^^^^^^^^^
  }
  ebs_block_device {
    encrypted = false # Noncompliant {{Make sure that using unencrypted volumes is safe here.}}
#   ^^^^^^^^^^^^^^^^^
  }
}

resource "aws_launch_configuration" "launch_configuration" {
  root_block_device {
    encrypted = true
  }
  ebs_block_device {
    encrypted = true
  }
}

resource "locals" {
  enabled = false
}

resource {
  # unnamed resource ?
}

