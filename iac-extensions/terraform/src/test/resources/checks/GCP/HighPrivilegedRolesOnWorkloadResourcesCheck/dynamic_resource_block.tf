# A `dynamic "resource"` block is parsed with `resource` as the block key and no labels,
# so the resource type is null and must not be looked up in the relevant-types set.
resource "aws_eks_cluster" "this" {
  dynamic "kube_scheduler_config" {
    for_each = var.kube_scheduler_config != null ? [var.kube_scheduler_config] : []

    content {
      dynamic "resource" {
        for_each = var.scoring_resources

        content {
          name   = resource.value.name
          weight = resource.value.weight
        }
      }
    }
  }
}

dynamic "resource" {
  for_each = var.scoring_resources

  content {
    name = resource.value.name
  }
}
