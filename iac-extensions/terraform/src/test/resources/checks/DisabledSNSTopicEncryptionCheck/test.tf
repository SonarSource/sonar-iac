resource "aws_sns_topic" "topic_encrypted" {
  name = "sns-encrypted"
  kms_master_key_id = aws_kms_key.enc_key.key_id
}

resource "aws_sns_topic" "topic_unencrypted" { # Noncompliant {{Make sure that using unencrypted SNS topics is safe here.}}
#        ^^^^^^^^^^^^^^^
  name = "sns-unencrypted"
}

resource "some_other_resource" "other_resource" {
}
