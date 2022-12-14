package org.sonar.iac.docker.tree;

import java.util.function.Predicate;
import javax.annotation.CheckForNull;
import org.sonar.iac.docker.tree.api.DockerTree;

public class TreeUtils {

  @CheckForNull
  public static DockerTree firstAncestor(DockerTree tree, Predicate<DockerTree> predicate) {
    DockerTree currentParent = tree.parent();
    while (currentParent != null) {
      if (predicate.test(currentParent)) {
        return currentParent;
      }
      currentParent = currentParent.parent();
    }
    return null;
  }

  @CheckForNull
  public static DockerTree firstAncestorOfKind(DockerTree tree, DockerTree.Kind... kinds) {
    return firstAncestor(tree, t -> t.is(kinds));
  }
}
