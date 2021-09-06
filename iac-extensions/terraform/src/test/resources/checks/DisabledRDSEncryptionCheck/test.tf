resource "aws_db_instance" "encrypted" {
  storage_encrypted = true
}

resource "aws_db_instance" "unencrypted1" { # Noncompliant
  storage_encrypted = false
}

resource "aws_db_instance" "unencrypted2" { # Noncompliant
  
}

resource "no_aws_db_instance" "whatever" {
  storage_encrypted = false
}
