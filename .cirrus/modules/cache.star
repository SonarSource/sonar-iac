#
# Gradle cache
#
# SHARED CANDIDATE
def gradle_cache(
    fingerprint_script="git rev-parse HEAD",
    reupload_on_changes=True
):
    """
    Gradle cache configuration to assign to a "<your name>_cache" key in the target task.

    The following default values are used:
    - cache folder is ${GRADLE_USER_HOME}.GRADLE_USER_HOME is an environment variable that points to the Gradle user home folder,
    it's defined by the gradle_env() function.
    - fingerprint script is git rev-parse HEAD
    - reupload_on_changes: True

    :return: the cache configuration for Gradle
    """
    return {
        "folder": "${GRADLE_USER_HOME}/caches",
        "fingerprint_script": fingerprint_script,
        "reupload_on_changes": reupload_on_changes,
        "populate_script": "mkdir -p ${GRADLE_USER_HOME}",
    }


# SHARED CANDIDATE
def cleanup_gradle_script():
    """
    Script to purge Gradle cache journal and 8.* files we don't need to push into the Cirrus cache.
    
    :return: the script to execute
    """
    return [
        "/usr/bin/find \"${GRADLE_USER_HOME}/caches\" -type d -name \"8.*\" -prune -maxdepth 1 -exec rm -rf {} \\;",
        "rm -rf \"${GRADLE_USER_HOME}/caches/journal-1/\""
    ]


#
# Orchestrator cache
#
# SHARED CANDIDATE
def mkdir_orchestrator_home_script():
    return "mkdir -p ${ORCHESTRATOR_HOME}"


# SHARED CANDIDATE
def set_orchestrator_home_script():
    return [
        "export TODAY=$(date '+%Y-%m-%d')", 'echo "TODAY=${TODAY}" >> $CIRRUS_ENV',
        'echo "ORCHESTRATOR_HOME=${CIRRUS_WORKING_DIR}/orchestrator/${TODAY}" >> $CIRRUS_ENV',
    ]


# SHARED CANDIDATE
def orchestrator_cache(
    fingerprint_script="echo ${TODAY}",
    reupload_on_changes=True
):
    """
    Orchestrator cache configuration.

    This definition must be preceded by the definition of the orchestrator home folder provided by the
    set_orchestrator_home_script() and mkdir_orchestrator_home_script() functions.

    The cache folder suffix is the current date in the format YYYY-MM-DD.

    The cache is renewed every day.
    """
    return {
        "folder": "${ORCHESTRATOR_HOME}",
        "fingerprint_script": fingerprint_script,
        "reupload_on_changes": reupload_on_changes,
    }


#
# Project version cache
#


def project_version_cache():
    return {
        "folder": "$PROJECT_VERSION_CACHE_DIR",
        "fingerprint_script": "echo $BUILD_NUMBER",
        "populate_script": "mkdir -p \"$PROJECT_VERSION_CACHE_DIR\"\ntouch \"$PROJECT_VERSION_CACHE_DIR\"/evaluated_project_version.txt\n",
        "reupload_on_changes": "true"
    }


def store_project_version_script():
    return [
        "source ~/.profile",
        "echo \"export PROJECT_VERSION=${PROJECT_VERSION}\" > ${PROJECT_VERSION_CACHE_DIR}/evaluated_project_version.txt"
    ]
