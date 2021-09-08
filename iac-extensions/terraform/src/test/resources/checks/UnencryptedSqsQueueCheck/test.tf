resource "aws_sqs_queue" "queue1" {  # Noncompliant {{Make sure that using unencrypted SQS queues is safe here.}}
#        ^^^^^^^^^^^^^^^
}

resource "aws_sqs_queue" "queue2" { # Compliant
  kms_master_key_id = aws_kms_key.enc_key.key_id
}

resource "randomeStuff" {
  kms_master_key_id = false
}

resource {
  # unnamed resource ?
}
