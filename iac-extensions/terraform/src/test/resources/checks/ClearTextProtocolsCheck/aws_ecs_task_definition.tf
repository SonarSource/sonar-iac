resource "aws_ecs_task_definition" "ecs_task" {

  volume {
    # Noncompliant@+1 {{Omitting "transit_encryption" enables clear-text traffic. Make sure it is safe here.}}
    efs_volume_configuration {
      file_system_id = aws_efs_file_system.fs.id
    }
  }

  volume {
    efs_volume_configuration {
      transit_encryption = "DISABLED"  # Noncompliant {{Make sure allowing clear-text traffic is safe here.}}
    # ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
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

