# Noncompliant@+1 {{Omitting settings allows unencrypted connections to the database. Make sure it is safe here.}}
resource "google_sql_database_instance" "noncompliant1" {
  #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}

resource "google_sql_database_instance" "noncompliant2" {
  settings { # Noncompliant {{Omitting ip_configuration allows unencrypted connections to the database. Make sure it is safe here.}}
# ^^^^^^^^
  }
}

resource "google_sql_database_instance" "noncompliant3" {
  settings {
    ip_configuration { # Noncompliant {{Omitting require_ssl allows unencrypted connections to the database. Make sure it is safe here.}}
  # ^^^^^^^^^^^^^^^^
    }
  }
}


resource "google_sql_database_instance" "noncompliant4" {
  settings {
    ip_configuration {
      require_ssl = false # Noncompliant {{Make sure creating a GCP SQL instance without requiring TLS is safe here.}}
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
