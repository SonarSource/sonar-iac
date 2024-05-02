# Default RE constants that map to CI capabilities provided by Cirrus CI or RE Team
RE_JAVA_17_IMAGE = "${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest"
RE_DEFAULT_REGION = "eu-central-1"
RE_SMALL_INSTANCE_TYPE = "t3.small"
RE_LARGE_INSTANCE_TYPE = "c5.4xlarge"
RE_WINDOWS_JDK7_IMAGE = "base-windows-jdk17-v*"
RE_WINDOWS_PLATFORM = "windows"


# SHARED CANDIDATE
def base_image_container_builder(
    cpu=4,
    memory="8G",
    image=RE_JAVA_17_IMAGE,
    use_in_memory_disk=True
):
    """
    Base configuration for a container that uses a pre-defined image provided by RE Team.

    Provides the default values for the container configuration:
    - image: the image to use for the container by default RE_JAVA_17_IMAGE
    - cluster_name: the name of the EKS cluster to use, by default "${CIRRUS_CLUSTER_NAME}"
    - region: the region of the EKS cluster, by default "eu-central-1"
    - namespace: the namespace to use for the container, by default "default"
    - use_in_memory_disk: whether to use an in-memory disk for the container, by default True
    - cpu: the number of CPUs to use for the container, by default 4
    - memory: the amount of memory to use for the container, by default "8G"
    """
    return {
        "image": image,
        "cluster_name": "${CIRRUS_CLUSTER_NAME}",
        "region": RE_DEFAULT_REGION,
        "namespace": "default",
        "use_in_memory_disk": use_in_memory_disk,
        "cpu": cpu,
        "memory": memory,
    }


# SHARED CANDIDATE
def custom_image_container_builder(
    dockerfile=".cirrus/Dockerfile",
    cpu=4,
    memory="8G",
    image=RE_JAVA_17_IMAGE,
    use_in_memory_disk=True,
    builder_instance_type=RE_SMALL_INSTANCE_TYPE,
):
    """
    Base configuration for a container that uses a custom image provided by the user.

    Provides the default values for the container configuration:
    - dockerfile: the path to the Dockerfile to use for the container, by default ".cirrus/Dockerfile"
    - cpu: the number of CPUs to use for the container, by default 4
    - memory: the amount of memory to use for the container, by default "8G"
    - use_in_memory_disk: whether to use an in-memory disk for the container, by default True
    - builder_instance_type: the type of the builder instance, by default RE_SMALL_INSTANCE_TYPE

    :return: a dictionary with the configuration for the container
    """
    conf = base_image_container_builder(cpu, memory, image, use_in_memory_disk)
    builder = {
        "dockerfile": dockerfile,
        "docker_arguments": {
            "CIRRUS_AWS_ACCOUNT": "${CIRRUS_AWS_ACCOUNT}",
            "GO_VERSION": "${GO_VERSION}",
            "PROTOC_VERSION": "${PROTOC_VERSION}"
        },
        "builder_role": "cirrus-builder",
        "builder_image": "docker-builder-v*",
        "builder_instance_type": builder_instance_type,
        "builder_subnet_id": "${CIRRUS_AWS_SUBNET}",
    }
    conf |= builder
    return conf


# SHARED CANDIDATE
def ec2_instance_builder(
    image=RE_WINDOWS_JDK7_IMAGE,
    platform=RE_WINDOWS_PLATFORM,
    instance_type=RE_LARGE_INSTANCE_TYPE,
    preemptible=False,
    use_ssd=True
):
    """
    Base configuration for a VM that uses a pre-defined image provided by RE Team.

    Provides the default values for the VM configuration:
    - image: the image to use for the VM by default RE_JAVA_17_IMAGE
    - platform: the platform of the VM, by default "windows"
    - instance_type: the type of the VM, by default RE_LARGE_INSTANCE_TYPE
    - preemptible: whether the VM is preemptible, by default False
    - use_ssd: whether the VM uses SSD, by default True

    :return: a dictionary with the configuration for the VM
    """
    return {
        "experimental": "true",
        "image": image,
        "platform": platform,
        "region": RE_DEFAULT_REGION,
        "type": instance_type,
        "subnet_id": "${CIRRUS_AWS_SUBNET}",
        "preemptible": preemptible,
        "use_ssd": use_ssd,
    }
