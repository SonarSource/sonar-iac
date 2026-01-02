/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.testing;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

public final class ThreadUtils {
  public static List<String> activeCreatedThreadsName() {
    var result = new ArrayList<String>();
    ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    var threads = threadMXBean.dumpAllThreads(true, true);
    for (ThreadInfo threadInfo : threads) {
      if (!threadInfo.isDaemon()) {
        result.add(threadInfo.getThreadName());
      }
    }
    return result;
  }
}
