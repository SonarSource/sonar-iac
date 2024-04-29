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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesParserBaseVisitorTest {

  TestVisitor visitor = new TestVisitor();

  @Test
  void shouldParseSimpleExpression() {
    var code = """
      foo=bar""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo=bar<EOF>",
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
      "visitPropertiesFile # example comment\\nfoo=bar\\n#comment at the end<EOF>",
      "visitRow # example comment\\n",
      "visitComment # example comment\\n",
      "visitEol \\n",
      "visitRow foo=bar\\n",
      "visitLine foo=bar\\n",
      "visitKey foo",
      "visitKey bar",
      "visitEol \\n",
      "visitRow #comment at the end<EOF>",
      "visitComment #comment at the end<EOF>",
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
      "visitPropertiesFile ! also a comment\\nfoo=bar\\n! also comment at the end<EOF>",
      "visitRow ! also a comment\\n",
      "visitComment ! also a comment\\n",
      "visitEol \\n",
      "visitRow foo=bar\\n",
      "visitLine foo=bar\\n",
      "visitKey foo",
      "visitKey bar",
      "visitEol \\n",
      "visitRow ! also comment at the end<EOF>",
      "visitComment ! also comment at the end<EOF>",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseURI() {
    var code = "spring.activemq.broker-url=tcp://127.0.0.1:61616";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile spring.activemq.broker-url=tcp://127.0.0.1:61616<EOF>",
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
      "visitPropertiesFile logging.pattern.console=%d{mm:ss.SSS} %-5p [%-31t] [%-54logger{0}] %marker%m%ex{full} - %logger - %F:%L%n<EOF>",
      "visitRow logging.pattern.console=%d{mm:ss.SSS} %-5p [%-31t] [%-54logger{0}] %marker%m%ex{full} - %logger - %F:%L%n<EOF>",
      "visitLine logging.pattern.console=%d{mm:ss.SSS} %-5p [%-31t] [%-54logger{0}] %marker%m%ex{full} - %logger - %F:%L%n<EOF>",
      "visitKey logging.pattern.console",
      "visitKey %d{mm:ss.SSS} %-5p [%-31t] [%-54logger{0}] %marker%m%ex{full} - %logger - %F:%L%n",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseListProperties() {
    var code = """
      mail.defaultRecipients[0]=admin@mail.com
      mail.defaultRecipients[1]=owner@mail.com""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile mail.defaultRecipients[0]=admin@mail.com\\nmail.defaultRecipients[1]=owner@mail.com<EOF>",
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
      "visitPropertiesFile foo=bar\\n\\nbar=foo<EOF>",
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
      "visitPropertiesFile foo1=bar1\\rfoo2=bar2\\r\\r\\rfoo3=bar3\\r",
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
      "visitPropertiesFile foo1=bar1\\r\\nfoo2=bar2\\r\\n\\r\\n\\r\\nfoo3=bar3\\r\\n",
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
      "visitPropertiesFile foo1=bar1\\u2028foo2=bar2\\u2028\\u2028\\u2028foo3=bar3\\u2028",
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
      "visitPropertiesFile foo1=bar1\\u2029foo2=bar2\\u2029\\u2029\\u2029foo3=bar3\\u2029",
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


  
  private void parseProperties(String code) {
    var inputCode = CharStreams.fromString(code);
    var propertiesLexer = new PropertiesLexer(inputCode);
    var commonTokenStream = new CommonTokenStream(propertiesLexer);
    var parser = new PropertiesParser(commonTokenStream);
    var propertiesFileContext = parser.propertiesFile();

    visitor.visitPropertiesFile(propertiesFileContext);
  }

  static class TestVisitor extends PropertiesParserBaseVisitor<Void> {

    private List<String> visited = new ArrayList<>();

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
      // ctx.key()

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

    private static String printText(ParseTree ctx) {
      return ctx.getText()
        .replace("\r\n", "\\r\\n")
        .replace("\r", "\\r")
        .replace("\n", "\\n")
        .replace("\u2028", "\\u2028")
        .replace("\u2029", "\\u2029");
    }

    public List<String> visited() {
      return visited;
    }
  }
}
