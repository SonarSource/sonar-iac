package org.sonar.plugins.iac.terraform.api.tree;

public interface FileTree extends Tree {
  BodyTree body();
}
