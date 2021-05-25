resource "aws_s3_bucket" "myawsbucket" {
  tags = {
    "anycompany:cost-center" = "" # Noncompliant {{Rename tag key "anycompany:cost-center" to match the regular expression "^([A-Z][A-Za-z]*:)*([A-Z][A-Za-z]*)$".}}
#   ^^^^^^^^^^^^^^^^^^^^^^^^
    "anycompany:CostCenter" = "" # Noncompliant
    ":CostCenter" = "" # Noncompliant
    "Anycompany:" = "" # Noncompliant
    "anycompany:cost-" = "" # Noncompliant
    "AnyCompany:CostCenter" = ""
    "AnyCompany" = ""
    "AnyCompany:CostCenter:Bar" = ""
    "anycompany" = "" # Noncompliant
    Name = ""
  }

  tags = { "anycompany:CostCenter" = "" } # Noncompliant

  foo = {
    "anycompany:CostCenter" = ""
  }

  tags = "bar"
}
