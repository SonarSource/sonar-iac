resource "aws_s3_bucket" "examplebucket" {
  bucket = "mybucketname"
  tags = {
    "anycompany:~cost-center~" = "Forbidden characters in the key" # Noncompliant {{Rename tag key "anycompany:~cost-center~" to comply with required format.}}
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^
    "aws:forbidden-namespace" = "aws: namespace is reserved" # Noncompliant
    "AnUnnecessaryLongKeyNameThatContainsMoreThan128AllowedCharacters:AnUnnecessaryLongKeyNameThatContainsMoreThan128AllowedCharacters" = "Example" # Noncompliant
    "compliant:format" = "Example"
  }
}
