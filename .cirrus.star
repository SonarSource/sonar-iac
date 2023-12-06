# Cirrus
load("cirrus", "yaml", "fs")
# RE
load("github.com/SonarSource/cirrus-modules@v2", "load_features")


def merge_conf_into(target, spec):
    for key in spec.keys():
        if target.get(key) == None:
            target.update({key: spec[key]})
        else:
            target[key].update(spec[key])


def env_conf():
    return {
        "env": {'CIRRUS_VAULT_URL': 'https://vault.sonar.build:8200', 'CIRRUS_VAULT_AUTH_PATH': 'jwt-cirrusci', 'CIRRUS_VAULT_ROLE': 'cirrusci-${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}', 'ARTIFACTORY_URL': 'VAULT[development/kv/data/repox data.url]', 'ARTIFACTORY_PRIVATE_USERNAME': 'vault-${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-private-reader', 'ARTIFACTORY_PRIVATE_PASSWORD': 'VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-private-reader access_token]', 'ARTIFACTORY_DEPLOY_USERNAME': 'vault-${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-qa-deployer', 'ARTIFACTORY_DEPLOY_PASSWORD': 'VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-qa-deployer access_token]', 'ARTIFACTORY_DEPLOY_REPO': 'sonarsource-public-qa', 'ARTIFACTORY_ACCESS_TOKEN': 'VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-private-reader access_token]', 'CIRRUS_SHELL': 'bash', 'CIRRUS_CLONE_DEPTH': 1}
    }


def maven_cache():
    
    return {'maven_cache': {'folder': '${CIRRUS_WORKING_DIR}/.m2/repository', 'fingerprint_script': 'cat **/pom.xml'}}
    

def build_secrets():
    
    return {'SIGN_KEY': 'VAULT[development/kv/data/sign data.key]', 'PGP_PASSPHRASE': 'VAULT[development/kv/data/sign data.passphrase]', 'SONAR_HOST_URL': 'https://sonarcloud.io', 'SONAR_TOKEN': 'VAULT[development/kv/data/sonarcloud data.token]'}
    

def ws_scan_secrets():
    
    return {'WS_APIKEY': 'VAULT[development/kv/data/mend data.apikey]'}
    

def promote_secrets():
    
    return {'ARTIFACTORY_PROMOTE_ACCESS_TOKEN': 'VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-promoter access_token]', 'GITHUB_TOKEN': 'VAULT[development/github/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-promotion token]', 'BURGR_URL': 'VAULT[development/kv/data/burgr data.url]', 'BURGR_USERNAME': 'VAULT[development/kv/data/burgr data.cirrus_username]', 'BURGR_PASSWORD': 'VAULT[development/kv/data/burgr data.cirrus_password]'}
    

def linux_image_template():
    
    return {'image': '${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest', 'cluster_name': '${CIRRUS_CLUSTER_NAME}', 'region': 'eu-central-1', 'namespace': 'default', 'use_in_memory_disk': True}
    

def linux_image_with_gcc_template():
    
    return {'dockerfile': '.cirrus/Dockerfile', 'docker_arguments': {'CIRRUS_AWS_ACCOUNT': '${CIRRUS_AWS_ACCOUNT}'}, 'cluster_name': '${CIRRUS_CLUSTER_NAME}', 'builder_role': 'cirrus-builder', 'builder_image': 'docker-builder-v*', 'builder_instance_type': 't3.small', 'builder_subnet_id': '${CIRRUS_AWS_SUBNET}', 'region': 'eu-central-1', 'namespace': 'default', 'use_in_memory_disk': True}
    

def linux_1_cpu_1G_template():
    
    return {'eks_container': {'image': '${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest', 'cluster_name': '${CIRRUS_CLUSTER_NAME}', 'region': 'eu-central-1', 'namespace': 'default', 'use_in_memory_disk': True, 'cpu': 1, 'memory': '1G'}}
    

def linux_3_5_cpu_7G_template():
    
    return {'eks_container': {'image': '${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest', 'cluster_name': '${CIRRUS_CLUSTER_NAME}', 'region': 'eu-central-1', 'namespace': 'default', 'use_in_memory_disk': True, 'cpu': 3.5, 'memory': '7G'}}
    

def linux_4_cpu_8G_java_17_template():
    
    return {'eks_container': {'image': '${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest', 'cluster_name': '${CIRRUS_CLUSTER_NAME}', 'region': 'eu-central-1', 'namespace': 'default', 'use_in_memory_disk': True, 'cpu': 4, 'memory': '8G'}}
    

def win_vm_definition():
    
    return {'ec2_instance': {'experimental': True, 'image': 'base-windows-jdk17-v*', 'platform': 'windows', 'region': 'eu-central-1', 'type': 't3.xlarge', 'subnet_id': '${CIRRUS_AWS_SUBNET}', 'use_ssd': True}}
    

