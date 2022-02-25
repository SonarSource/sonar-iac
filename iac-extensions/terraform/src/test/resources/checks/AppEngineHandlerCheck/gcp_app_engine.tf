resource "google_app_engine_standard_app_version" "noncompliant1" {
  handlers {
    security_level = "SECURE_ALWAYS"
  }

  handlers {
    security_level = "SECURE_DEFAULT" # Noncompliant {{Make sure creating a App Engine handler without requiring TLS is safe here.}}
  # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }

  handlers {
    security_level = "SECURE_NEVER" # Noncompliant
  }

  handlers {
    security_level = "SECURE_OPTIONAL" # Noncompliant
  }

  handlers {
    security_level = var.app_handler_security_level
  }

  handlers {} # Noncompliant {{Omitting security_level allows unencrypted connections to the App Engine. Make sure it is safe here.}}
# ^^^^^^^^
}

resource "google_app_engine_flexible_app_version" "noncompliant2" {
  handlers {
    security_level = "SECURE_DEFAULT" # Noncompliant
  }
}

resource "non_google_app_engine_flexible_app_version" "compliant" {
  handlers {
    security_level = "SECURE_DEFAULT"
  }
}
