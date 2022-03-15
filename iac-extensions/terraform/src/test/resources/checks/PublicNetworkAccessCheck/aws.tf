# Noncompliant@+1 {{Omitting "publicly_accessible" allows network access from the Internet. Make sure it is safe here.}}
resource "aws_dms_replication_instance" "noncompliantdms1" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_dms_replication_instance" "noncompliantdms2" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^> {{Related instance}}
  publicly_accessible = true # Noncompliant {{Make sure allowing public network access is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_dms_replication_instance" "compliantdms" {
  publicly_accessible = false
}

# Noncompliant@+1 {{Omitting "associate_public_ip_address" allows network access from the Internet. Make sure it is safe here.}}
resource "aws_instance" "noncompliantawsinstance1" {
#        ^^^^^^^^^^^^^^
}

resource "aws_instance" "noncompliantawsinstance2" {
#        ^^^^^^^^^^^^^^> {{Related instance}}
  associate_public_ip_address = true # Noncompliant {{Make sure allowing public network access is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_instance" "compliantawsinstance" {
  associate_public_ip_address = false
}

# Noncompliant@+1 {{Omitting "network_interfaces.associate_public_ip_address" allows network access from the Internet. Make sure it is safe here.}}
resource "aws_launch_template" "noncompliantawstemplate1" {
#        ^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_launch_template" "noncompliantawstemplate2" {
#        ^^^^^^^^^^^^^^^^^^^^^> {{Related template}}
  # Noncompliant@+1 {{Omitting "associate_public_ip_address" allows network access from the Internet. Make sure it is safe here.}}
  network_interfaces {
# ^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_launch_template" "noncompliantawstemplate3" {
#        ^^^^^^^^^^^^^^^^^^^^^> {{Related template}}
  network_interfaces {
    associate_public_ip_address = true # Noncompliant {{Make sure allowing public network access is safe here.}}
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource "aws_launch_template" "compliantawstemplate" {
  network_interfaces {
    associate_public_ip_address = false
  }
}
