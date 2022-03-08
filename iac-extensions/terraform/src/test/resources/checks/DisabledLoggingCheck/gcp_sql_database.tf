resource "google_sql_database_instance" "sql-noncompliant" { # Noncompliant {{Omitting settings makes logs incomplete. Make sure it is safe here.}}
}

resource "google_sql_database_instance" "sql-noncompliant" {
  settings { # Noncompliant {{Omitting database_flags.log_connections makes logs incomplete. Make sure it is safe here.}}
  }
}

resource "google_sql_database_instance" "sql-noncompliant" {
  settings {
    database_flags {
      name  = "log_connections"
      value = "off" # Noncompliant {{Make sure that disabling logging is safe here.}}
    }
    database_flags {
      name  = "log_disconnections"
      value = "off" # Noncompliant
    }
    database_flags {
      name  = "log_checkpoints"
      value = "off" # Noncompliant
    }
    database_flags {
      name  = "log_lock_waits"
      value = "off" # Noncompliant
    }
  }
}

resource "google_sql_database_instance" "sql-noncompliant" {
  settings { # Noncompliant {{Omitting database_flags.log_disconnections makes logs incomplete. Make sure it is safe here.}}
    database_flags {
      name  = "log_connections"
      value = "on"
    }

    database_flags {
      value = "off"
    }
  }
}

resource "google_sql_database_instance" "sql-noncompliant" {
  settings { # Noncompliant {{Omitting database_flags.log_checkpoints makes logs incomplete. Make sure it is safe here.}}
    database_flags {
      name  = "log_connections"
      value = "on"
    }
    database_flags {
      name  = "log_disconnections"
      value = "on"
    }
  }
}

resource "google_sql_database_instance" "sql-noncompliant" {
  settings { # Noncompliant {{Omitting database_flags.log_connections makes logs incomplete. Make sure it is safe here.}}
    database_flags {
      name  = "log_checkpoints"
      value = "off" # Noncompliant
    }
    database_flags {
      name  = "log_lock_waits"
      value = "off" # Noncompliant
    }

    database_flags {
      name  = "log_disconnections"
      value = "not_on" # Noncompliant
    }
  }
}

resource "google_sql_database_instance" "sql-compliant" {
  settings {
    database_flags {
      name  = "log_connections"
      value = "on"
    }
    database_flags {
      name  = "log_disconnections"
      value = "on"
    }
    database_flags {
      name  = "log_checkpoints"
      value = "on"
    }
    database_flags {
      name  = "log_lock_waits"
      value = "on"
    }
    database_flags {
      name  = "something_else"
      value = "off"
    }
    database_flags {
      name  = "log_connections"
      value = var.log_connections
    }
  }
}
