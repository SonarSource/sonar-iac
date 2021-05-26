resource "aws_s3_bucket" "myawsbucket" {
  tags = {
    "anycompany:cost-center" = "" # Noncompliant (S6273)
    "anycompany:CostCenter" = "" # Noncompliant
    "AnyCompany:CostCenter:Bar" = ""
  }

  tags = { "anycompany:CostCenter" = "" } # Noncompliant
}
