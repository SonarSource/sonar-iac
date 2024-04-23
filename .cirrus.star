# Cirrus
load("cirrus", "yaml", "fs")
# RE
load("github.com/SonarSource/cirrus-modules@v2", "load_features")
# Sonar IaC
load(".cirrus/modules/env.star", "env")
load(".cirrus/modules/cache.star", "setup_gradle_cache", "cleanup_gradle_cache")


def merge_dict(target, source):
    for key in source.keys():
        if target.get(key) == None:
            target.update({key: source[key]})
        else:
            target[key].update(source[key])


def build_secrets_vars():
    return {
        "SIGN_KEY": "VAULT[development/kv/data/sign data.key]",
        "PGP_PASSPHRASE": "VAULT[development/kv/data/sign data.passphrase]",
        "SONAR_HOST_URL": "https://sonarcloud.io",
        "SONAR_TOKEN": "VAULT[development/kv/data/sonarcloud data.token]"
    }


def ws_scan_secrets():
    return {
        "WS_APIKEY": "VAULT[development/kv/data/mend data.apikey]"
    }


def promote_secrets():
    return {
        "ARTIFACTORY_PROMOTE_ACCESS_TOKEN": "VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-promoter access_token]",
        "GITHUB_TOKEN": "VAULT[development/github/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-promotion token]",
        "BURGR_URL": "VAULT[development/kv/data/burgr data.url]",
        "BURGR_USERNAME": "VAULT[development/kv/data/burgr data.cirrus_username]",
        "BURGR_PASSWORD": "VAULT[development/kv/data/burgr data.cirrus_password]"
    }


def linux_image_template():
    return {
        "image": "${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest",
        "cluster_name": "${CIRRUS_CLUSTER_NAME}",
        "region": "eu-central-1",
        "namespace": "default",
        "use_in_memory_disk": True
    }


def linux_image_with_gcc_template():
    return {
        "dockerfile": ".cirrus/Dockerfile",
        "docker_arguments": {
            "CIRRUS_AWS_ACCOUNT": "${CIRRUS_AWS_ACCOUNT}",
            "GO_VERSION": "1.21.7",  #
            "PROTOC_VERSION": "25.0"
        },
        "cluster_name": "${CIRRUS_CLUSTER_NAME}",
        "builder_role": "cirrus-builder",
        "builder_image": "docker-builder-v*",
        "builder_instance_type": "t3.small",
        "builder_subnet_id": "${CIRRUS_AWS_SUBNET}",
        "region": "eu-central-1",
        "namespace": "default",
        "use_in_memory_disk": True
    }


def linux_1_cpu_2G_template():
    eks_container = {"cpu": 1, "memory": "2G"}
    eks_container.update(linux_image_template())
    return {"eks_container": eks_container}


def linux_4_cpu_8G_java_17_template():
    eks_container = {"cpu": 4, "memory": "8G"}
    eks_container.update(linux_image_template())
    return {"eks_container": eks_container}


def win_vm_definition():
    return {
        "ec2_instance": {
            "experimental": True,
            "image": "base-windows-jdk17-v*",
            "platform": "windows",
            "region": "eu-central-1",
            "type": "c5.4xlarge",
            "subnet_id": "${CIRRUS_AWS_SUBNET}",
            "preemptible": False,
            "use_ssd": True
        }
    }


def only_sonarsource_qa_filter():
    return {
        "only_if": "$CIRRUS_USER_COLLABORATOR == 'true' && $CIRRUS_TAG == \"\" && ($CIRRUS_PR != \"\" || $CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ \"branch-.*\" || $CIRRUS_BRANCH =~ \"dogfood-on-.*\")"
    }


def only_main_branches_filter():
    return {
        "only_if": "$CIRRUS_USER_COLLABORATOR == 'true' && $CIRRUS_TAG == \"\" && ($CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ \"branch-.*\")"
    }


def qa_task_filter():
    filter = {"depends_on": ["build"]}
    merge_dict(filter, only_sonarsource_qa_filter())
    return filter


def setup_project_version_cache():
    return {
        "project_version_cache": {
            "folder": "$PROJECT_VERSION_CACHE_DIR",
            "fingerprint_script": "echo $BUILD_NUMBER",
            "populate_script": "mkdir -p \"$PROJECT_VERSION_CACHE_DIR\"\ntouch \"$PROJECT_VERSION_CACHE_DIR\"/evaluated_project_version.txt\n",
            "reupload_on_changes": "true"
        }
    }


def gradle_its_template():
    return {
        "_inject_setup_gradle_cache": "dummy",
        "_inject_setup_orchestrator_cache": "dummy",
        "env": {
            "GO_CROSS_COMPILE": "1",
            "KEEP_ORCHESTRATOR_RUNNING": "true"
        },
        "run_its_script": [
            "git submodule update --init --depth 1",
            "source cirrus-env QA",
            "source .cirrus/use-gradle-wrapper.sh",
            "./gradlew \"${GRADLE_TASK}\" \"-Dsonar.runtimeVersion=${SQ_VERSION}\" --info --build-cache --console plain --no-daemon"
        ],
        "_inject_cleanup_gradle_cache": "dummy"
    }


