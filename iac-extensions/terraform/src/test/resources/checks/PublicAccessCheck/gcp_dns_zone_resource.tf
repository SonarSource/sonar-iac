resource "google_dns_managed_zone" "rspec6404_sensitive_explicit" {
  visibility = "public" # Noncompliant {{Ensure that granting public access to this resource is safe here.}}
# ^^^^^^^^^^^^^^^^^^^^^
}

# Noncompliant@+1 {{Omitting visibility will grant public access to this managed zone. Ensure it is safe here.}}
resource "google_dns_managed_zone" "rspec6404_sensitive_omission" {
       # ^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_dns_managed_zone" "rspec6404_compliant" {
  visibility = "private"
}

resource "non_google_dns_managed_zone" "rspec6404_coverage" {
  visibility = "public"
}
