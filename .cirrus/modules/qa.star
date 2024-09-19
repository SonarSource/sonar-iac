load("github.com/SonarSource/cirrus-modules/cloud-native/platform.star@analysis/master", "base_image_container_builder", "ec2_instance_builder")
load("github.com/SonarSource/cirrus-modules/cloud-native/conditions.star@analysis/master", "is_branch_qa_eligible")
load("github.com/SonarSource/cirrus-modules/cloud-native/env.star@analysis/master", "artifactory_reader_env")
load("build.star", "profile_report_artifacts")
load(
    "github.com/SonarSource/cirrus-modules/cloud-native/cache.star@analysis/master",
    "gradle_cache",
    "cleanup_gradle_script",
    "gradle_wrapper_cache",
    "orchestrator_cache",
    "set_orchestrator_home_script",
    "mkdir_orchestrator_home_script",
)

QA_PLUGIN_GRADLE_TASK = ":its:plugin:integrationTest"
QA_RULING_GRADLE_TASK = ":its:ruling:integrationTest"
QA_QUBE_LATEST_RELEASE = "LATEST_RELEASE"

def is_rule_metadata_update_pr():
    return "$CIRRUS_PR != \"\" && changesIncludeOnly(\"iac-extensions/*/src/main/resources/org/sonar/l10n/*/rules/**\", \"iac-extensions/*/sonarpedia.json\")"

def qa_win_script():
    return [
        "powershell -NonInteractive -Command 'New-Item -ItemType Directory -Path C:\\golang-dl -Force'",
        "powershell -NonInteractive -Command '(new-object System.Net.WebClient).DownloadFile(\"https://golang.org/dl/go$env:GO_VERSION.windows-386.zip\",\"C:\\golang-dl\\golang-$env:GO_VERSION.zip\")'",
        "eval $(powershell -NonInteractive -Command '(Get-FileHash C:\\golang-dl\\golang-$env:GO_VERSION.zip).Hash -eq \"872ac1c6ba1e23927a5cd60ce2e7a9e64cc6e5a550334c0fbcc785b4347d5f0d\"')",
        "eval $(powershell -NonInteractive -Command 'Add-Type -Assembly \"System.IO.Compression.Filesystem\"; [System.IO.Compression.ZipFile]::ExtractToDirectory(\"C:\\golang-dl\\golang-$env:GO_VERSION.zip\", \"C:\\\")')",
        "powershell -NonInteractive -Command 'setx PATH \"$env:path;C:\\go\\bin\"'",
        "eval $(powershell -NonInteractive -Command 'write(\"export PATH=`\"\" + ([Environment]::GetEnvironmentVariable(\"PATH\",\"Machine\") + \";\" + [Environment]::GetEnvironmentVariable(\"PATH\",\"User\")).replace(\"\\\",\"/\").replace(\"C:\",\"/c\").replace(\";\",\":\") + \":`$PATH`\"\")')",
        "source cirrus-env CI",
        "./gradlew ${GRADLE_COMMON_FLAGS} test"
    ]


def qa_os_win_task():
    return {
        "qa_os_win_task": {
            "only_if": is_branch_qa_eligible(),
            "skip": is_rule_metadata_update_pr(),
            "depends_on": "build",
            "ec2_instance": ec2_instance_builder(),
            "env": artifactory_reader_env(),
            "gradle_cache": gradle_cache(),
            "gradle_wrapper_cache": gradle_wrapper_cache(),
            "build_script": qa_win_script(),
            "on_success": profile_report_artifacts(),
            "on_failure": {
                "go_test_report_artifacts": {
                    "path": "sonar-helm-for-iac/build/test-report.json",
                },
                "java_test_report_artifacts": {
                    "path": "**/build/reports/tests/**/*.html"
                }
            },
        }
    }


#
# Plugin
#

def qa_task(env):
    return {
        "only_if": is_branch_qa_eligible(),
        "skip": is_rule_metadata_update_pr(),
        "depends_on": "build",
        "eks_container": base_image_container_builder(memory="10G"),
        "env": env,
        "gradle_cache": gradle_cache(),
        "gradle_wrapper_cache": gradle_wrapper_cache(),
        "set_orchestrator_home_script": set_orchestrator_home_script(),
        "mkdir_orchestrator_home_script": mkdir_orchestrator_home_script(),
        "orchestrator_cache": orchestrator_cache(),
        "run_its_script": run_its_script(),
        "on_failure": {
            "junit_artifacts": {
                "path": "**/test-results/**/*.xml",
                "format": "junit"
            }
        },
        "cleanup_gradle_script": cleanup_gradle_script(),
    }


def run_its_script():
    return [
        "git submodule update --init --depth 1",
        "source cirrus-env QA",
        "source .cirrus/use-gradle-wrapper.sh",
        "./gradlew \"${GRADLE_TASK}\" \"-Dsonar.runtimeVersion=${SQ_VERSION}\" --info --build-cache --console plain --no-daemon"
    ]


def qa_plugin_env():
    return {
        "GRADLE_TASK": QA_PLUGIN_GRADLE_TASK,
        "KEEP_ORCHESTRATOR_RUNNING": "true",
        "matrix": [
            {"SQ_VERSION": QA_QUBE_LATEST_RELEASE},
            {"SQ_VERSION": "DEV"},
        ]
    }


def qa_plugin_task():
    return {
        "qa_plugin_task": qa_task(qa_plugin_env())
    }


#
# Ruling
#

def qa_ruling_env():
    return {
        "GRADLE_TASK": QA_RULING_GRADLE_TASK,
        "SQ_VERSION": QA_QUBE_LATEST_RELEASE,
        "KEEP_ORCHESTRATOR_RUNNING": "true"
    }


def qa_ruling_task():
    return {
        "qa_ruling_task": qa_task(qa_ruling_env())
    }
