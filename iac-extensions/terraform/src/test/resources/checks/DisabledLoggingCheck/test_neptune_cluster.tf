resource "aws_neptune_cluster" "neptune_noncompliant_wrong" {
  enable_cloudwatch_logs_exports = []  # Noncompliant {{Make sure that disabling logging is safe here.}}
  #                                ^^
}

resource "aws_neptune_cluster" "neptune_noncompliant_missing" {  # Noncompliant
}

resource "aws_neptune_cluster" "neptune_compliant" {
  enable_cloudwatch_logs_exports = ["audit"]
}

resource "aws_neptune_cluster" "neptune_compliant_ref" {
  enable_cloudwatch_logs_exports = XXXXX.arn
}

resource "non_aws_neptune_cluster" "for_coverage" {
}
