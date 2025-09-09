# This file should be in private folder but it's not yet possible load function can not be called in a function
# See: https://sonarsource.atlassian.net/browse/BUILD-4963
load(
    "github.com/SonarSource/cirrus-modules/cloud-native/helper.star@analysis/master",
    "merge_dict"
)
load(".cirrus/modules/env.star", "env")
load(
    ".cirrus/modules/build.star",
    "build_task",
    "sca_scan_task",
    "build_test_analyze_task",
    "shadow_scan_sqc_eu_task",
    "shadow_scan_sqc_us_task",
    "run_iris_next_enterprise_to_sqc_eu_enterprise_task",
    "run_iris_next_enterprise_to_sqc_eu_public_task",
    "run_iris_next_enterprise_to_sqc_us_enterprise_task",
    "run_iris_next_enterprise_to_sqc_us_public_task",
    "run_iris_next_enterprise_to_next_public_task",
)
load(
    ".cirrus/modules/qa.star",
    "qa_plugin_task",
    "qa_ruling_task",
    "qa_os_win_task",
    "qa_arm64_task",
)
load(".cirrus/modules/promote.star", "promote_task")


def private_pipeline_builder():
    conf = dict()
    merge_dict(conf, env())
    merge_dict(conf, build_task())
    merge_dict(conf, build_test_analyze_task())
    merge_dict(conf, sca_scan_task())
    merge_dict(conf, qa_plugin_task())
    merge_dict(conf, qa_ruling_task())
    merge_dict(conf, qa_arm64_task())
    merge_dict(conf, qa_os_win_task())
    merge_dict(conf, promote_task())
    merge_dict(conf, shadow_scan_sqc_eu_task())
    merge_dict(conf, shadow_scan_sqc_us_task())
    merge_dict(conf, run_iris_next_enterprise_to_sqc_eu_enterprise_task())
    merge_dict(conf, run_iris_next_enterprise_to_sqc_eu_public_task())
    merge_dict(conf, run_iris_next_enterprise_to_sqc_us_enterprise_task())
    merge_dict(conf, run_iris_next_enterprise_to_sqc_us_public_task())
    merge_dict(conf, run_iris_next_enterprise_to_next_public_task())
    return conf
