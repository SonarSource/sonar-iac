load("github.com/SonarSource/cirrus-modules@v2", "load_features")
load("cirrus", "env", "fs", "yaml")
load(
    "github.com/SonarSource/cirrus-modules/cloud-native/helper.star@analysis/master",
    "merge_dict"
)
load(".enterprise-cirrus.star", "private_pipeline_builder")


def private_conf(ctx):
    features = load_features(ctx, only_if=dict())
    doc = private_pipeline_builder()
    conf = dict()
    merge_dict(conf, features)
    merge_dict(conf, doc)
    return conf

# workaround for BUILD-4413 (build number on public CI)
def build_4413_workaround():
    return {
        'env': {
            'CI_BUILD_NUMBER': env.get("CIRRUS_PR", "1")
        },
    }

def public_conf(ctx):
    conf = fs.read(".cirrus-public.yml")
    if env.get("CIRRUS_USER_PERMISSION") in ["write", "admin"]:
        features = load_features(ctx, features=["build_number"])
    else:
        features = build_4413_workaround()
    features = yaml.dumps(features)
    return features + conf

def is_enterprise():
    return env.get("CIRRUS_REPO_FULL_NAME") == 'SonarSource/sonar-iac-enterprise'

def main(ctx):
    if is_enterprise():
        return private_conf(ctx)
    else:
        return public_conf(ctx)
