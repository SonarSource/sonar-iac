resource "google_dns_managed_zone" "compliant-dns-zone" {
  dnssec_config {
    default_key_specs {
      algorithm = "rsasha256"
    }
  }
}

resource "google_dns_managed_zone" "noncompliant-dns-zone" { # Noncompliant {{Make sure creating a DNS zone without DNSSEC enabled is safe here.}}
#        ^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_dns_managed_zone" "noncompliant-dns-zone-2" { # Noncompliant {{Make sure creating a DNS zone without DNSSEC enabled is safe here.}}
#        ^^^^^^^^^^^^^^^^^^^^^^^^^
  dnssec_config = "something"
}