def only_sonarsource_qa():
    
    return {'only_if': '$CIRRUS_USER_COLLABORATOR == \'true\' && $CIRRUS_TAG == "" && ($CIRRUS_PR != "" || $CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ "branch-.*" || $CIRRUS_BRANCH =~ "dogfood-on-.*")'}
    

def only_main_branches():
    
    return {'only_if': '$CIRRUS_USER_COLLABORATOR == \'true\' && $CIRRUS_TAG == "" && ($CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ "branch-.*")'}
    

def qa_task_filter_template():
    
    return {'only_if': '$CIRRUS_USER_COLLABORATOR == \'true\' && $CIRRUS_TAG == "" && ($CIRRUS_PR != "" || $CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ "branch-.*" || $CIRRUS_BRANCH =~ "dogfood-on-.*")', 'depends_on': ['build']}
    



def build_task_conf():
    
    return { "build_task": {'maven_cache': {'folder': '${CIRRUS_WORKING_DIR}/.m2/repository', 'fingerprint_script': 'cat **/pom.xml'}, 'eks_container': {'dockerfile': '.cirrus/Dockerfile', 'docker_arguments': {'CIRRUS_AWS_ACCOUNT': '${CIRRUS_AWS_ACCOUNT}'}, 'cluster_name': '${CIRRUS_CLUSTER_NAME}', 'builder_role': 'cirrus-builder', 'builder_image': 'docker-builder-v*', 'builder_instance_type': 't3.small', 'builder_subnet_id': '${CIRRUS_AWS_SUBNET}', 'region': 'eu-central-1', 'namespace': 'default', 'use_in_memory_disk': True, 'cpu': 3.5, 'memory': '7G'}, 'env': {'SIGN_KEY': 'VAULT[development/kv/data/sign data.key]', 'PGP_PASSPHRASE': 'VAULT[development/kv/data/sign data.passphrase]', 'SONAR_HOST_URL': 'https://sonarcloud.io', 'SONAR_TOKEN': 'VAULT[development/kv/data/sonarcloud data.token]', 'DEPLOY_PULL_REQUEST': True}, 'build_script': ['source cirrus-env BUILD', 'regular_mvn_build_deploy_analyze'], 'cleanup_before_cache_script': 'cleanup_maven_repository'} }
    

def qa_os_win_task_conf():
    
    return { "qa_os_win_task": {'only_if': '$CIRRUS_USER_COLLABORATOR == \'true\' && $CIRRUS_TAG == "" && ($CIRRUS_PR != "" || $CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ "branch-.*" || $CIRRUS_BRANCH =~ "dogfood-on-.*")', 'depends_on': ['build'], 'ec2_instance': {'experimental': True, 'image': 'base-windows-jdk17-v*', 'platform': 'windows', 'region': 'eu-central-1', 'type': 't3.xlarge', 'subnet_id': '${CIRRUS_AWS_SUBNET}', 'use_ssd': True}, 'maven_cache': {'folder': '~/.m2/repository'}, '_lib': ['eval $(powershell -NonInteractive -Command \'write("export PATH=`"" + ([Environment]::GetEnvironmentVariable("PATH","Machine") + ";" + [Environment]::GetEnvironmentVariable("PATH","User")).replace("\\","/").replace("C:","/c").replace(";",":") + ":`$PATH`"")\')'], 'build_script': ['choco install golang --version 1.21.1', 'eval $(powershell -NonInteractive -Command \'write("export PATH=`"" + ([Environment]::GetEnvironmentVariable("PATH","Machine") + ";" + [Environment]::GetEnvironmentVariable("PATH","User")).replace("\\","/").replace("C:","/c").replace(";",":") + ":`$PATH`"")\')', 'source cirrus-env CI', 'mvn.cmd clean verify']} }
    

def sca_scan_task_conf():
    
    return { "sca_scan_task": {'eks_container': {'image': '${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest', 'cluster_name': '${CIRRUS_CLUSTER_NAME}', 'region': 'eu-central-1', 'namespace': 'default', 'use_in_memory_disk': True, 'cpu': 3.5, 'memory': '7G'}, 'maven_cache': {'folder': '${CIRRUS_WORKING_DIR}/.m2/repository', 'fingerprint_script': 'cat **/pom.xml'}, 'only_if': '$CIRRUS_USER_COLLABORATOR == \'true\' && $CIRRUS_TAG == "" && ($CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ "branch-.*")', 'depends_on': ['build'], 'env': {'WS_APIKEY': 'VAULT[development/kv/data/mend data.apikey]'}, 'whitesource_script': ['source cirrus-env QA', 'source set_maven_build_version $BUILD_NUMBER', 'mvn clean install -DskipTests', 'source ws_scan.sh'], 'allow_failures': 'true', 'always': {'ws_artifacts': {'path': 'whitesource/**/*'}}} }
    

