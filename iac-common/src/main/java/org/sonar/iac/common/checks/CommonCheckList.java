package org.sonar.iac.common.checks;

import java.util.Arrays;
import java.util.List;

public class CommonCheckList {
  private CommonCheckList() {
  }

  public static List<Class<?>> checks() {
    return Arrays.asList(ToDoCommentCheck.class);
  }
}
