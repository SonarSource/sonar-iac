def setup_orchestrator_cache():
    return {
        "set_orchestrator_home_script": "export TODAY=$(date '+%Y-%m-%d')\necho \"TODAY=${TODAY}\" >> $CIRRUS_ENV\necho \"ORCHESTRATOR_HOME=${CIRRUS_WORKING_DIR}/orchestrator/${TODAY}\" >> $CIRRUS_ENV\n",
        "mkdir_orchestrator_home_script": "echo \"Create dir ${ORCHESTRATOR_HOME} if needed\"\nmkdir -p ${ORCHESTRATOR_HOME}\n",
        "orchestrator_cache": {
            "folder": "${ORCHESTRATOR_HOME}",
            "fingerprint_script": "echo ${TODAY}",
            "reupload_on_changes": "true"
        }
    }


def setup_gradle_cache():
    return {
        "create_gradle_directory_script": [
            "mkdir -p ${GRADLE_USER_HOME}"
        ],
        "gradle_cache": {
            "folder": ".gradle/caches",
            "fingerprint_script": "git rev-parse HEAD",
            "reupload_on_changes": "true"
        }
    }


def cleanup_gradle_cache():
    return {
        "cleanup_gradle_script": [
            "/usr/bin/find \"${CIRRUS_WORKING_DIR}/.gradle/caches\" -type d -name \"8.*\" -prune -maxdepth 1 -exec rm -rf {} \\;",
            "rm -rf \"${CIRRUS_WORKING_DIR}/.gradle/caches/journal-1/\""
        ]
    }
