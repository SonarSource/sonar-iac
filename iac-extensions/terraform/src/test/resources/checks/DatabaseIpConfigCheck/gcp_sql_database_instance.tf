# Noncompliant@+1 {{Omitting "settings.ip_configuration.ssl_mode" allows unencrypted connections to the database.}}
resource "google_sql_database_instance" "noncompliant1" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_sql_database_instance" "noncompliant2" {
  settings { # Noncompliant {{Omitting "ip_configuration.ssl_mode" allows unencrypted connections to the database.}}
# ^^^^^^^^
  }
}

resource "google_sql_database_instance" "noncompliant3" {
  settings {
    ip_configuration { # Noncompliant {{Omitting "ssl_mode" allows unencrypted connections to the database.}}
  # ^^^^^^^^^^^^^^^^
    }
  }
}


resource "google_sql_database_instance" "noncompliant4" {
  settings {
    ip_configuration {
      require_ssl = false # Noncompliant {{Change to disallow unencrypted connections.}}
    # ^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource "google_sql_database_instance" "compliant" {
  settings {
    ip_configuration {
      require_ssl = true
    }
  }
}

resource "google_sql_database_instance" "noncompliant_ssl_mode_override" {
  settings {
    ip_configuration {
      ssl_mode    = "ALLOW_UNENCRYPTED_AND_ENCRYPTED" # Noncompliant {{Change to disallow unencrypted connections.}}
    # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      require_ssl = true
    }
  }
}

resource "google_sql_database_instance" "compliant_ssl_mode" {
  settings {
    ip_configuration {
      ssl_mode    = "ENCRYPTED_ONLY"
      require_ssl = false
    }
  }
}
