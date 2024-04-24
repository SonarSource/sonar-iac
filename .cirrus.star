# Check Starlark specs: https://github.com/bazelbuild/starlark/blob/master/spec.md
# Cirrus
load("cirrus", "yaml", "fs")
# RE
load("github.com/SonarSource/cirrus-modules@v2", "load_features")


def merge_dict(target, source):
    for key in source.keys():
        if target.get(key) == None:
            target.update({key: source[key]})
        else:
            target[key].update(source[key])


def env():
    return {
        "env": {
            "ARTIFACTORY_URL": "VAULT[development/kv/data/repox data.url]",
            "ARTIFACTORY_PRIVATE_USERNAME": "vault-${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-private-reader",
            "ARTIFACTORY_PRIVATE_PASSWORD": "VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-private-reader access_token]",
            "ARTIFACTORY_DEPLOY_USERNAME": "vault-${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-qa-deployer",
            "ARTIFACTORY_DEPLOY_PASSWORD": "VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-qa-deployer access_token]",
            "ARTIFACTORY_DEPLOY_REPO": "sonarsource-public-qa",
            "ARTIFACTORY_ACCESS_TOKEN": "VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-private-reader access_token]",
            "CIRRUS_SHELL": "bash",
            "CIRRUS_CLONE_DEPTH": 1,
            "GRADLE_USER_HOME": "${CIRRUS_WORKING_DIR}/.gradle",
            "GRADLE_COMMON_FLAGS": "--console plain --no-daemon --profile",
            "GO_VERSION": "1.21.7",
            "GO_CROSS_COMPILE": "1",
            "PROTOC_VERSION": "25.0",
            "ORG_GRADLE_PROJECT_signingKey": "VAULT[development/kv/data/sign data.key]",
            "ORG_GRADLE_PROJECT_signingPassword": "VAULT[development/kv/data/sign data.passphrase]",
            "ORG_GRADLE_PROJECT_signingKeyId": "0x7DCD4258",
            "PROJECT_VERSION_CACHE_DIR": "project-version"
        }
    }


def build_task():
    return {
        "build_task": {
            "env": {
                "SIGN_KEY": "VAULT[development/kv/data/sign data.key]",
                "PGP_PASSPHRASE": "VAULT[development/kv/data/sign data.passphrase]",
                "SONAR_HOST_URL": "https://sonarcloud.io",
                "SONAR_TOKEN": "VAULT[development/kv/data/sonarcloud data.token]",
                "DEPLOY_PULL_REQUEST": "true",
                "BUILD_ARGUMENTS": "-DtrafficInspection=false --parallel --profile -x test -x sonar"
            },
            "eks_container": {
                "dockerfile": ".cirrus/Dockerfile",
                "docker_arguments": {
                    "CIRRUS_AWS_ACCOUNT": "${CIRRUS_AWS_ACCOUNT}",
                    "GO_VERSION": "${GO_VERSION}",
                    "PROTOC_VERSION": "${PROTOC_VERSION}"
                },
                "cluster_name": "${CIRRUS_CLUSTER_NAME}",
                "builder_role": "cirrus-builder",
                "builder_image": "docker-builder-v*",
                "builder_instance_type": "t3.small",
                "builder_subnet_id": "${CIRRUS_AWS_SUBNET}",
                "region": "eu-central-1",
                "namespace": "default",
                "use_in_memory_disk": "true",
                "memory": "6G",
                "cpu": 10
            },
            "project_version_cache": {
                "folder": "$PROJECT_VERSION_CACHE_DIR",
                "fingerprint_script": "echo $BUILD_NUMBER",
                "populate_script": "mkdir -p \"$PROJECT_VERSION_CACHE_DIR\"\ntouch \"$PROJECT_VERSION_CACHE_DIR\"/evaluated_project_version.txt\n",
                "reupload_on_changes": "true"
            },
            "create_gradle_directory_script": [
                "mkdir -p ${GRADLE_USER_HOME}"
            ],
            "gradle_cache": {
                "folder": ".gradle/caches",
                "fingerprint_script": "git rev-parse HEAD",
                "reupload_on_changes": "true"
            },
            "build_script": [
                "source cirrus-env BUILD",
                "source .cirrus/use-gradle-wrapper.sh",
                "regular_gradle_build_deploy_analyze ${BUILD_ARGUMENTS}",
                "source set_gradle_build_version ${BUILD_NUMBER}",
                "echo export PROJECT_VERSION=${PROJECT_VERSION} >> ~/.profile"
            ],
            "cleanup_gradle_script": [
                "/usr/bin/find \"${CIRRUS_WORKING_DIR}/.gradle/caches\" -type d -name \"8.*\" -prune -maxdepth 1 -exec rm -rf {} \\;",
                "rm -rf \"${CIRRUS_WORKING_DIR}/.gradle/caches/journal-1/\""
            ],
            "on_success": {
                "profile_report_artifacts": {
                    "path": "build/reports/profile/profile-*.html"
                }
            },
            "store_project_version_script": [
                "source ~/.profile",
                "echo \"export PROJECT_VERSION=${PROJECT_VERSION}\" > ${PROJECT_VERSION_CACHE_DIR}/evaluated_project_version.txt"
            ]
        }
    }


