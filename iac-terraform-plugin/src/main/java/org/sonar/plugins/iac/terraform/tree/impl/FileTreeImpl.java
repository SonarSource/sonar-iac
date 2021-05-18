package org.sonar.plugins.iac.terraform.tree.impl;

import org.sonar.plugins.iac.terraform.api.tree.BodyTree;
import org.sonar.plugins.iac.terraform.api.tree.FileTree;

public class FileTreeImpl extends TerraformTree implements FileTree {
  private final BodyTree body;

  public FileTreeImpl(BodyTree body) {
    this.body = body;
  }

  @Override
  public BodyTree body() {
    return body;
  }
}
