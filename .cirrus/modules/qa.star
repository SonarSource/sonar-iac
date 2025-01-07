load("github.com/SonarSource/cirrus-modules/cloud-native/actions.star@analysis/master", "default_gradle_on_failure")
load("github.com/SonarSource/cirrus-modules/cloud-native/platform.star@analysis/master", "base_image_container_builder",
     "ec2_instance_builder")
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
load("cache.star", "gradle_cache_fingerprint_script")
load("conditions.star", "is_rule_metadata_or_docs_update_pr")

QA_PLUGIN_GRADLE_TASK = ":private:its:plugin:integrationTest"
QA_RULING_GRADLE_TASK = ":private:its:ruling:integrationTest"
QA_QUBE_LATEST_RELEASE = "LATEST_RELEASE"
QA_QUBE_DEV = "DEV"

def qa_win_script():
    return [
        "powershell -NonInteractive -Command 'New-Item -ItemType Directory -Path C:\\golang-dl -Force'",
        "powershell -NonInteractive -Command '(new-object System.Net.WebClient).DownloadFile(\"https://golang.org/dl/go$env:GO_VERSION.windows-386.zip\",\"C:\\golang-dl\\golang-$env:GO_VERSION.zip\")'",
        "eval $(powershell -NonInteractive -Command '(Get-FileHash C:\\golang-dl\\golang-$env:GO_VERSION.zip).Hash -eq \"e544e0e356147ba998e267002bd0f2c4bf3370d495467a55baf2c63595a2026d\"')",
        "eval $(powershell -NonInteractive -Command 'Add-Type -Assembly \"System.IO.Compression.Filesystem\"; [System.IO.Compression.ZipFile]::ExtractToDirectory(\"C:\\golang-dl\\golang-$env:GO_VERSION.zip\", \"C:\\\")')",
        "powershell -NonInteractive -Command 'setx PATH \"$env:path;C:\\go\\bin\"'",
        "eval $(powershell -NonInteractive -Command 'write(\"export PATH=`\"\" + ([Environment]::GetEnvironmentVariable(\"PATH\",\"Machine\") + \";\" + [Environment]::GetEnvironmentVariable(\"PATH\",\"User\")).replace(\"\\\",\"/\").replace(\"C:\",\"/c\").replace(\";\",\":\") + \":`$PATH`\"\")')",
        "source cirrus-env CI",
        "./gradlew ${GRADLE_COMMON_FLAGS} test"
    ]


def qa_os_win_task():
    return {
        "qa_os_win_task": {
            "only_if": "({}) && !({})".format(is_branch_qa_eligible(), is_rule_metadata_or_docs_update_pr()),
            "depends_on": "build",
            "ec2_instance": ec2_instance_builder(),
            "env": artifactory_reader_env(),
            "gradle_cache": gradle_cache(fingerprint_script="git rev-parse HEAD"),
            "gradle_wrapper_cache": gradle_wrapper_cache(),
            "build_script": qa_win_script(),
            "on_success": profile_report_artifacts(),
            "on_failure": default_gradle_on_failure(),
        }
    }


#
# Plugin
#

def qa_task(env):
    return {
        "only_if": "({}) && !({})".format(is_branch_qa_eligible(), is_rule_metadata_or_docs_update_pr()),
        "depends_on": "build",
        "eks_container": base_image_container_builder(cpu=6, memory="16G"),
        "env": env,
        "gradle_cache": gradle_cache(fingerprint_script=gradle_cache_fingerprint_script()),
        "gradle_wrapper_cache": gradle_wrapper_cache(),
        "set_orchestrator_home_script": set_orchestrator_home_script(),
        "mkdir_orchestrator_home_script": mkdir_orchestrator_home_script(),
        "orchestrator_cache": orchestrator_cache(),
        "run_its_script": run_its_script(),
        "on_failure": default_gradle_on_failure(),
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
            {"SQ_VERSION": QA_QUBE_DEV},
        ],
        "GITHUB_TOKEN": "VAULT[development/github/token/licenses-ro token]",
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
        "KEEP_ORCHESTRATOR_RUNNING": "true",
         "GITHUB_TOKEN": "VAULT[development/github/token/licenses-ro token]",
    }


def qa_ruling_task():
    return {
        "qa_ruling_task": qa_task(qa_ruling_env())
    }
