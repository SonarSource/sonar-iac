load("github.com/SonarSource/cirrus-modules@v3", "load_features")
load(
    "github.com/SonarSource/cirrus-modules/cloud-native/helper.star@analysis/master",
    "merge_dict"
)
load(
    "github.com/SonarSource/cirrus-modules/cloud-native/env.star@analysis/master",
    "cirrus_env",
    "gradle_base_env",
    "gradle_develocity_env",
)
load(
    "github.com/SonarSource/cirrus-modules/cloud-native/cache.star@analysis/master",
    "gradle_cache",
    "gradle_wrapper_cache",
    "cleanup_gradle_script",
    "cleanup_gradle_wrapper_cache_script",
)
load(
    "github.com/SonarSource/cirrus-modules/cloud-native/platform.star@analysis/master",
    "base_image_container_builder"
)


def main(ctx):
    conf = {}
    merge_dict(conf, load_features(ctx))
    merge_dict(conf, build_task())
    return conf


def build_task():
    return {
        "build_task": {
            "env": gradle_base_env() | gradle_develocity_env() | {
                "CIRRUS_CLONE_DEPTH": 10,
            },
            "eks_container": base_image_container_builder(cpu=1, memory="4G"),
            "gradle_cache": gradle_cache(),
            "gradle_wrapper_cache": gradle_wrapper_cache(),
            "build_script": [
                "./gradlew build"
            ],
            "cleanup_script": cleanup_gradle_script(),
            "cleanup_wrapper_script": cleanup_gradle_wrapper_cache_script(),
        }
    }
