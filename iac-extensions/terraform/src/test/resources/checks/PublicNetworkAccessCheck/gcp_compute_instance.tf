resource "google_compute_instance" "noncompliant" {
  network_interface {
    access_config {  // Noncompliant {{Make sure allowing public network access is safe here.}}
    }
  }

  network_interface {
    ipv6_access_config {  // Noncompliant {{Make sure allowing public network access is safe here.}}
    }
  }

  network_interface {
    access_config { }  // Noncompliant
    ipv6_access_config { }  // Noncompliant
  }

  network_interface {
  }
}

resource "non_google_compute_instance" "coverage" {
  network_interface {
    access_config { }
    ipv6_access_config { }
  }
}
