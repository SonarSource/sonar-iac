ARG digest=@sha256:762d8d035c3b1c98d30c5385f394f4d762302ba9ee8e0da8c93344c688d160b2
ARG invalid_image=openjdk@sha256:762d8d035c3b1c98d30c5385f394f4d762302ba9ee8e0da8c93344c688d160b2
ARG valid_image=openjdk
ARG random_string=value

# Noncompliant@+1 {{Setting a digest will prevent receiving updates of the base image. Make sure it is safe here.}}
  FROM openjdk@sha256:762d8d035c3b1c98d30c5385f394f4d762302ba9ee8e0da8c93344c688d160b2
#      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
FROM openjdk:21-oracle@sha256:762d8d035c3b1c98d30c5385f394f4d762302ba9ee8e0da8c93344c688d160b2
# Noncompliant@+1
FROM openjdk@sha256:762d8d035c3b1c98d30c5385f394f4d762302ba9ee8e0da8c93344c688d160b2
# Noncompliant@+1
FROM $invalid_image
# Noncompliant@+1
FROM openjdk$digest
# Noncompliant@+1
FROM openjdk@$random_string
# Noncompliant@+1
FROM openjdk:21-oracle\
@sha256:762d8d035c3b1c98d30c5385f394f4d762302ba9ee8e0da8c93344c688d160b2
# Noncompliant@+1
FROM openjdk@string
# Noncompliant@+1
FROM openjdk@


FROM openjdk
FROM openjdk:$random_string
FROM openjdk$random_string
FROM $valid_image
FROM $unknown_image