def build_test_analyze_task():
    return {
        "build_test_analyze_task": {
            "only_if": "$CIRRUS_USER_COLLABORATOR == 'true' && $CIRRUS_TAG == \"\" && ($CIRRUS_PR != \"\" || $CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ \"branch-.*\" || $CIRRUS_BRANCH =~ \"dogfood-on-.*\")",
            "depends_on": [
                "build"
            ],
            "env": {
                "SIGN_KEY": "VAULT[development/kv/data/sign data.key]",
                "PGP_PASSPHRASE": "VAULT[development/kv/data/sign data.passphrase]",
                "SONAR_HOST_URL": "https://sonarcloud.io",
                "SONAR_TOKEN": "VAULT[development/kv/data/sonarcloud data.token]",
                "DEPLOY_PULL_REQUEST": "false",
                "BUILD_ARGUMENTS": "-x artifactoryPublish"
            },
            "eks_container": {
                "dockerfile": ".cirrus/Dockerfile",
                "docker_arguments": {
                    "CIRRUS_AWS_ACCOUNT": "${CIRRUS_AWS_ACCOUNT}",
                    "GO_VERSION": "${GO_VERSION}",
                    "PROTOC_VERSION": "${PROTOC_VERSION}"
                },
                "cluster_name": "${CIRRUS_CLUSTER_NAME}",
                "builder_role": "cirrus-builder",
                "builder_image": "docker-builder-v*",
                "builder_instance_type": "t3.small",
                "builder_subnet_id": "${CIRRUS_AWS_SUBNET}",
                "region": "eu-central-1",
                "namespace": "default",
                "use_in_memory_disk": "true",
                "memory": "6G",
                "cpu": 6
            },
            "create_gradle_directory_script": [
                "mkdir -p ${GRADLE_USER_HOME}"
            ],
            "gradle_cache": {
                "folder": ".gradle/caches",
                "fingerprint_script": "git rev-parse HEAD",
                "reupload_on_changes": "true"
            },
            "build_script": [
                "source cirrus-env BUILD",
                "source .cirrus/use-gradle-wrapper.sh",
                "regular_gradle_build_deploy_analyze ${BUILD_ARGUMENTS}",
                "source set_gradle_build_version ${BUILD_NUMBER}",
                "echo export PROJECT_VERSION=${PROJECT_VERSION} >> ~/.profile"
            ],
            "cleanup_gradle_script": [
                "/usr/bin/find \"${CIRRUS_WORKING_DIR}/.gradle/caches\" -type d -name \"8.*\" -prune -maxdepth 1 -exec rm -rf {} \\;",
                "rm -rf \"${CIRRUS_WORKING_DIR}/.gradle/caches/journal-1/\""
            ],
        }
    }


