# SHARED CANDIDATE
def artifactory_env():
    """
    Provides typical environment variables to work with  Artifactory.

    The following credentials are provided:
    - private reader
    - qa deployer

    Most of the values are fetched from the Sonar Vault.

    :return: a dictionary with the following keys:
        - ARTIFACTORY_URL
        - ARTIFACTORY_PRIVATE_USERNAME
        - ARTIFACTORY_PRIVATE_PASSWORD
        - ARTIFACTORY_DEPLOY_USERNAME
        - ARTIFACTORY_DEPLOY_PASSWORD
        - ARTIFACTORY_DEPLOY_REPO
        - ARTIFACTORY_ACCESS_TOKEN
    """
    return {
        "ARTIFACTORY_URL": "VAULT[development/kv/data/repox data.url]",
        "ARTIFACTORY_PRIVATE_USERNAME": "vault-${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-private-reader",
        "ARTIFACTORY_PRIVATE_PASSWORD": "VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-private-reader access_token]",
        "ARTIFACTORY_DEPLOY_USERNAME": "vault-${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-qa-deployer",
        "ARTIFACTORY_DEPLOY_PASSWORD": "VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-qa-deployer access_token]",
        "ARTIFACTORY_DEPLOY_REPO": "sonarsource-public-qa",
        "ARTIFACTORY_ACCESS_TOKEN": "VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-private-reader access_token]",
    }


# SHARED CANDIDATE
# Maybe CIRRUS_CLONE_DEPTH should be a parameter with a default value set to 1
def cirrus_env():
    """
    Provides typical environment variables to work with Cirrus CI.

    The following default values are provided:
    - CIRRUS_SHELL: bash
    - CIRRUS_CLONE_DEPTH: 1

    :return: a dictionary with the following keys:
        - CIRRUS_SHELL
        - CIRRUS_CLONE_DEPTH
    """
    return {
        "CIRRUS_SHELL": "bash",
        "CIRRUS_CLONE_DEPTH": 1,
    }


# SHARED CANDIDATE
def pgp_signing_env():
    """
    Provides the environment variables to sign artifacts with PGP.

    Values are fetched from the Sonar Vault.

    :return: a dictionary with the following keys:
        - SIGN_KEY
        - PGP_PASSPHRASE
    """
    return {
        "SIGN_KEY": "VAULT[development/kv/data/sign data.key]",
        "PGP_PASSPHRASE": "VAULT[development/kv/data/sign data.passphrase]",
    }


# SHARED CANDIDATE
def gradle_signing_env():
    """
    Provides the environment variables to sign artifacts with Gradle.
    Values are fetched from the Sonar Vault.

    :return: a dictionary with the following keys:
        - ORG_GRADLE_PROJECT_signingKey
        - ORG_GRADLE_PROJECT_signingPassword
        - ORG_GRADLE_PROJECT_signingKeyId
    """
    return {
        "ORG_GRADLE_PROJECT_signingKey": "VAULT[development/kv/data/sign data.key]",
        "ORG_GRADLE_PROJECT_signingPassword": "VAULT[development/kv/data/sign data.passphrase]",
        "ORG_GRADLE_PROJECT_signingKeyId": "0x7DCD4258",
    }


# SHARED CANDIDATE
def whitesource_api_env():
    """
    Provides the environment variables to interact with the WhiteSource API.
    Values are fetched from the Sonar Vault.

    :return: a dictionary with the following keys:
        - WS_APIKEY
    """
    return {
        "WS_APIKEY": "VAULT[development/kv/data/mend data.apikey]"
    }


def gradle_env():
    """
    Provides typical environment variables to work with Gradle.
    The following default values are provided:
    - GRADLE_USER_HOME: ${CIRRUS_WORKING_DIR}/.gradle
    - GRADLE_COMMON_FLAGS: --console plain --no-daemon --profile

    :return: a dictionary with the following keys:
        - GRADLE_USER_HOME
        - GRADLE_COMMON_FLAGS
    """
    gradle_base = {
        "GRADLE_USER_HOME": "${CIRRUS_WORKING_DIR}/.gradle",
        "GRADLE_COMMON_FLAGS": "--console plain --no-daemon --profile"
    }
    return gradle_base | gradle_signing_env()


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


# SHARED CANDIDATE
def sonarcloud_env():
    """
    Provides typical environment variables to work with SonarCloud.
    The following default values are provided:
    - SONAR_HOST_URL: https://sonarcloud.io
    - SONAR_TOKEN: from the Sonar Vault

    :return: a dictionary with the following keys:
        - SONAR_HOST_URL
        - SONAR_TOKEN
    """
    return {
        "SONAR_HOST_URL": "https://sonarcloud.io",
        "SONAR_TOKEN": "VAULT[development/kv/data/sonarcloud data.token]",
    }


# SHARED CANDIDATE
def promotion_env():
    """
    Provides typical environment variables to promote artifacts.
    Values are fetched from the Sonar Vault.

    :return: a dictionary with the following keys:
        - ARTIFACTORY_PROMOTE_ACCESS_TOKEN
        - GITHUB_TOKEN
        - BURGR_URL
        - BURGR_USERNAME
        - BURGR_PASSWORD
    """
    return {
        "ARTIFACTORY_PROMOTE_ACCESS_TOKEN": "VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-promoter access_token]",
        "GITHUB_TOKEN": "VAULT[development/github/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-promotion token]",
        "BURGR_URL": "VAULT[development/kv/data/burgr data.url]",
        "BURGR_USERNAME": "VAULT[development/kv/data/burgr data.cirrus_username]",
        "BURGR_PASSWORD": "VAULT[development/kv/data/burgr data.cirrus_password]",
    }


def project_version_env():
    return {
        "PROJECT_VERSION_CACHE_DIR": "project-version",
    }


def env():
    vars = artifactory_env()
    vars |= cirrus_env()
    vars |= gradle_env()
    vars |= go_env()
    vars |= project_version_env()
    return {"env": vars}
