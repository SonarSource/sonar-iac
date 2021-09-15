resource "aws_dms_replication_instance" "noncompliantdms1" { # Noncompliant {{Make sure that using public IP address is safe here.}}
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_dms_replication_instance" "noncompliantdms2" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^> {{Related instance}}
  publicly_accessible = true # Noncompliant {{Make sure that using public IP address is safe here.}}
# ^^^^^^^^^^^^^^^^^^^
}

resource "aws_dms_replication_instance" "compliantdms" {
  publicly_accessible = false
}

resource "aws_instance" "noncompliantawsinstance1" { # Noncompliant {{Make sure that using public IP address is safe here.}}
#        ^^^^^^^^^^^^^^
}

resource "aws_instance" "noncompliantawsinstance2" {
#        ^^^^^^^^^^^^^^> {{Related instance}}
  associate_public_ip_address = true # Noncompliant {{Make sure that using public IP address is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_instance" "compliantawsinstance" {
  associate_public_ip_address = false
}

resource "aws_launch_template" "noncompliantawstemplate1" { # Noncompliant {{Make sure that using public IP address is safe here.}}
#        ^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_launch_template" "noncompliantawstemplate2" {
#        ^^^^^^^^^^^^^^^^^^^^^> {{Related template}}
  network_interfaces { # Noncompliant {{Make sure that using public IP address is safe here.}}
# ^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_launch_template" "noncompliantawstemplate3" {
#        ^^^^^^^^^^^^^^^^^^^^^> {{Related template}}
  network_interfaces {
    associate_public_ip_address = true # Noncompliant {{Make sure that using public IP address is safe here.}}
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_launch_template" "compliantawstemplate" {
  network_interfaces {
    associate_public_ip_address = false
  }
}
