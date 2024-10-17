/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.common.checks.network;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonarsource.analyzer.commons.TokenLocation;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

/**
 * Utilities for detecting unencrypted URLs.
 */
public final class UrlUtils {
  public static final Pattern UNENCRYPTED_PROTOCOLS = Pattern.compile("(http|ftp)://(?<rest>\\S+)", Pattern.CASE_INSENSITIVE);
  private static final String LOOPBACK_IPV4 = "^127(?:\\.\\d+){2}\\.\\d+";
  private static final String LOOPBACK_IPV6 = "^(?:0*:){7}:?0*1|^::1";
  public static final Pattern LOOPBACK = Pattern.compile("^localhost|" + LOOPBACK_IPV4 + "|" + LOOPBACK_IPV6, Pattern.CASE_INSENSITIVE);

  private UrlUtils() {
  }

  public static boolean isUnencryptedUrl(String url) {
    var matcher = UNENCRYPTED_PROTOCOLS.matcher(url);
    return matcher.find() && !LOOPBACK.matcher(matcher.group("rest")).find();
  }

  public static List<TextRange> findUnencryptedUrlsOffsets(TextPointer start, String value) {
    var result = new ArrayList<TextRange>();
    var matcher = UNENCRYPTED_PROTOCOLS.matcher(value);
    while (matcher.find() && !LOOPBACK.matcher(matcher.group("rest")).find()) {
      var skipBeforeHighlight = new TokenLocation(start.line(), start.lineOffset(), value.substring(0, matcher.start()));
      var highlight = range(skipBeforeHighlight.endLine(), skipBeforeHighlight.endLineOffset(), matcher.group());
      result.add(highlight);
    }
    return result;
  }
}
