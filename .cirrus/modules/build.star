load(
    "github.com/SonarSource/cirrus-modules/cloud-native/env.star@analysis/master",
    "pgp_signing_env",
    "next_env",
    "whitesource_api_env"
)

load(
    "github.com/SonarSource/cirrus-modules/cloud-native/conditions.star@analysis/master",
    "is_main_branch",
    "is_branch_qa_eligible"
)
load(
    "github.com/SonarSource/cirrus-modules/cloud-native/platform.star@analysis/master",
    "base_image_container_builder",
    "custom_image_container_builder",
)
load(
    "github.com/SonarSource/cirrus-modules/cloud-native/cache.star@analysis/master",
    "gradle_cache",
    "cleanup_gradle_script",
    "gradle_wrapper_cache",
    "go_build_cache",
    "project_version_cache",
)
load("cache.star", "gradle_cache_fingerprint_script")


#
# Common
#

def profile_report_artifacts():
    return {
        "profile_report_artifacts": {
            "path": "build/reports/profile/profile-*.html"
        }
    }


#
# Build
#

def build_script():
    return [
        "git submodule update --init --depth 1 -- build-logic/common",
        "source cirrus-env BUILD-PRIVATE",
        "source .cirrus/use-gradle-wrapper.sh",
        "regular_gradle_build_deploy_analyze ${BUILD_ARGUMENTS}",
        "echo 'Checking if any files are uncommitted in the Go code (this may happen to the generated code). In case of of failure, run ./gradlew generateProto locally and commit the generated files.'",
        "git diff --exit-code --name-only -- sonar-helm-for-iac/",
    ]


def build_env():
    env = pgp_signing_env()
    env |= next_env()
    env |= {
        "DEPLOY_PULL_REQUEST": "true",
        "BUILD_ARGUMENTS": "-DtrafficInspection=false --parallel --profile -x test -x sonar storeProjectVersion"
    }
    return env


def build_task():
    return {
        "build_task": {
            "env": build_env(),
            "eks_container": custom_image_container_builder(dockerfile="build-logic/iac/Dockerfile", cpu=10, memory="6G"),
            "project_version_cache": project_version_cache(),
            "gradle_cache": gradle_cache(fingerprint_script=gradle_cache_fingerprint_script()),
            "gradle_wrapper_cache": gradle_wrapper_cache(),
            "go_build_cache": go_build_cache(go_src_dir="${CIRRUS_WORKING_DIR}/sonar-helm-for-iac"),
            "build_script": build_script(),
            "cleanup_gradle_script": cleanup_gradle_script(),
            "on_success": profile_report_artifacts(),
        }
    }


#
# Build test analyze
#

def build_test_env():
    env = pgp_signing_env()
    env |= next_env()
    env |= {
        "DEPLOY_PULL_REQUEST": "false",
        # --no-parallel because as of 6.2.0.5505 sonar-scanner-gradle doesn't work well with Gradle 9
        # see https://community.sonarsource.com/t/error-when-running-sonar-task-with-gradle-9-0-0-rc-1/143857/12
        "BUILD_ARGUMENTS": "-x artifactoryPublish --no-parallel",
        "SONAR_PROJECT_KEY": "org.sonarsource.iac:iac"
    }
    return env


def build_test_analyze_task():
    return {
        "build_test_analyze_task": {
            "only_if": is_branch_qa_eligible(),
            "depends_on": "build",
            "env": build_test_env(),
            "eks_container": custom_image_container_builder(dockerfile="build-logic/iac/Dockerfile", cpu=6, memory="10G"),
            "gradle_cache": gradle_cache(fingerprint_script=gradle_cache_fingerprint_script()),
            "gradle_wrapper_cache": gradle_wrapper_cache(),
            "go_build_cache": go_build_cache(go_src_dir="${CIRRUS_WORKING_DIR}/sonar-helm-for-iac"),
            "build_script": build_script(),
            "on_failure": {
                "junit_artifacts": {
                    "path": "**/test-results/**/*.xml",
                    "format": "junit"
                }
            },
            "cleanup_gradle_script": cleanup_gradle_script(),
        }
    }


#
# Shadow Scans
#

def is_run_shadow_scan():
    return "($CIRRUS_CRON == $CRON_NIGHTLY_JOB_NAME && $CIRRUS_BRANCH == \"master\") || $CIRRUS_PR_LABELS =~ \".*shadow_scan.*\""