def qa_plugin_task_conf():
    
    return { "qa_plugin_task": {'only_if': '$CIRRUS_USER_COLLABORATOR == \'true\' && $CIRRUS_TAG == "" && ($CIRRUS_PR != "" || $CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ "branch-.*" || $CIRRUS_BRANCH =~ "dogfood-on-.*")', 'depends_on': ['build'], 'eks_container': {'image': '${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest', 'cluster_name': '${CIRRUS_CLUSTER_NAME}', 'region': 'eu-central-1', 'namespace': 'default', 'use_in_memory_disk': True, 'cpu': 4, 'memory': '8G'}, 'maven_cache': {'folder': '${CIRRUS_WORKING_DIR}/.m2/repository', 'fingerprint_script': 'cat **/pom.xml'}, 'env': {'matrix': [{'SQ_VERSION': 'LATEST_RELEASE'}, {'SQ_VERSION': 'DEV'}]}, 'submodules_script': ['git submodule update --init'], 'plugin_script': ['source cirrus-env QA', 'source set_maven_build_version $BUILD_NUMBER', 'cd its/plugin', 'mvn verify -Drevision=${PROJECT_VERSION} -Dsonar.runtimeVersion=${SQ_VERSION} -B -e -V'], 'cleanup_before_cache_script': 'cleanup_maven_repository'} }
    

def qa_ruling_task_conf():
    
    return { "qa_ruling_task": {'only_if': '$CIRRUS_USER_COLLABORATOR == \'true\' && $CIRRUS_TAG == "" && ($CIRRUS_PR != "" || $CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ "branch-.*" || $CIRRUS_BRANCH =~ "dogfood-on-.*")', 'depends_on': ['build'], 'eks_container': {'image': '${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest', 'cluster_name': '${CIRRUS_CLUSTER_NAME}', 'region': 'eu-central-1', 'namespace': 'default', 'use_in_memory_disk': True, 'cpu': 4, 'memory': '8G'}, 'maven_cache': {'folder': '${CIRRUS_WORKING_DIR}/.m2/repository', 'fingerprint_script': 'cat **/pom.xml'}, 'submodules_script': ['git submodule update --init'], 'ruling_script': ['source cirrus-env QA', 'source set_maven_build_version $BUILD_NUMBER', 'cd its/ruling', 'mvn verify -Dsonar.runtimeVersion=LATEST_RELEASE -B -e -V'], 'cleanup_before_cache_script': 'cleanup_maven_repository'} }
    

def promote_task_conf():
    
    return { "promote_task": {'only_if': '$CIRRUS_USER_COLLABORATOR == \'true\' && $CIRRUS_TAG == "" && ($CIRRUS_PR != "" || $CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ "branch-.*" || $CIRRUS_BRANCH =~ "dogfood-on-.*")', 'eks_container': {'image': '${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest', 'cluster_name': '${CIRRUS_CLUSTER_NAME}', 'region': 'eu-central-1', 'namespace': 'default', 'use_in_memory_disk': True, 'cpu': 1, 'memory': '1G'}, 'maven_cache': {'folder': '${CIRRUS_WORKING_DIR}/.m2/repository', 'fingerprint_script': 'cat **/pom.xml'}, 'depends_on': ['build', 'qa_ruling', 'qa_plugin'], 'env': {'ARTIFACTORY_PROMOTE_ACCESS_TOKEN': 'VAULT[development/artifactory/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-promoter access_token]', 'GITHUB_TOKEN': 'VAULT[development/github/token/${CIRRUS_REPO_OWNER}-${CIRRUS_REPO_NAME}-promotion token]', 'BURGR_URL': 'VAULT[development/kv/data/burgr data.url]', 'BURGR_USERNAME': 'VAULT[development/kv/data/burgr data.cirrus_username]', 'BURGR_PASSWORD': 'VAULT[development/kv/data/burgr data.cirrus_password]', 'ARTIFACTS': 'com.sonarsource.iac:sonar-iac-plugin:jar'}, 'script': 'cirrus_promote_maven', 'cleanup_before_cache_script': 'cleanup_maven_repository'} }
    




def main(ctx):
    conf = dict()
    re_builtins_conf = load_features(ctx)
    merge_conf_into(conf, re_builtins_conf)
    merge_conf_into(conf, env_conf())
    merge_conf_into(conf, build_task_conf())
    merge_conf_into(conf, qa_os_win_task_conf())
    merge_conf_into(conf, sca_scan_task_conf())
    merge_conf_into(conf, qa_plugin_task_conf())
    merge_conf_into(conf, qa_ruling_task_conf())
    merge_conf_into(conf, promote_task_conf())
    
    return conf