def qa_os_win_task():
    return {
        "qa_os_win_task": {
            "only_if": "$CIRRUS_USER_COLLABORATOR == 'true' && $CIRRUS_TAG == \"\" && ($CIRRUS_PR != \"\" || $CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ \"branch-.*\" || $CIRRUS_BRANCH =~ \"dogfood-on-.*\")",
            "depends_on": [
                "build"
            ],
            "ec2_instance": {
                "experimental": "true",
                "image": "base-windows-jdk17-v*",
                "platform": "windows",
                "region": "eu-central-1",
                "type": "c5.4xlarge",
                "subnet_id": "${CIRRUS_AWS_SUBNET}",
                "preemptible": "false",
                "use_ssd": "true"
            },
            "create_gradle_directory_script": [
                "mkdir -p ${GRADLE_USER_HOME}"
            ],
            "gradle_cache": {
                "folder": ".gradle/caches",
                "fingerprint_script": "git rev-parse HEAD",
                "reupload_on_changes": "true"
            },
            "build_script": [
                "choco install golang --version ${GO_VERSION}",
                "choco install protoc --version ${PROTOC_VERSION}.0",
                "eval $(powershell -NonInteractive -Command 'write(\"export PATH=`\"\" + ([Environment]::GetEnvironmentVariable(\"PATH\",\"Machine\") + \";\" + [Environment]::GetEnvironmentVariable(\"PATH\",\"User\")).replace(\"\\\",\"/\").replace(\"C:\",\"/c\").replace(\";\",\":\") + \":`$PATH`\"\")')",
                "source cirrus-env CI",
                "./gradlew ${GRADLE_COMMON_FLAGS} test"
            ],
            "on_success": {
                "profile_report_artifacts": {
                    "path": "build/reports/profile/profile-*.html"
                }
            },
        }
    }


def sca_scan_task():
    return {
        "sca_scan_task": {
            "only_if": "$CIRRUS_USER_COLLABORATOR == 'true' && $CIRRUS_TAG == \"\" && ($CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ \"branch-.*\")",
            "depends_on": [
                "build"
            ],
            "env": {
                "WS_APIKEY": "VAULT[development/kv/data/mend data.apikey]"
            },
            "eks_container": {
                "dockerfile": ".cirrus/Dockerfile",
                "docker_arguments": {
                    "CIRRUS_AWS_ACCOUNT": "${CIRRUS_AWS_ACCOUNT}",
                    "GO_VERSION": "${GO_VERSION}",
                    "PROTOC_VERSION": "${PROTOC_VERSION}"
                },
                "cluster_name": "${CIRRUS_CLUSTER_NAME}",
                "builder_role": "cirrus-builder",
                "builder_image": "docker-builder-v*",
                "builder_instance_type": "t3.small",
                "builder_subnet_id": "${CIRRUS_AWS_SUBNET}",
                "region": "eu-central-1",
                "namespace": "default",
                "use_in_memory_disk": "true",
                "cpu": 4,
                "memory": "8G"
            },
            "create_gradle_directory_script": [
                "mkdir -p ${GRADLE_USER_HOME}"
            ],
            "gradle_cache": {
                "folder": ".gradle/caches",
                "fingerprint_script": "git rev-parse HEAD",
                "reupload_on_changes": "true"
            },
            "project_version_cache": {
                "folder": "$PROJECT_VERSION_CACHE_DIR",
                "fingerprint_script": "echo $BUILD_NUMBER",
                "populate_script": "mkdir -p \"$PROJECT_VERSION_CACHE_DIR\"\ntouch \"$PROJECT_VERSION_CACHE_DIR\"/evaluated_project_version.txt\n",
                "reupload_on_changes": "true"
            },
            "whitesource_script": [
                "source cirrus-env QA",
                "source .cirrus/use-gradle-wrapper.sh",
                "source ${PROJECT_VERSION_CACHE_DIR}/evaluated_project_version.txt",
                "GRADLE_OPTS=\"-Xmx64m -Dorg.gradle.jvmargs='-Xmx3G' -Dorg.gradle.daemon=false\" ./gradlew ${GRADLE_COMMON_FLAGS} :iac-common:processResources -Pkotlin.compiler.execution.strategy=in-process",
                "source ws_scan.sh -d \"${PWD},${PWD}/sonar-helm-for-iac\""
            ],
            "cleanup_gradle_script": [
                "/usr/bin/find \"${CIRRUS_WORKING_DIR}/.gradle/caches\" -type d -name \"8.*\" -prune -maxdepth 1 -exec rm -rf {} \\;",
                "rm -rf \"${CIRRUS_WORKING_DIR}/.gradle/caches/journal-1/\""
            ],
            "allow_failures": "true",
            "always": {
                "ws_artifacts": {
                    "path": "whitesource/**/*"
                }
            },
            "on_success": {
                "profile_report_artifacts": {
                    "path": "build/reports/profile/profile-*.html"
                }
            },
        }
    }


