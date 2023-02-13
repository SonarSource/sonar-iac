package org.sonar.iac.docker;

import javax.annotation.Nullable;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.utils.ArgumentUtils;

public class TestUtils {

  private TestUtils() {
    // utils class
  }

  @Nullable
  public static String argValue(Argument argument) {
    return ArgumentUtils.resolve(argument).value();
  }
}
