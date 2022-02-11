resource "google_compute_firewall" "s6321-noncompliant1" {
  name    = "s6321-noncompliant1"
  network = google_compute_network.default.name

  allow {
    protocol = "tcp"
    ports    = ["22"]
    #           ^^^^> {{Related protocol setting.}}
  }

  allow {
    protocol = "tcp"
    ports    = ["3389"]
    #           ^^^^^^> {{Related protocol setting.}}
  }

  allow {
    protocol = "tcp"
    ports    = ["10-30"]
    #           ^^^^^^^> {{Related protocol setting.}}
  }

  allow {
    protocol = "tcp"
    ports    = ["3000-4000"]
    #           ^^^^^^^^^^^> {{Related protocol setting.}}
  }

  allow {
    protocol = "tcp"
    ports    = ["21", "22"]
    #                 ^^^^> {{Related protocol setting.}}
  }


  source_ranges = ["0.0.0.0/0"] # Noncompliant {{Restrict IP addresses authorized to access administration services.}}
  #                ^^^^^^^^^^^
}


resource "google_compute_firewall" "s6321-noncompliant2" {
  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_ranges = ["0::0/0"] # Noncompliant
  #                ^^^^^^^^
}

resource "google_compute_firewall" "s6321-noncompliant2" {
  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_ranges = ["::0/0"] # Noncompliant
  #                ^^^^^^^
}


resource "google_compute_firewall" "s6321-noncompliant3" {
  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_ranges = ["::/0"]  # Noncompliant
  #                ^^^^^^
}

resource "google_compute_firewall" "s6321-noncompliant4" {
  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_ranges = ["10.0.0.1/32", "::/0"]  # Noncompliant
  #                               ^^^^^^
}

resource "google_compute_firewall" "s6321-compliant1" {

  allow {
    protocol = "tcp"
    ports    = ["21"]  # Compliant because not 22 or 3389
  }

  allow {
    protocol = "tcp"
    ports    = ["30-50"]  # Compliant because does not contain 22 or 3389
  }

  allow {
    protocol = "udp"  # Compliant because not tcp
    ports    = ["22"]
  }

  source_ranges = ["0.0.0.0/0"]
}

resource "google_compute_firewall" "s6321-compliant2" {

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_tags = ["compliant"]  # Compliant because no source_ranges, only source_tags
}

resource "google_compute_firewall" "s6321-compliant3" {
  # This case can be ignored since source_ranges is missing.

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  direction = "EGRESS"  # Compliant because not INGRESS
}

resource "google_compute_firewall" "s6321-compliant4" {

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_ranges = ["10.0.0.1/32"]  # Compliant because limited
}

resource "google_compute_firewall" "s6321-compliant5" {

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  disabled = true  # Compliant because disabled
  source_ranges = ["0.0.0.0/0"]
}
