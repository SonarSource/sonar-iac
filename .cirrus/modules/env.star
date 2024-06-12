load(
    "github.com/SonarSource/cirrus-modules/cloud-native/env.star@analysis/master",
    "artifactory_env",
    "cirrus_env",
    "gradle_env",
)


def go_env():
    """
    Provides typical environment variables to work with Go.
    The following default values are provided:
    - GO_VERSION: 1.21.7
    - GO_CROSS_COMPILE: 1
    - PROTOC_VERSION: 25.0

    :return: a dictionary with the following keys:
        - GO_VERSION
        - GO_CROSS_COMPILE
        - PROTOC_VERSION
    """
    return {
        "GO_VERSION": "1.21.7",
        "GO_CROSS_COMPILE": "1",
        "PROTOC_VERSION": "25.0",
    }

def project_version_env():
    return {
        "PROJECT_VERSION_CACHE_DIR": "project-version",
    }


def env():
    vars = artifactory_env()
    vars |= cirrus_env(depth=1)
    vars |= gradle_env()
    vars |= go_env()
    vars |= project_version_env()
    return {"env": vars}