def shadow_scan_general_env():
    env = pgp_signing_env()
    env |= {
        "DEPLOY_PULL_REQUEST": "false",
        # --no-parallel because as of 6.2.0.5505 sonar-scanner-gradle doesn't work well with Gradle 9
        # see https://community.sonarsource.com/t/error-when-running-sonar-task-with-gradle-9-0-0-rc-1/143857/12
        "BUILD_ARGUMENTS": "-x test -x artifactoryPublish --no-parallel",
        "CRON_NIGHTLY_JOB_NAME": "nightly",
        "SONAR_PROJECT_KEY": "SonarSource_sonar-iac-enterprise"
    }
    return env

def shadow_scan_task_template(env):
    return {
        "only_if": "({}) && ({})".format(is_branch_qa_eligible(), is_run_shadow_scan()),
        "depends_on": "build",
        "env": env,
        "eks_container": custom_image_container_builder(dockerfile="build-logic/iac/Dockerfile", cpu=6, memory="10G"),
        "gradle_cache": gradle_cache(fingerprint_script=gradle_cache_fingerprint_script()),
        "gradle_wrapper_cache": gradle_wrapper_cache(),
        "go_build_cache": go_build_cache(go_src_dir="${CIRRUS_WORKING_DIR}/sonar-helm-for-iac"),
        "build_script": build_script(),
        "cleanup_gradle_script": cleanup_gradle_script(),
    }

def shadow_scan_sqc_eu_env():
    env = shadow_scan_general_env()
    env |= {
       "SONAR_TOKEN": "VAULT[development/kv/data/sonarcloud data.token]",
       "SONAR_HOST_URL": "https://sonarcloud.io"
    }
    return env

# Overlap with build_task, because of imminent GHA migration we don't take any effort to abstract it
def shadow_scan_sqc_eu_task():
    return {
        "shadow_scan_sqc_eu_task": shadow_scan_task_template(shadow_scan_sqc_eu_env())
    }


def shadow_scan_sqc_us_env():
    env = shadow_scan_general_env()
    env |= {
       "SONAR_TOKEN": "VAULT[development/kv/data/sonarqube-us data.token]",
       "SONAR_HOST_URL": "https://sonarqube.us"
    }
    return env

# Overlap with build_task, because of imminent GHA migration we don't take any effort to abstract it
def shadow_scan_sqc_us_task():
    return {
        "shadow_scan_sqc_us_task": shadow_scan_task_template(shadow_scan_sqc_us_env())
    }

#
# Iris
#

def iris_general_env():
    return {
       "SONAR_SOURCE_IRIS_TOKEN": "VAULT[development/kv/data/iris data.next]",
       "SONAR_SOURCE_PROJECT_KEY": "org.sonarsource.iac:iac",
    }

def run_iris_task_template(env):
    return {
        "only_if": "({}) && ({})".format(is_branch_qa_eligible(), is_run_shadow_scan()),
        "depends_on": "promote",
        "env": env,
        "eks_container": base_image_container_builder(cpu=2, memory="2G"),
        "build_script": [
                        "./run_iris.sh"
                    ],
    }

# Next Enterprise -> SQC EU Enterprise

def iris_next_enterprise_to_sqc_eu_enterprise_env():
    env = iris_general_env()
    env |= {
       "SONAR_TARGET_URL": "https://sonarcloud.io",
       "SONAR_TARGET_IRIS_TOKEN": "VAULT[development/kv/data/iris data.sqc-eu]",
       "SONAR_TARGET_PROJECT_KEY": "SonarSource_sonar-iac-enterprise",
    }
    return env

def run_iris_next_enterprise_to_sqc_eu_enterprise_task():
    return {
        "run_iris_next_enterprise_to_sqc_eu_enterprise_task": run_iris_task_template(iris_next_enterprise_to_sqc_eu_enterprise_env())
    }

# Next Enterprise -> SQC EU Public

def iris_next_enterprise_to_sqc_eu_public_env():
    env = iris_general_env()
    env |= {
       "SONAR_TARGET_URL": "https://sonarcloud.io",
       "SONAR_TARGET_IRIS_TOKEN": "VAULT[development/kv/data/iris data.sqc-eu]",
       "SONAR_TARGET_PROJECT_KEY": "org.sonarsource.iac:iac",
    }
    return env

