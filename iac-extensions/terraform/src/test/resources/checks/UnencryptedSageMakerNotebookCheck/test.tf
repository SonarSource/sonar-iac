# Noncompliant@+1 {{Omitting "kms_key_id" disable encryption of SageMaker notebook instances. Make sure it is safe here.}}
resource "aws_sagemaker_notebook_instance" "notebook1" {
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_sagemaker_notebook_instance" "notebook2" { # Compliant
  kms_key_id = aws_kms_key.enc_key.key_id
}

resource "randomeStuff" {
  kms_key_id = false
}

resource {
  # unnamed resource ?
}
