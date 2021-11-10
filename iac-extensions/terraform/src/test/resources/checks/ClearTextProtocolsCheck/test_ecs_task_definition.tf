resource "aws_ecs_task_definition" "ecs_task" {

  volume {
    efs_volume_configuration { # Noncompliant
      file_system_id = aws_efs_file_system.fs.id
    }
  }

  volume {
    efs_volume_configuration {
      transit_encryption = "DISABLED"  # Noncompliant
    }
  }

  volume {
    efs_volume_configuration {
      transit_encryption = "ENABLED"
    }
  }

  volume {
    # Compliant; no "efs_volume_configuration" section
  }
}

resource "not_an_aws_msk_cluster" "for_coverage" {
}

