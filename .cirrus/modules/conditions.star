def is_rule_metadata_update_pr():
    return ("$CIRRUS_PR != \"\" && changesIncludeOnly(\"iac-extensions/*/src/main/resources/org/sonar/l10n/*/rules/**\"," +
            "\"iac-extensions/*/sonarpedia.json\",\".cirrus/**\")")