def run_iris_next_enterprise_to_sqc_eu_public_task():
    return {
        "run_iris_next_enterprise_to_sqc_eu_public_task": run_iris_task_template(iris_next_enterprise_to_sqc_eu_public_env())
    }

# Next Enterprise -> SQC US Enterprise

def iris_next_enterprise_to_sqc_us_enterprise_env():
    env = iris_general_env()
    env |= {
       "SONAR_TARGET_URL": "https://sonarqube.us",
       "SONAR_TARGET_IRIS_TOKEN": "VAULT[development/kv/data/iris data.sqc-us]",
       "SONAR_TARGET_PROJECT_KEY": "SonarSource_sonar-iac-enterprise",
    }
    return env

def run_iris_next_enterprise_to_sqc_us_enterprise_task():
    return {
        "run_iris_next_enterprise_to_sqc_us_enterprise_task": run_iris_task_template(iris_next_enterprise_to_sqc_us_enterprise_env())
    }

# Next Enterprise -> SQC US Public

def iris_next_enterprise_to_sqc_us_public_env():
    env = iris_general_env()
    env |= {
       "SONAR_TARGET_URL": "https://sonarqube.us",
       "SONAR_TARGET_IRIS_TOKEN": "VAULT[development/kv/data/iris data.sqc-eu]",
       "SONAR_TARGET_PROJECT_KEY": "SonarSource_sonar-iac",
    }
    return env

def run_iris_next_enterprise_to_sqc_us_public_task():
    return {
        "run_iris_next_enterprise_to_sqc_us_public_task": run_iris_task_template(iris_next_enterprise_to_sqc_us_public_env())
    }

# Next Enterprise -> Next Public

def iris_next_enterprise_to_next_public_env():
    env = iris_general_env()
    env |= {
       "SONAR_TARGET_URL": "https://next.sonarqube.com/sonarqube",
       "SONAR_TARGET_IRIS_TOKEN": "VAULT[development/kv/data/iris data.next]",
       "SONAR_TARGET_PROJECT_KEY": "SonarSource_sonar-iac",
    }
    return env

def run_iris_next_enterprise_to_next_public_task():
    return {
        "run_iris_next_enterprise_to_next_public_task": run_iris_task_template(iris_next_enterprise_to_next_public_env())
    }


#
# Whitesource scan
#
# SHARED CANDIDATE???
# Some bits depend on the project: memory options, gradle task
def whitesource_script():
    return [
        "git submodule update --init --depth 1 -- build-logic/common",
        "source cirrus-env QA",
        "source .cirrus/use-gradle-wrapper.sh",
        "export PROJECT_VERSION=$(cat ${PROJECT_VERSION_CACHE_DIR}/evaluated_project_version.txt)",
        "GRADLE_OPTS=\"-Xmx64m -Dorg.gradle.jvmargs='-Xmx3G' -Dorg.gradle.daemon=false\" ./gradlew ${GRADLE_COMMON_FLAGS} :iac-common:processResources -Pkotlin.compiler.execution.strategy=in-process",
        "source ws_scan.sh -d \"${PWD},${PWD}/sonar-helm-for-iac\""
    ]


# SHARED CANDIDATE???
# Some bits depend on the project: project version cache, on success profile report artifacts
def sca_scan_task():
    return {
        "sca_scan_task": {
            "only_if": is_main_branch(),
            "depends_on": "build",
            "env": whitesource_api_env(),
            "eks_container": custom_image_container_builder(dockerfile="build-logic/iac/Dockerfile"),
            "gradle_cache": gradle_cache(fingerprint_script=gradle_cache_fingerprint_script()),
            "gradle_wrapper_cache": gradle_wrapper_cache(),
            "go_build_cache": go_build_cache(go_src_dir="${CIRRUS_WORKING_DIR}/sonar-helm-for-iac"),
            "project_version_cache": project_version_cache(),
            "whitesource_script": whitesource_script(),
            "cleanup_gradle_script": cleanup_gradle_script(),
            "allow_failures": "true",
            "always": {
                "ws_artifacts": {
                    "path": "whitesource/**/*"
                }
            },
            "on_success": profile_report_artifacts(),
        }
    }
