# Good
// Bad

locals {
  list  = ["a", "b", "c"]
  value = list.0
}

output "no_description" {
  value = "value"
}

variable "my_list" {
  type = list(string)
}