def qa_plugin_task():
    return {
        "qa_plugin_task": {
            "only_if": "$CIRRUS_USER_COLLABORATOR == 'true' && $CIRRUS_TAG == \"\" && ($CIRRUS_PR != \"\" || $CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ \"branch-.*\" || $CIRRUS_BRANCH =~ \"dogfood-on-.*\")",
            "depends_on": [
                "build"
            ],
            "env": {
                "GRADLE_TASK": ":its:plugin:integrationTest",
                "matrix": [
                    {"SQ_VERSION": "LATEST_RELEASE"},
                    {"SQ_VERSION": "DEV"},
                ]
            },
            "eks_container": {
                "image": "${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest",
                "cluster_name": "${CIRRUS_CLUSTER_NAME}",
                "region": "eu-central-1",
                "namespace": "default",
                "use_in_memory_disk": "true",
                "cpu": 4,
                "memory": "8G"
            },
            "create_gradle_directory_script": [
                "mkdir -p ${GRADLE_USER_HOME}"
            ],
            "gradle_cache": {
                "folder": ".gradle/caches",
                "fingerprint_script": "git rev-parse HEAD",
                "reupload_on_changes": "true"
            },
            "set_orchestrator_home_script": "export TODAY=$(date '+%Y-%m-%d')\necho \"TODAY=${TODAY}\" >> $CIRRUS_ENV\necho \"ORCHESTRATOR_HOME=${CIRRUS_WORKING_DIR}/orchestrator/${TODAY}\" >> $CIRRUS_ENV\n",
            "mkdir_orchestrator_home_script": "echo \"Create dir ${ORCHESTRATOR_HOME} if needed\"\nmkdir -p ${ORCHESTRATOR_HOME}\n",
            "orchestrator_cache": {
                "folder": "${ORCHESTRATOR_HOME}",
                "fingerprint_script": "echo ${TODAY}",
                "reupload_on_changes": "true"
            },
            "run_its_script": [
                "git submodule update --init --depth 1",
                "source cirrus-env QA",
                "source .cirrus/use-gradle-wrapper.sh",
                "./gradlew \"${GRADLE_TASK}\" \"-Dsonar.runtimeVersion=${SQ_VERSION}\" --info --build-cache --console plain --no-daemon"
            ],
            "cleanup_gradle_script": [
                "/usr/bin/find \"${CIRRUS_WORKING_DIR}/.gradle/caches\" -type d -name \"8.*\" -prune -maxdepth 1 -exec rm -rf {} \\;",
                "rm -rf \"${CIRRUS_WORKING_DIR}/.gradle/caches/journal-1/\""
            ],
        }
    }


