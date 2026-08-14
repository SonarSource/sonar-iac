/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.checks.network;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonarsource.analyzer.commons.TokenLocation;
import org.sonarsource.analyzer.commons.appsec.CleartextProtocolFilter;

import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

/**
 * Utilities for detecting unencrypted URLs.
 */
public final class UrlUtils {
  public static final Pattern UNENCRYPTED_PROTOCOLS = Pattern.compile("(http|ftp)://[^\\s\"']++", Pattern.CASE_INSENSITIVE);

  private UrlUtils() {
  }

  public static boolean isSensitiveUnencryptedUrl(String url) {
    var matcher = UNENCRYPTED_PROTOCOLS.matcher(url);
    return matcher.find() && !CleartextProtocolFilter.isSafeWithoutTls(matcher.group());
  }

  public static boolean isUnencryptedUrl(String url) {
    var matcher = UNENCRYPTED_PROTOCOLS.matcher(url);
    return matcher.find();
  }

  public static List<TextRange> findUnencryptedUrlsOffsets(TextPointer start, String value) {
    var result = new ArrayList<TextRange>();
    var matcher = UNENCRYPTED_PROTOCOLS.matcher(value);
    while (matcher.find()) {
      if (CleartextProtocolFilter.isSafeWithoutTls(matcher.group())) {
        continue;
      }
      var skipBeforeHighlight = new TokenLocation(start.line(), start.lineOffset(), value.substring(0, matcher.start()));
      var highlight = range(skipBeforeHighlight.endLine(), skipBeforeHighlight.endLineOffset(), matcher.group());
      result.add(highlight);
    }
    return result;
  }
}
