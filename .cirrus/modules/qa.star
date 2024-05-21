load("platform.star", "base_image_container_builder", "ec2_instance_builder")
load("conditions.star", "is_sonarsource_qa")
load("build.star", "profile_report_artifacts")
load(
    "cache.star",
    "gradle_cache",
    "cleanup_gradle_script",
    "orchestrator_cache",
    "set_orchestrator_home_script",
    "mkdir_orchestrator_home_script",
)

QA_PLUGIN_GRADLE_TASK = ":its:plugin:integrationTest"
QA_RULING_GRADLE_TASK = ":its:ruling:integrationTest"
QA_QUBE_LATEST_RELEASE = "LATEST_RELEASE"


def qa_win_script():
    return [
        "choco install golang --version ${GO_VERSION}",
        "choco install protoc --version ${PROTOC_VERSION}.0",
        "eval $(powershell -NonInteractive -Command 'write(\"export PATH=`\"\" + ([Environment]::GetEnvironmentVariable(\"PATH\",\"Machine\") + \";\" + [Environment]::GetEnvironmentVariable(\"PATH\",\"User\")).replace(\"\\\",\"/\").replace(\"C:\",\"/c\").replace(\";\",\":\") + \":`$PATH`\"\")')",
        "source cirrus-env CI",
        "./gradlew ${GRADLE_COMMON_FLAGS} test"
    ]


def qa_os_win_task():
    return {
        "qa_os_win_task": {
            "only_if": is_sonarsource_qa(),
            "depends_on": "build",
            "ec2_instance": ec2_instance_builder(),
            "gradle_cache": gradle_cache(),
            "build_script": qa_win_script(),
            "on_success": profile_report_artifacts(),
        }
    }


#
# Plugin
#

def qa_task(env):
    return {
        "only_if": is_sonarsource_qa(),
        "depends_on": "build",
        "eks_container": base_image_container_builder(memory="9G"),
        "env": env,
        "gradle_cache": gradle_cache(),
        "set_orchestrator_home_script": set_orchestrator_home_script(),
        "mkdir_orchestrator_home_script": mkdir_orchestrator_home_script(),
        "orchestrator_cache": orchestrator_cache(),
        "run_its_script": run_its_script(),
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
        "SQ_VERSION": QA_QUBE_LATEST_RELEASE
    }


def qa_ruling_task():
    return {
        "qa_ruling_task": qa_task(qa_ruling_env())
    }
