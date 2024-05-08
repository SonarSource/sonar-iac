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
package org.sonar.iac.springconfig.parser.properties;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.sonar.iac.springconfig.parser.properties.PropertiesTestUtils.createPropertiesFileContext;

class PropertiesParserBaseVisitorTest {

  TestVisitor visitor = new TestVisitor();
  TextRangeTestVisitor visitorTextRanges = new TextRangeTestVisitor();

  @Test
  void shouldParseSimpleKeyValue() {
    var code = "foo=bar";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo=bar<EOF><EOF>",
      "visitRow foo=bar<EOF>",
      "visitLine foo=bar<EOF>",
      "visitKey foo",
      "visitKey bar",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseKeyValueSeparatedByColon() {
    var code = "foo:bar";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo:bar<EOF><EOF>",
      "visitRow foo:bar<EOF>",
      "visitLine foo:bar<EOF>",
      "visitKey foo",
      "visitKey bar",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseKeyValueSeparatedBySpace() {
    var code = "foo bar";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo bar<EOF><EOF>",
      "visitRow foo bar<EOF>",
      "visitLine foo bar<EOF>",
      "visitKey foo",
      "visitKey bar",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseKeyValueSeparatedByTab() {
    var code = "foo\tbar";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo\\tbar<EOF><EOF>",
      "visitRow foo\\tbar<EOF>",
      "visitLine foo\\tbar<EOF>",
      "visitKey foo",
      "visitKey bar",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseKeyValueSeparatedByFormFeed() {
    var code = "foo\fbar";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo\\fbar<EOF><EOF>",
      "visitRow foo\\fbar<EOF>",
      "visitLine foo\\fbar<EOF>",
      "visitKey foo",
      "visitKey bar",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseKeyValueWithLeadingSpaces() {
    var code = """
      # comment
         foo=bar""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile # comment\\nfoo=bar<EOF><EOF>",
      "visitRow # comment\\n",
      "visitComment # comment\\n",
      "visitCommentStartAndText # comment",
      "visitCommentText  comment",
      "visitEol \\n",
      "visitRow foo=bar<EOF>",
      "visitLine foo=bar<EOF>",
      "visitKey foo",
      "visitKey bar",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseSimpleExpressionWithComments() {
    var code = """
      # example comment
      foo=bar
      #comment at the end""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile # example comment\\nfoo=bar\\n#comment at the end<EOF><EOF>",
      "visitRow # example comment\\n",
      "visitComment # example comment\\n",
      "visitCommentStartAndText # example comment",
      "visitCommentText  example comment",
      "visitEol \\n",
      "visitRow foo=bar\\n",
      "visitLine foo=bar\\n",
      "visitKey foo",
      "visitKey bar",
      "visitEol \\n",
      "visitRow #comment at the end<EOF>",
      "visitComment #comment at the end<EOF>",
      "visitCommentStartAndText #comment at the end",
      "visitCommentText comment at the end",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseCommentsStartsWithExclamationMark() {
    var code = """
      ! also a comment
      foo=bar
      ! also comment at the end""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile ! also a comment\\nfoo=bar\\n! also comment at the end<EOF><EOF>",
      "visitRow ! also a comment\\n",
      "visitComment ! also a comment\\n",
      "visitCommentStartAndText ! also a comment",
      "visitCommentText  also a comment",
      "visitEol \\n",
      "visitRow foo=bar\\n",
      "visitLine foo=bar\\n",
      "visitKey foo",
      "visitKey bar",
      "visitEol \\n",
      "visitRow ! also comment at the end<EOF>",
      "visitComment ! also comment at the end<EOF>",
      "visitCommentStartAndText ! also comment at the end",
      "visitCommentText  also comment at the end",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseCommentWithLeadingSpaces() {
    var code = """
      foo=bar
         # comment""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo=bar\\n# comment<EOF><EOF>",
      "visitRow foo=bar\\n",
      "visitLine foo=bar\\n",
      "visitKey foo",
      "visitKey bar",
      "visitEol \\n",
      "visitRow # comment<EOF>",
      "visitComment # comment<EOF>",
      "visitCommentStartAndText # comment",
      "visitCommentText  comment",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseURI() {
    var code = "spring.activemq.broker-url=tcp://127.0.0.1:61616";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile spring.activemq.broker-url=tcp://127.0.0.1:61616<EOF><EOF>",
      "visitRow spring.activemq.broker-url=tcp://127.0.0.1:61616<EOF>",
      "visitLine spring.activemq.broker-url=tcp://127.0.0.1:61616<EOF>",
      "visitKey spring.activemq.broker-url",
      "visitKey tcp://127.0.0.1:61616",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseLoggingPattern() {
    var code = "logging.pattern.console=%d{mm:ss.SSS} %-5p [%-31t] [%-54logger{0}] %marker%m%ex{full} - %logger - %F:%L%n";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile logging.pattern.console=%d{mm:ss.SSS} %-5p [%-31t] [%-54logger{0}] %marker%m%ex{full} - %logger - %F:%L%n<EOF><EOF>",
      "visitRow logging.pattern.console=%d{mm:ss.SSS} %-5p [%-31t] [%-54logger{0}] %marker%m%ex{full} - %logger - %F:%L%n<EOF>",
      "visitLine logging.pattern.console=%d{mm:ss.SSS} %-5p [%-31t] [%-54logger{0}] %marker%m%ex{full} - %logger - %F:%L%n<EOF>",
      "visitKey logging.pattern.console",
      "visitKey %d{mm:ss.SSS} %-5p [%-31t] [%-54logger{0}] %marker%m%ex{full} - %logger - %F:%L%n",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseTrailingCommentsAsValue() {
    var code = """
      foo=bar
      # foo1=bar1
      foo2=bar2 # foo3=bar3""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo=bar\\n# foo1=bar1\\nfoo2=bar2 # foo3=bar3<EOF><EOF>",
      "visitRow foo=bar\\n",
      "visitLine foo=bar\\n",
      "visitKey foo",
      "visitKey bar",
      "visitEol \\n",
      "visitRow # foo1=bar1\\n",
      "visitComment # foo1=bar1\\n",
      "visitCommentStartAndText # foo1=bar1",
      "visitCommentText  foo1=bar1",
      "visitEol \\n",
      "visitRow foo2=bar2 # foo3=bar3<EOF>",
      "visitLine foo2=bar2 # foo3=bar3<EOF>",
      "visitKey foo2",
      "visitKey bar2 # foo3=bar3",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseListProperties() {
    var code = """
      mail.defaultRecipients[0]=admin@mail.com
      mail.defaultRecipients[1]=owner@mail.com""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile mail.defaultRecipients[0]=admin@mail.com\\nmail.defaultRecipients[1]=owner@mail.com<EOF><EOF>",
      "visitRow mail.defaultRecipients[0]=admin@mail.com\\n",
      "visitLine mail.defaultRecipients[0]=admin@mail.com\\n",
      "visitKey mail.defaultRecipients[0]",
      "visitKey admin@mail.com",
      "visitEol \\n",
      "visitRow mail.defaultRecipients[1]=owner@mail.com<EOF>",
      "visitLine mail.defaultRecipients[1]=owner@mail.com<EOF>",
      "visitKey mail.defaultRecipients[1]",
      "visitKey owner@mail.com",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseMultipleEmptyLines() {
    var code = """
      foo=bar

      bar=foo""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo=bar\\n\\nbar=foo<EOF><EOF>",
      "visitRow foo=bar\\n\\n",
      "visitLine foo=bar\\n\\n",
      "visitKey foo",
      "visitKey bar",
      "visitEol \\n\\n",
      "visitRow bar=foo<EOF>",
      "visitLine bar=foo<EOF>",
      "visitKey bar",
      "visitKey foo",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseNewLinesCR() {
    var code = "foo1=bar1\rfoo2=bar2\r\r\rfoo3=bar3\r";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo1=bar1\\rfoo2=bar2\\r\\r\\rfoo3=bar3\\r<EOF>",
      "visitRow foo1=bar1\\r",
      "visitLine foo1=bar1\\r",
      "visitKey foo1",
      "visitKey bar1",
      "visitEol \\r",
      "visitRow foo2=bar2\\r\\r\\r",
      "visitLine foo2=bar2\\r\\r\\r",
      "visitKey foo2",
      "visitKey bar2",
      "visitEol \\r\\r\\r",
      "visitRow foo3=bar3\\r",
      "visitLine foo3=bar3\\r",
      "visitKey foo3",
      "visitKey bar3",
      "visitEol \\r");
  }

  @Test
  void shouldParseNewLinesCRLF() {
    var code = "foo1=bar1\r\nfoo2=bar2\r\n\r\n\r\nfoo3=bar3\r\n";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo1=bar1\\r\\nfoo2=bar2\\r\\n\\r\\n\\r\\nfoo3=bar3\\r\\n<EOF>",
      "visitRow foo1=bar1\\r\\n",
      "visitLine foo1=bar1\\r\\n",
      "visitKey foo1",
      "visitKey bar1",
      "visitEol \\r\\n",
      "visitRow foo2=bar2\\r\\n\\r\\n\\r\\n",
      "visitLine foo2=bar2\\r\\n\\r\\n\\r\\n",
      "visitKey foo2",
      "visitKey bar2",
      "visitEol \\r\\n\\r\\n\\r\\n",
      "visitRow foo3=bar3\\r\\n",
      "visitLine foo3=bar3\\r\\n",
      "visitKey foo3",
      "visitKey bar3",
      "visitEol \\r\\n");
  }

  @Test
  void shouldParseNewLinesU2028() {
    var code = "foo1=bar1\u2028foo2=bar2\u2028\u2028\u2028foo3=bar3\u2028";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo1=bar1\\u2028foo2=bar2\\u2028\\u2028\\u2028foo3=bar3\\u2028<EOF>",
      "visitRow foo1=bar1\\u2028",
      "visitLine foo1=bar1\\u2028",
      "visitKey foo1",
      "visitKey bar1",
      "visitEol \\u2028",
      "visitRow foo2=bar2\\u2028\\u2028\\u2028",
      "visitLine foo2=bar2\\u2028\\u2028\\u2028",
      "visitKey foo2",
      "visitKey bar2",
      "visitEol \\u2028\\u2028\\u2028",
      "visitRow foo3=bar3\\u2028",
      "visitLine foo3=bar3\\u2028",
      "visitKey foo3",
      "visitKey bar3",
      "visitEol \\u2028");
  }

  @Test
  void shouldParseNewLinesU2029() {
    var code = "foo1=bar1\u2029foo2=bar2\u2029\u2029\u2029foo3=bar3\u2029";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo1=bar1\\u2029foo2=bar2\\u2029\\u2029\\u2029foo3=bar3\\u2029<EOF>",
      "visitRow foo1=bar1\\u2029",
      "visitLine foo1=bar1\\u2029",
      "visitKey foo1",
      "visitKey bar1",
      "visitEol \\u2029",
      "visitRow foo2=bar2\\u2029\\u2029\\u2029",
      "visitLine foo2=bar2\\u2029\\u2029\\u2029",
      "visitKey foo2",
      "visitKey bar2",
      "visitEol \\u2029\\u2029\\u2029",
      "visitRow foo3=bar3\\u2029",
      "visitLine foo3=bar3\\u2029",
      "visitKey foo3",
      "visitKey bar3",
      "visitEol \\u2029");
  }

  @Test
  void shouldAcceptAllCharactersInComments() {
    var code = """
      #comment 1 ! # : =
      !comment 2 # aa
      foo2=bar2""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile #comment 1 ! # : =\\n!comment 2 # aa\\nfoo2=bar2<EOF><EOF>",
      "visitRow #comment 1 ! # : =\\n",
      "visitComment #comment 1 ! # : =\\n",
      "visitCommentStartAndText #comment 1 ! # : =",
      "visitCommentText comment 1 ! # : =",
      "visitEol \\n",
      "visitRow !comment 2 # aa\\n",
      "visitComment !comment 2 # aa\\n",
      "visitCommentStartAndText !comment 2 # aa",
      "visitCommentText comment 2 # aa",
      "visitEol \\n",
      "visitRow foo2=bar2<EOF>",
      "visitLine foo2=bar2<EOF>",
      "visitKey foo2",
      "visitKey bar2",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseEmptyComment() {
    var code = """
      #
      foo=bar""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile #\\nfoo=bar<EOF><EOF>",
      "visitRow #\\n",
      "visitComment #\\n",
      "visitCommentStartAndText #",
      "visitCommentText ",
      "visitEol \\n",
      "visitRow foo=bar<EOF>",
      "visitLine foo=bar<EOF>",
      "visitKey foo",
      "visitKey bar",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseNewLinesCommentsLF() {
    var code = """
      #comment 1
      !comment 2
      foo=bar
      #
      !
      foo2=bar2
      !comment 5""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile #comment 1\\n!comment 2\\nfoo=bar\\n#\\n!\\nfoo2=bar2\\n!comment 5<EOF><EOF>",
      "visitRow #comment 1\\n",
      "visitComment #comment 1\\n",
      "visitCommentStartAndText #comment 1",
      "visitCommentText comment 1",
      "visitEol \\n",
      "visitRow !comment 2\\n",
      "visitComment !comment 2\\n",
      "visitCommentStartAndText !comment 2",
      "visitCommentText comment 2",
      "visitEol \\n",
      "visitRow foo=bar\\n",
      "visitLine foo=bar\\n",
      "visitKey foo",
      "visitKey bar",
      "visitEol \\n",
      "visitRow #\\n",
      "visitComment #\\n",
      "visitCommentStartAndText #",
      "visitCommentText ",
      "visitEol \\n",
      "visitRow !\\n",
      "visitComment !\\n",
      "visitCommentStartAndText !",
      "visitCommentText ",
      "visitEol \\n",
      "visitRow foo2=bar2\\n",
      "visitLine foo2=bar2\\n",
      "visitKey foo2",
      "visitKey bar2",
      "visitEol \\n",
      "visitRow !comment 5<EOF>",
      "visitComment !comment 5<EOF>",
      "visitCommentStartAndText !comment 5",
      "visitCommentText comment 5",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseNewLinesCommentsCR() {
    var code = "#comment 1\r!comment 2\rfoo=bar\r#\r!\rfoo2=bar2\r!comment 5";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile #comment 1\\r!comment 2\\rfoo=bar\\r#\\r!\\rfoo2=bar2\\r!comment 5<EOF><EOF>",
      "visitRow #comment 1\\r",
      "visitComment #comment 1\\r",
      "visitCommentStartAndText #comment 1",
      "visitCommentText comment 1",
      "visitEol \\r",
      "visitRow !comment 2\\r",
      "visitComment !comment 2\\r",
      "visitCommentStartAndText !comment 2",
      "visitCommentText comment 2",
      "visitEol \\r",
      "visitRow foo=bar\\r",
      "visitLine foo=bar\\r",
      "visitKey foo",
      "visitKey bar",
      "visitEol \\r",
      "visitRow #\\r",
      "visitComment #\\r",
      "visitCommentStartAndText #",
      "visitCommentText ",
      "visitEol \\r",
      "visitRow !\\r",
      "visitComment !\\r",
      "visitCommentStartAndText !",
      "visitCommentText ",
      "visitEol \\r",
      "visitRow foo2=bar2\\r",
      "visitLine foo2=bar2\\r",
      "visitKey foo2",
      "visitKey bar2",
      "visitEol \\r",
      "visitRow !comment 5<EOF>",
      "visitComment !comment 5<EOF>",
      "visitCommentStartAndText !comment 5",
      "visitCommentText comment 5",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseNewLinesCommentsCRLF() {
    var code = "#comment 1\r\n!comment 2\r\nfoo=bar\r\n#\r\n!\r\nfoo2=bar2\r\n!comment 5";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile #comment 1\\r\\n!comment 2\\r\\nfoo=bar\\r\\n#\\r\\n!\\r\\nfoo2=bar2\\r\\n!comment 5<EOF><EOF>",
      "visitRow #comment 1\\r\\n",
      "visitComment #comment 1\\r\\n",
      "visitCommentStartAndText #comment 1",
      "visitCommentText comment 1",
      "visitEol \\r\\n",
      "visitRow !comment 2\\r\\n",
      "visitComment !comment 2\\r\\n",
      "visitCommentStartAndText !comment 2",
      "visitCommentText comment 2",
      "visitEol \\r\\n",
      "visitRow foo=bar\\r\\n",
      "visitLine foo=bar\\r\\n",
      "visitKey foo",
      "visitKey bar",
      "visitEol \\r\\n",
      "visitRow #\\r\\n",
      "visitComment #\\r\\n",
      "visitCommentStartAndText #",
      "visitCommentText ",
      "visitEol \\r\\n",
      "visitRow !\\r\\n",
      "visitComment !\\r\\n",
      "visitCommentStartAndText !",
      "visitCommentText ",
      "visitEol \\r\\n",
      "visitRow foo2=bar2\\r\\n",
      "visitLine foo2=bar2\\r\\n",
      "visitKey foo2",
      "visitKey bar2",
      "visitEol \\r\\n",
      "visitRow !comment 5<EOF>",
      "visitComment !comment 5<EOF>",
      "visitCommentStartAndText !comment 5",
      "visitCommentText comment 5",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseCommentContainsEquals() {
    var code = "#a=b#";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile #a=b#<EOF><EOF>",
      "visitRow #a=b#<EOF>",
      "visitComment #a=b#<EOF>",
      "visitCommentStartAndText #a=b#",
      "visitCommentText a=b#",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseKeyWithoutValue() {
    var code = "foo";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo<EOF><EOF>",
      "visitRow foo<EOF>",
      "visitLine foo<EOF>",
      "visitKey foo",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseKeyEmptyValue() {
    var code = "foo=";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo=<EOF><EOF>",
      "visitRow foo=<EOF>",
      "visitLine foo=<EOF>",
      "visitKey foo",
      "visitEol <EOF>");
  }

  @ParameterizedTest
  @CsvSource({"foo#,foo#",
    "foo#=,foo#",
    "foo#:,foo#",
    "foo# ,foo#",
    "foo#\t,foo#",
    "foo#\f,foo#",
    "foo!,foo!",
    "foo!=,foo!",
    "foo! ,foo!",
    "foo!\t,foo!",
    "foo!\f,foo!",
  })
  void shouldParseKeyThatContainsCommentIndicator(String code, String key) {
    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile %s<EOF><EOF>".formatted(code),
      "visitRow %s<EOF>".formatted(code),
      "visitLine %s<EOF>".formatted(code),
      "visitKey %s".formatted(key),
      "visitEol <EOF>");
  }

  @ParameterizedTest
  @ValueSource(strings = {"=", " =", "= ", " = ",
    ":", " :", ": ", " : ",
    "\t", " \t", "\t ", " \t ",
    "\f", " \f", "\f ", " \f ",
    " ", "  "})
  void shouldParseKeyThatContainsHashAndSomeValue(String delimiter) {
    var code = "foo#%sbar".formatted(delimiter);

    parseProperties(code);

    var printableDelimiter = printable(delimiter);
    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo#%sbar<EOF><EOF>".formatted(printableDelimiter),
      "visitRow foo#%sbar<EOF>".formatted(printableDelimiter),
      "visitLine foo#%sbar<EOF>".formatted(printableDelimiter),
      "visitKey foo#",
      "visitKey bar",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseValueWithUnicodeCharacters() {
    var code = "foo=\u00FC\u00F6\u00E4\u0105\u0107\u0119\u0411\u0413\u0414\u00E0\u00E8\u00F9";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo=\u00FC\u00F6\u00E4\u0105\u0107\u0119\u0411\u0413\u0414\u00E0\u00E8\u00F9<EOF><EOF>",
      "visitRow foo=\u00FC\u00F6\u00E4\u0105\u0107\u0119\u0411\u0413\u0414\u00E0\u00E8\u00F9<EOF>",
      "visitLine foo=\u00FC\u00F6\u00E4\u0105\u0107\u0119\u0411\u0413\u0414\u00E0\u00E8\u00F9<EOF>",
      "visitKey foo",
      "visitKey \u00FC\u00F6\u00E4\u0105\u0107\u0119\u0411\u0413\u0414\u00E0\u00E8\u00F9",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseKeyWithUnicodeCharacters() {
    var code = "\u00FC\u00F6\u00E4\u0105\u0107\u0119\u0411\u0413\u0414\u00E0\u00E8\u00F9=bar";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile \u00FC\u00F6\u00E4\u0105\u0107\u0119\u0411\u0413\u0414\u00E0\u00E8\u00F9=bar<EOF><EOF>",
      "visitRow \u00FC\u00F6\u00E4\u0105\u0107\u0119\u0411\u0413\u0414\u00E0\u00E8\u00F9=bar<EOF>",
      "visitLine \u00FC\u00F6\u00E4\u0105\u0107\u0119\u0411\u0413\u0414\u00E0\u00E8\u00F9=bar<EOF>",
      "visitKey \u00FC\u00F6\u00E4\u0105\u0107\u0119\u0411\u0413\u0414\u00E0\u00E8\u00F9",
      "visitKey bar",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseKeyValueExtraSpace() {
    var code = """
      foo1 = bar1
      foo2= bar2
      foo3 =bar3""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo1 = bar1\\nfoo2= bar2\\nfoo3 =bar3<EOF><EOF>",
      "visitRow foo1 = bar1\\n",
      "visitLine foo1 = bar1\\n",
      "visitKey foo1",
      "visitKey bar1",
      "visitEol \\n",
      "visitRow foo2= bar2\\n",
      "visitLine foo2= bar2\\n",
      "visitKey foo2",
      "visitKey bar2",
      "visitEol \\n",
      "visitRow foo3 =bar3<EOF>",
      "visitLine foo3 =bar3<EOF>",
      "visitKey foo3",
      "visitKey bar3",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseDuplicatedKeys() {
    var code = """
      duplicateKey=first
      duplicateKey=second""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile duplicateKey=first\\nduplicateKey=second<EOF><EOF>",
      "visitRow duplicateKey=first\\n",
      "visitLine duplicateKey=first\\n",
      "visitKey duplicateKey",
      "visitKey first",
      "visitEol \\n",
      "visitRow duplicateKey=second<EOF>",
      "visitLine duplicateKey=second<EOF>",
      "visitKey duplicateKey",
      "visitKey second",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseKeyContainingDelimiter() {
    var code = "delimiterCharacters\\:\\=\\ = This is the value for the key \"delimiterCharacters\\:\\=\\ \"";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile delimiterCharacters\\:\\=\\ = This is the value for the key \"delimiterCharacters\\:\\=\\ \"<EOF><EOF>",
      "visitRow delimiterCharacters\\:\\=\\ = This is the value for the key \"delimiterCharacters\\:\\=\\ \"<EOF>",
      "visitLine delimiterCharacters\\:\\=\\ = This is the value for the key \"delimiterCharacters\\:\\=\\ \"<EOF>",
      "visitKey delimiterCharacters\\:\\=\\ ",
      "visitKey This is the value for the key \"delimiterCharacters\\:\\=\\ \"",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseMultilineValue() {
    var code = """
      multiline=This line \\
      continues""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile multiline=This line continues<EOF><EOF>",
      "visitRow multiline=This line continues<EOF>",
      "visitLine multiline=This line continues<EOF>",
      "visitKey multiline",
      "visitKey This line continues",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseWindowsPath() {
    var code = "path=c:\\wiki\\templates";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile path=c:\\wiki\\templates<EOF><EOF>",
      "visitRow path=c:\\wiki\\templates<EOF>",
      "visitLine path=c:\\wiki\\templates<EOF>",
      "visitKey path",
      "visitKey c:\\wiki\\templates",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseDoubleSlashAtTheEndOfValue() {
    var code = "evenKey=This is on one line\\\\";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile evenKey=This is on one line\\\\<EOF><EOF>",
      "visitRow evenKey=This is on one line\\\\<EOF>",
      "visitLine evenKey=This is on one line\\\\<EOF>",
      "visitKey evenKey",
      "visitKey This is on one line\\\\",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseOddSlashAndNextLineAsContinuationOfTheValue() {
    var code = """
      oddKey=This is line one and\\\\\\
      # This is line two""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile oddKey=This is line one and\\\\# This is line two<EOF><EOF>",
      "visitRow oddKey=This is line one and\\\\# This is line two<EOF>",
      "visitLine oddKey=This is line one and\\\\# This is line two<EOF>",
      "visitKey oddKey",
      "visitKey This is line one and\\\\# This is line two",
      "visitEol <EOF>");
  }

  @Test
  void shouldRemoveLeadingSpacesAfterLineBreak() {
    var code = """
      welcome = Welcome to \\
                Wikipedia!""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile welcome = Welcome to Wikipedia!<EOF><EOF>",
      "visitRow welcome = Welcome to Wikipedia!<EOF>",
      "visitLine welcome = Welcome to Wikipedia!<EOF>",
      "visitKey welcome",
      "visitKey Welcome to Wikipedia!",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseEscapedNewLineAndCarriageReturn() {
    var code = "valueWithEscapes=This is a newline\\n and a carriage return\\r and a tab\\t.";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile valueWithEscapes=This is a newline\\n and a carriage return\\r and a tab\\t.<EOF><EOF>",
      "visitRow valueWithEscapes=This is a newline\\n and a carriage return\\r and a tab\\t.<EOF>",
      "visitLine valueWithEscapes=This is a newline\\n and a carriage return\\r and a tab\\t.<EOF>",
      "visitKey valueWithEscapes",
      "visitKey This is a newline\\n and a carriage return\\r and a tab\\t.",
      "visitEol <EOF>");
  }

  @Test
  void shouldVerifyTextRanges() {
    var code = """
      # comment 1
      foo1 = bar1
      !comment2
      foo2=bar2
      foo3 =multiline\\
      value
      foo4=valueUnicode\u00FC\u00F6""";

    parseProperties(code);

    assertThat(visitorTextRanges.visited()).containsExactly(
      "visitPropertiesFile # comment 1\\nfoo1 = bar1\\n!comment2\\nfoo2=bar2\\nfoo3 =multilinevalue\\nfoo4=valueUnicodeüö<EOF><EOF>",
      "visitRow # comment 1\\n",
      "visitComment # comment 1\\n",
      "visitCommentStartAndText # comment 1 [1:0/1:10] StartIndex: 0",
      "visitCommentStartAndText # comment 1",
      "visitCommentText  comment 1",
      "visitEol \\n",
      "visitRow foo1 = bar1\\n",
      "visitLine foo1 = bar1\\n",
      "visitKey foo1 [2:0/2:3] StartIndex: 12",
      "visitKey foo1",
      "visitKey bar1 [2:7/2:10] StartIndex: 19",
      "visitKey bar1",
      "visitEol \\n",
      "visitRow !comment2\\n",
      "visitComment !comment2\\n",
      "visitCommentStartAndText !comment2 [3:0/3:8] StartIndex: 24",
      "visitCommentStartAndText !comment2",
      "visitCommentText comment2",
      "visitEol \\n",
      "visitRow foo2=bar2\\n",
      "visitLine foo2=bar2\\n",
      "visitKey foo2 [4:0/4:3] StartIndex: 34",
      "visitKey foo2",
      "visitKey bar2 [4:5/4:8] StartIndex: 39",
      "visitKey bar2",
      "visitEol \\n",
      "visitRow foo3 =multilinevalue\\n",
      "visitLine foo3 =multilinevalue\\n",
      "visitKey foo3 [5:0/5:3] StartIndex: 44",
      "visitKey foo3",
      "visitKey multilinevalue [5:6/6:4] StartIndex: 50",
      "visitKey multilinevalue",
      "visitEol \\n",
      "visitRow foo4=valueUnicodeüö<EOF>",
      "visitLine foo4=valueUnicodeüö<EOF>",
      "visitKey foo4 [7:0/7:3] StartIndex: 67",
      "visitKey foo4",
      "visitKey valueUnicodeüö [7:5/7:18] StartIndex: 72",
      "visitKey valueUnicodeüö",
      "visitEol <EOF>");
  }

  @Test
  void shouldVerifyTextRangesWhenCR() {
    var code = "# comment 1\rfoo1 = bar1\r!comment2\rfoo2=bar2\rfoo3 =value\rfoo4=valueUnicode\u00FC\u00F6";

    parseProperties(code);

    assertThat(visitorTextRanges.visited()).containsExactly(
      "visitPropertiesFile # comment 1\\rfoo1 = bar1\\r!comment2\\rfoo2=bar2\\rfoo3 =value\\rfoo4=valueUnicodeüö<EOF><EOF>",
      "visitRow # comment 1\\r",
      "visitComment # comment 1\\r",
      "visitCommentStartAndText # comment 1 [1:0/1:10] StartIndex: 0",
      "visitCommentStartAndText # comment 1",
      "visitCommentText  comment 1",
      "visitEol \\r",
      "visitRow foo1 = bar1\\r",
      "visitLine foo1 = bar1\\r",
      "visitKey foo1 [2:0/2:3] StartIndex: 12",
      "visitKey foo1",
      "visitKey bar1 [2:7/2:10] StartIndex: 19",
      "visitKey bar1",
      "visitEol \\r",
      "visitRow !comment2\\r",
      "visitComment !comment2\\r",
      "visitCommentStartAndText !comment2 [3:0/3:8] StartIndex: 24",
      "visitCommentStartAndText !comment2",
      "visitCommentText comment2",
      "visitEol \\r",
      "visitRow foo2=bar2\\r",
      "visitLine foo2=bar2\\r",
      "visitKey foo2 [4:0/4:3] StartIndex: 34",
      "visitKey foo2",
      "visitKey bar2 [4:5/4:8] StartIndex: 39",
      "visitKey bar2",
      "visitEol \\r",
      "visitRow foo3 =value\\r",
      "visitLine foo3 =value\\r",
      "visitKey foo3 [5:0/5:3] StartIndex: 44",
      "visitKey foo3",
      "visitKey value [5:6/5:10] StartIndex: 50",
      "visitKey value",
      "visitEol \\r",
      "visitRow foo4=valueUnicodeüö<EOF>",
      "visitLine foo4=valueUnicodeüö<EOF>",
      "visitKey foo4 [6:0/6:3] StartIndex: 56",
      "visitKey foo4",
      "visitKey valueUnicodeüö [6:5/6:18] StartIndex: 61",
      "visitKey valueUnicodeüö",
      "visitEol <EOF>");
  }

  @Test
  void shouldThrowExceptionForEmptyKey() {
    var code = "=abc";

    var exception = catchException(() -> parseProperties(code));

    assertThat(exception)
      .isInstanceOf(ParseException.class)
      .hasMessage("Cannot parse, extraneous input '=' expecting {<EOF>, LEADING_SPACING, COMMENT, CHARACTER} at null:1:1");
  }

  @Test
  void shouldParseEmptyString() {
    var code = "";
    parseProperties(code);
    assertThat(visitor.visited()).containsExactly("visitPropertiesFile <EOF>");
  }

  private void parseProperties(String code) {
    var propertiesFileContext = createPropertiesFileContext(code);

    visitor.visitPropertiesFile(propertiesFileContext);
    visitorTextRanges.visitPropertiesFile(propertiesFileContext);
  }

  @Test
  void shouldParseFileStartingWithNewline() {
    var code = """

      foo=bar
      """;

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo=bar\\n<EOF>",
      "visitRow foo=bar\\n",
      "visitLine foo=bar\\n",
      "visitKey foo",
      "visitKey bar",
      "visitEol \\n");
  }

  @Test
  void shouldParseFileStartingWithWhitespace() {
    var code = """
          foo=bar
      """;

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo=bar\\n<EOF>",
      "visitRow foo=bar\\n",
      "visitLine foo=bar\\n",
      "visitKey foo",
      "visitKey bar",
      "visitEol \\n");
  }

  private static String printable(String input) {
    return input.replace("\r\n", "\\r\\n")
      .replace("\r", "\\r")
      .replace("\n", "\\n")
      .replace("\t", "\\t")
      .replace("\f", "\\f")
      .replace("\u2028", "\\u2028")
      .replace("\u2029", "\\u2029");
  }

  static class TestVisitor extends PropertiesParserBaseVisitor<Void> {

    protected List<String> visited = new ArrayList<>();

    @Override
    public Void visitPropertiesFile(PropertiesParser.PropertiesFileContext ctx) {
      visited.add("visitPropertiesFile " + printText(ctx));
      return super.visitPropertiesFile(ctx);
    }

    @Override
    public Void visitRow(PropertiesParser.RowContext ctx) {
      visited.add("visitRow " + printText(ctx));
      return super.visitRow(ctx);
    }

    @Override
    public Void visitLine(PropertiesParser.LineContext ctx) {
      visited.add("visitLine " + printText(ctx));
      return super.visitLine(ctx);
    }

    @Override
    public Void visitKey(PropertiesParser.KeyContext ctx) {
      visited.add("visitKey " + printText(ctx));
      return super.visitKey(ctx);
    }

    @Override
    public Void visitEol(PropertiesParser.EolContext ctx) {
      visited.add("visitEol " + printText(ctx));
      return super.visitEol(ctx);
    }

    @Override
    public Void visitComment(PropertiesParser.CommentContext ctx) {
      visited.add("visitComment " + printText(ctx));
      return super.visitComment(ctx);
    }

    @Override
    public Void visitCommentText(PropertiesParser.CommentTextContext ctx) {
      visited.add("visitCommentText " + printText(ctx));
      return super.visitCommentText(ctx);
    }

    @Override
    public Void visitCommentStartAndText(PropertiesParser.CommentStartAndTextContext ctx) {
      visited.add("visitCommentStartAndText " + printText(ctx));
      return super.visitCommentStartAndText(ctx);
    }

    protected static String printText(ParseTree ctx) {
      return printable(ctx.getText());

    }

    public List<String> visited() {
      return visited;
    }
  }

  static class TextRangeTestVisitor extends TestVisitor {
    @Override
    public Void visitKey(PropertiesParser.KeyContext ctx) {
      visited.add("visitKey " + printText(ctx) + printTextRange(ctx));
      return super.visitKey(ctx);
    }

    @Override
    public Void visitCommentStartAndText(PropertiesParser.CommentStartAndTextContext ctx) {
      visited.add("visitCommentStartAndText " + printText(ctx) + printTextRange(ctx));
      return super.visitCommentStartAndText(ctx);
    }

    private String printTextRange(ParserRuleContext ctx) {
      var textRange = TextRanges.range(
        ctx.start.getLine(),
        ctx.start.getCharPositionInLine(),
        ctx.stop.getLine(),
        ctx.stop.getCharPositionInLine());
      return " " + textRange + " StartIndex: " + ctx.start.getStartIndex();
    }
  }
}