def base_container_factory(
    cpu,
    memory,
    use_in_memory_disk=True
):
    return {
        "cluster_name": "${CIRRUS_CLUSTER_NAME}",
        "region": "eu-central-1",
        "namespace": "default",
        "memory": memory,
        "cpu": cpu,
        "use_in_memory_disk": use_in_memory_disk
    }


def builder_docker_factory(instance_type="t3.small"):
    return {
        "dockerfile": ".cirrus/Dockerfile",
        "docker_arguments": {
            "CIRRUS_AWS_ACCOUNT": "${CIRRUS_AWS_ACCOUNT}",
            "GO_VERSION": "${GO_VERSION}",
            "PROTOC_VERSION": "${PROTOC_VERSION}",
        },
        "builder_role": "cirrus-builder",
        "builder_image": "docker-builder-v*",
        "builder_instance_type": instance_type,
        "builder_subnet_id": "${CIRRUS_AWS_SUBNET}"
    }


def builder_container_factory(
    cpu=4,
    memory="8G",
    instance_type="t3.small",
    use_in_memory_disk=True
):
    container = base_container_factory(cpu, memory, use_in_memory_disk)
    container |= builder_docker_factory(instance_type)
    return {"eks_container": container}


def build_script():
    return {
        "build_script": [
            "source cirrus-env BUILD",
            "source .cirrus/use-gradle-wrapper.sh",
            "regular_gradle_build_deploy_analyze ${BUILD_ARGUMENTS}",
            "source set_gradle_build_version ${BUILD_NUMBER}",
            "echo export PROJECT_VERSION=${PROJECT_VERSION} >> ~/.profile"
        ],
    }


def gradle_build_template(cpu=4, memory="6G", use_cache=True):
    template = builder_container_factory(cpu, memory)
    if use_cache:
        template |= setup_gradle_cache()
    template |= build_script()
    if use_cache:
        template |= cleanup_gradle_cache()
    return template


def store_profile_report_template():
    return {
        "on_success": {
            "profile_report_artifacts": {
                "path": "build/reports/profile/profile-*.html"
            }
        }
    }


def store_project_version_script():
    return {
        "store_project_version_script": [
            "source ~/.profile",
            "echo \"export PROJECT_VERSION=${PROJECT_VERSION}\" > ${PROJECT_VERSION_CACHE_DIR}/evaluated_project_version.txt"
        ],
    }


def build_args():
    return {
        "DEPLOY_PULL_REQUEST": True,
        "BUILD_ARGUMENTS": "-DtrafficInspection=false --parallel --profile -x test -x sonar"
    }


def build_task():
    conf = {"env": build_secrets_vars() | build_args()}
    conf |= setup_project_version_cache()
    conf |= gradle_build_template(cpu=10)
    conf |= store_project_version_script()
    conf |= store_profile_report_template()
    return {"build_task": conf}


def build_test_args():
    return {
        "DEPLOY_PULL_REQUEST": False,
        "BUILD_ARGUMENTS": "-x artifactoryPublish"
    }


def build_test_analyze_task():
    conf = {"env": build_test_args()}
    conf |= qa_task_filter()
    conf |= gradle_build_template(cpu=6)
    return {"build_test_analyze_task": conf}


def whitesource_script():
    return {
        "whitesource_script": [
            "source cirrus-env SCA",
            "source .cirrus/use-gradle-wrapper.sh",
            "source ${PROJECT_VERSION_CACHE_DIR}/evaluated_project_version.txt",
            'GRADLE_OPTS="-Xmx64m -Dorg.gradle.jvmargs=\'-Xmx3G\' -Dorg.gradle.daemon=false" ./gradlew ${GRADLE_COMMON_FLAGS} :iac-common:processResources -Pkotlin.compiler.execution.strategy=in-process"',
            'source ws_scan.sh -d "${PWD},${PWD}/sonar-helm-for-iac"'
        ]
    }


def sca_scan_task():
    conf = {"depends_on": ["build"], "allow_failures": "true"}
    conf |= {"env": ws_scan_secrets()}
    conf |= only_main_branches_filter()
    conf |= setup_project_version_cache()
    conf |= gradle_build_template(cpu=4, memory="8G", use_cache=False)
    conf |= whitesource_script()
    conf |= store_project_version_script()
    conf |= store_profile_report_template()
    conf |= {"always": {"ws_artifacts": {"path": "whitesource/whitesource*.html"}}}
    return {"sca_scan_task": conf}


def main(ctx):
    conf = dict()
    merge_dict(conf, load_features(ctx))
    merge_dict(conf, env())
    merge_dict(conf, build_task())
    merge_dict(conf, build_test_analyze_task())
    merge_dict(conf, sca_scan_task())
    return conf