def qa_ruling_task():
    return {
        "qa_ruling_task": {
            "only_if": "$CIRRUS_USER_COLLABORATOR == 'true' && $CIRRUS_TAG == \"\" && ($CIRRUS_PR != \"\" || $CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ \"branch-.*\" || $CIRRUS_BRANCH =~ \"dogfood-on-.*\")",
            "depends_on": [
                "build"
            ],
            "env": {
                "GRADLE_TASK": ":its:ruling:integrationTest",
                "SQ_VERSION": "LATEST_RELEASE"
            },
            "eks_container": {
                "image": "${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest",
                "cluster_name": "${CIRRUS_CLUSTER_NAME}",
                "region": "eu-central-1",
                "namespace": "default",
                "use_in_memory_disk": "true",
                "cpu": 4,
                "memory": "8G"
            },
            "create_gradle_directory_script": [
                "mkdir -p ${GRADLE_USER_HOME}"
            ],
            "gradle_cache": {
                "folder": ".gradle/caches",
                "fingerprint_script": "git rev-parse HEAD",
                "reupload_on_changes": "true"
            },
            "set_orchestrator_home_script": "export TODAY=$(date '+%Y-%m-%d')\necho \"TODAY=${TODAY}\" >> $CIRRUS_ENV\necho \"ORCHESTRATOR_HOME=${CIRRUS_WORKING_DIR}/orchestrator/${TODAY}\" >> $CIRRUS_ENV\n",
            "mkdir_orchestrator_home_script": "echo \"Create dir ${ORCHESTRATOR_HOME} if needed\"\nmkdir -p ${ORCHESTRATOR_HOME}\n",
            "orchestrator_cache": {
                "folder": "${ORCHESTRATOR_HOME}",
                "fingerprint_script": "echo ${TODAY}",
                "reupload_on_changes": "true"
            },
            "run_its_script": [
                "git submodule update --init --depth 1",
                "source cirrus-env QA",
                "source .cirrus/use-gradle-wrapper.sh",
                "./gradlew \"${GRADLE_TASK}\" \"-Dsonar.runtimeVersion=${SQ_VERSION}\" --info --build-cache --console plain --no-daemon"
            ],
            "cleanup_gradle_script": [
                "/usr/bin/find \"${CIRRUS_WORKING_DIR}/.gradle/caches\" -type d -name \"8.*\" -prune -maxdepth 1 -exec rm -rf {} \\;",
                "rm -rf \"${CIRRUS_WORKING_DIR}/.gradle/caches/journal-1/\""
            ],
        }
    }


def promote_task():
    return {
        "promote_task": {
            "only_if": "$CIRRUS_USER_COLLABORATOR == 'true' && $CIRRUS_TAG == \"\" && ($CIRRUS_PR != \"\" || $CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ \"branch-.*\" || $CIRRUS_BRANCH =~ \"dogfood-on-.*\")",
            "depends_on": [
                "build",
                "build_test_analyze",
                "qa_os_win",
                "qa_ruling",
                "qa_plugin"
            ],
            "env": {
                "ARTIFACTORY_PROMOTE_ACCESS_TOKEN": "VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-promoter access_token]",
                "GITHUB_TOKEN": "VAULT[development/github/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-promotion token]",
                "BURGR_URL": "VAULT[development/kv/data/burgr data.url]",
                "BURGR_USERNAME": "VAULT[development/kv/data/burgr data.cirrus_username]",
                "BURGR_PASSWORD": "VAULT[development/kv/data/burgr data.cirrus_password]",
                "ARTIFACTS": "com.sonarsource.iac:sonar-iac-plugin:jar"
            },
            "eks_container": {
                "image": "${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest",
                "cluster_name": "${CIRRUS_CLUSTER_NAME}",
                "region": "eu-central-1",
                "namespace": "default",
                "use_in_memory_disk": "true",
                "cpu": 1,
                "memory": "2G"
            },
            "project_version_cache": {
                "folder": "$PROJECT_VERSION_CACHE_DIR",
                "fingerprint_script": "echo $BUILD_NUMBER",
                "populate_script": "mkdir -p \"$PROJECT_VERSION_CACHE_DIR\"\ntouch \"$PROJECT_VERSION_CACHE_DIR\"/evaluated_project_version.txt\n",
                "reupload_on_changes": "true"
            },
            "script": [
                "source cirrus-env PROMOTE",
                "cirrus_jfrog_promote multi",
                "source ${PROJECT_VERSION_CACHE_DIR}/evaluated_project_version.txt",
                "github-notify-promotion",
                "burgr-notify-promotion"
            ]
        }
    }


def main(ctx):
    conf = dict()
    merge_dict(conf, load_features(ctx))
    merge_dict(conf, env())
    merge_dict(conf, build_task())
    merge_dict(conf, build_test_analyze_task())
    merge_dict(conf, qa_os_win_task())
    merge_dict(conf, sca_scan_task())
    merge_dict(conf, qa_plugin_task())
    merge_dict(conf, qa_ruling_task())
    merge_dict(conf, promote_task())
    return conf
