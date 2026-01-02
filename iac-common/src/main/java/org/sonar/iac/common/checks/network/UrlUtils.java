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
  private static final String CLOUD_METADATA_IPV4 = "^169\\.254\\.169\\.254";
  private static final String LOOPBACK_IPV6 = "^(?:0*:){7}:?0*1|^::1";
  private static final String CLOUD_METADATA_IPV6 = "^\\[?fd00:ec2::254\\]?";
  public static final Pattern COMPLIANT_HTTP_DOMAINS = Pattern.compile("^localhost|" + LOOPBACK_IPV4 + "|" + CLOUD_METADATA_IPV4 + "|" + LOOPBACK_IPV6 + "|" + CLOUD_METADATA_IPV6,
    Pattern.CASE_INSENSITIVE);

  private UrlUtils() {
  }

  public static boolean isUnencryptedUrl(String url) {
    var matcher = UNENCRYPTED_PROTOCOLS.matcher(url);
    return matcher.find() && !COMPLIANT_HTTP_DOMAINS.matcher(matcher.group("rest")).find();
  }

  public static List<TextRange> findUnencryptedUrlsOffsets(TextPointer start, String value) {
    var result = new ArrayList<TextRange>();
    var matcher = UNENCRYPTED_PROTOCOLS.matcher(value);
    while (matcher.find() && !COMPLIANT_HTTP_DOMAINS.matcher(matcher.group("rest")).find()) {
      var skipBeforeHighlight = new TokenLocation(start.line(), start.lineOffset(), value.substring(0, matcher.start()));
      var highlight = range(skipBeforeHighlight.endLine(), skipBeforeHighlight.endLineOffset(), matcher.group());
      result.add(highlight);
    }
    return result;
  }
}
