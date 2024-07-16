load("github.com/SonarSource/cirrus-modules/cloud-native/env.star@analysis/master","promotion_env")
load("github.com/SonarSource/cirrus-modules/cloud-native/platform.star@analysis/master", "base_image_container_builder")
load("github.com/SonarSource/cirrus-modules/cloud-native/cache.star@petertrr/fix-cache-config", "project_version_cache")
load("github.com/SonarSource/cirrus-modules/cloud-native/conditions.star@analysis/master", "is_branch_qa_eligible")


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
    ]


# SHARED CANDIDATE???
# There are some specific configuration that might not be needed for all the projects
def promote_task():
    return {
        "promote_task": {
            "only_if": is_branch_qa_eligible(),
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
