load(
    "github.com/SonarSource/cirrus-modules/cloud-native/env.star@analysis/master",
    "promotion_env",
)
load("platform.star", "base_image_container_builder")
load("cache.star", "project_version_cache")
load("conditions.star", "is_sonarsource_qa")


def promote_env():
    env = promotion_env()
    env |= {
        "ARTIFACTS": "com.sonarsource.iac:sonar-iac-plugin:jar"
    }
    return env


# SHARED CANDIDATE???
def promote_script():
    return [
        "source cirrus-env PROMOTE",
        "cirrus_jfrog_promote multi",
        "source ${PROJECT_VERSION_CACHE_DIR}/evaluated_project_version.txt",
        "github-notify-promotion",
        "burgr-notify-promotion"
    ]


# SHARED CANDIDATE???
# There are some specific configuration that might not be needed for all the projects
def promote_task():
    return {
        "promote_task": {
            "only_if": is_sonarsource_qa(),
            "depends_on": [
                "build",
                "build_test_analyze",
                "qa_os_win",
                "qa_ruling",
                "qa_plugin"
            ],
            "env": promote_env(),
            "eks_container": base_image_container_builder(cpu=1, memory="2G"),
            "project_version_cache": project_version_cache(),
            "script": promote_script()
        }
    }
