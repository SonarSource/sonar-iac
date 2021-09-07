resource "aws_db_instance" "encrypted" {
  storage_encrypted = true
}

resource "aws_db_instance" "unencrypted1" {
#        ^^^^^^^^^^^^^^^^^>
  storage_encrypted = false # Noncompliant {{Make sure that using unencrypted databases is safe here.}}
# ^^^^^^^^^^^^^^^^^
}

resource "aws_db_instance" "unencrypted2" { # Noncompliant {{Make sure that using unencrypted databases is safe here.}}
#        ^^^^^^^^^^^^^^^^^
}

resource "no_aws_db_instance" "whatever" {
  storage_encrypted = false
}
