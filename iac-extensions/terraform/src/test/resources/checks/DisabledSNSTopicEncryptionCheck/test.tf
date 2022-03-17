resource "aws_sns_topic" "topic_encrypted" {
  name = "sns-encrypted"
  kms_master_key_id = aws_kms_key.enc_key.key_id
}

# Noncompliant@+1 {{Omitting "kms_master_key_id" disables SNS topics encryption. Make sure it is safe here.}}
resource "aws_sns_topic" "topic_unencrypted" {
#        ^^^^^^^^^^^^^^^
  name = "sns-unencrypted"
}

resource "some_other_resource" "other_resource" {
}
