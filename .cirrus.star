# Check Starlark specs: https://github.com/bazelbuild/starlark/blob/master/spec.md
# DevInfra modules
load("github.com/SonarSource/cirrus-modules@v2", "load_features")
# Modules
load(
    "github.com/SonarSource/cirrus-modules/cloud-native/helper.star@analysis/master",
    "merge_dict"
)
load(".cirrus/modules/env.star", "env")
load(
    ".cirrus/modules/build.star",
    "build_task",
    "build_test_analyze_task",
    "sca_scan_task"
)
load(
    ".cirrus/modules/qa.star",
    "qa_os_win_task",
    "qa_plugin_task",
    "qa_ruling_task"
)
load(".cirrus/modules/promote.star", "promote_task")


def main(ctx):
    conf = dict()
    merge_dict(conf, load_features(ctx))
    merge_dict(conf, env())
    merge_dict(conf, build_task())
    merge_dict(conf, qa_os_win_task())
    return conf
