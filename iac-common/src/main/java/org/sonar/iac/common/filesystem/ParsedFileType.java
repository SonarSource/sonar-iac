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
package org.sonar.iac.common.filesystem;

import java.util.function.Function;

/**
 * The identity of one kind of parsed referenced file: it fixes the result type, carries the parser, and is the
 * discriminator {@link ReferencedFileCache} parses by.
 * <p>
 * Instance identity is the key — this class deliberately has no {@code equals}, because two parsers can produce the
 * same type from the same file and must not share a result. Declare one {@code static final} constant per kind: an
 * instance built per call is a new key every time, so it never reuses a parsed result and its entry stays in the
 * cache for the rest of the analysis.
 */
public final class ParsedFileType<R> {

  private final Function<String, R> parser;

  private ParsedFileType(Function<String, R> parser) {
    this.parser = parser;
  }

  public static <R> ParsedFileType<R> of(Function<String, R> parser) {
    return new ParsedFileType<>(parser);
  }

  R parse(String content) {
    return parser.apply(content);
  }
}
