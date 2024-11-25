def gradle_cache_fingerprint_script():
    common_value = ("find . -name '*.gradle.kts' -type f -exec md5sum {} \\; | sort && md5sum gradle/libs.versions.toml && " +
                    "md5sum gradle/wrapper/gradle-wrapper.properties && md5sum gradle.properties")
    return common_value + " && md5sum sonar-helm-for-iac/go.sum"
