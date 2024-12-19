def is_rule_metadata_or_docs_update_pr():
    return ("$CIRRUS_PR != \"\" && changesIncludeOnly(" +
            "\"**/src/main/resources/org/sonar/l10n/*/rules/**\"," +
            "\"**/src/main/resources/com/sonar/l10n/*/rules/**\"," +
            "\"**/sonarpedia.json\"," +
            "\"**/*.md\")")
