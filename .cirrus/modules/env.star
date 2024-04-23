
def artifactory_env():
    return {
        "ARTIFACTORY_URL": "VAULT[development/kv/data/repox data.url]",
        "ARTIFACTORY_PRIVATE_USERNAME": "vault-${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-private-reader",
        "ARTIFACTORY_PRIVATE_PASSWORD": "VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-private-reader access_token]",
        "ARTIFACTORY_DEPLOY_USERNAME": "vault-${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-qa-deployer",
        "ARTIFACTORY_DEPLOY_PASSWORD": "VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-qa-deployer access_token]",
        "ARTIFACTORY_DEPLOY_REPO": "sonarsource-public-qa",
        "ARTIFACTORY_ACCESS_TOKEN": "VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-private-reader access_token]",
    }


def gradle_env():
    return {
        "GRADLE_USER_HOME": "${CIRRUS_WORKING_DIR}/.gradle",
        "GRADLE_COMMON_FLAGS": "--console plain --no-daemon --profile",
        "ORG_GRADLE_PROJECT_signingKey": "VAULT[development/kv/data/sign data.key]",
        "ORG_GRADLE_PROJECT_signingPassword": "VAULT[development/kv/data/sign data.passphrase]",
        "ORG_GRADLE_PROJECT_signingKeyId": "0x7DCD4258",
    }


def go_env():
    return {
        "GO_VERSION": "1.21.7",
        "GO_CROSS_COMPILE": "1",
        "PROTOC_VERSION": "25.0",
    }


def cirrus_env():
    return {
        "CIRRUS_SHELL": "bash",
        "CIRRUS_CLONE_DEPTH": 1,
    }


def project_version_env():
    return {
        "PROJECT_VERSION_CACHE_DIR": "project-version",
    }


def env():
    vars = artifactory_env()
    vars |= gradle_env()
    vars |= go_env()
    vars |= cirrus_env()
    vars |= project_version_env()
    return {"env": vars}
