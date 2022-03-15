resource "aws_efs_file_system" "fs" { # Noncompliant {{Omitting "encrypted" disables EFS file systems encryption. Make sure it is safe here.}}
#        ^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_efs_file_system" "fs" {
#        ^^^^^^^^^^^^^^^^^^^^^> {{Related file system}}
  encrypted = false  # Noncompliant {{Make sure that using unencrypted EFS file systems is safe here.}}
# ^^^^^^^^^^^^^^^^^
}

resource "aws_efs_file_system" "fs" { # Compliant
  encrypted = true  
}

resource "some_other_type" "other" {
}
