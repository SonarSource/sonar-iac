resource "google_sql_database_instance" "s6329-sdi-noncompliant1" {
  settings {
    ip_configuration {
      ipv4_enabled = true  # Noncompliant {{Make sure allowing public network access is safe here.}}
    # ^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource "google_sql_database_instance" "s6329-sdi-compliant1" {
  settings {
    ip_configuration {
      ipv4_enabled = false
    }
  }
}

resource "google_sql_database_instance" "s6329-sdi-compliant2" {
  settings {
    ip_configuration {
    }
  }
}

resource "google_sql_database_instance" "s6329-sdi-compliant3" {
  settings {
  }
}

resource "google_sql_database_instance" "s6329-sdi-compliant4" {
}

resource "non_google_sql_database_instance" "s6329-sdi-coverage" {
  settings {
    ip_configuration {
      ipv4_enabled = true
    }
  }
}
