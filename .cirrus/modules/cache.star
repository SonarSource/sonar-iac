# TODO: move this to the shared library
def gradle_wrapper_cache():
    return {
        "populate_script": "mkdir -p \"${CIRRUS_WORKING_DIR}/.gradle\"",
        "folder": "${CIRRUS_WORKING_DIR}/.gradle/wrapper/dists",
        "fingerprint_script": "md5sum gradle/wrapper/gradle-wrapper.properties",
        "reupload_on_changes": "true"
    }
