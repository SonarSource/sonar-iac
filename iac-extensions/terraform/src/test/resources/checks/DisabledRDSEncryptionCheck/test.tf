resource "aws_db_instance" "encrypted" {
  storage_encrypted = true
}

resource "aws_db_instance" "unencrypted1" {
#        ^^^^^^^^^^^^^^^^^>
  storage_encrypted = false # Noncompliant {{Make sure that using unencrypted databases is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting "storage_encrypted" disables databases encryption. Make sure it is safe here.}}
resource "aws_db_instance" "unencrypted2" {
#        ^^^^^^^^^^^^^^^^^
}

resource "no_aws_db_instance" "whatever" {
  storage_encrypted = false
}
