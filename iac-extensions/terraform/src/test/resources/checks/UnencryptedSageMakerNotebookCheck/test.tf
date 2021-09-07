resource "aws_sagemaker_notebook_instance" "notebook1" {  # Noncompliant {{Make sure that using unencrypted SageMaker notebook instances is safe here.}}
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