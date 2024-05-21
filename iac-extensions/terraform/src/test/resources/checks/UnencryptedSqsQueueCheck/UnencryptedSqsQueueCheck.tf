resource "aws_sqs_queue" "queue" {
  name = "queue-unencrypted"
  sqs_managed_sse_enabled = false # Noncompliant {{Setting "SqsManagedSseEnabled" to "false" disables SQS queues encryption. Make sure it is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "aws_sqs_queue" "queue1" { # Compliant - defaults are secure
}

resource "aws_sqs_queue" "queue2" { # Compliant - KMS encryption
  kms_master_key_id = aws_kms_key.enc_key.key_id
}

resource "aws_sqs_queue" "queue" { # Compliant - SQS encryption
  name = "queue-encrypted"
  sqs_managed_sse_enabled = true
}

resource "randomeStuff" {
  kms_master_key_id = false
}

resource {
  # unnamed resource ?
}
