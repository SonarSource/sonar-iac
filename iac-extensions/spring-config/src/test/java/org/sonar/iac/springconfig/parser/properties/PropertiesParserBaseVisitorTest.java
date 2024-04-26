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
  void shouldParseSimpleExpressionWithComment() {
    var code = """
      # example comment
      foo=bar
      #comment at the end""";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile # example comment\n" +
        "foo=bar\n" +
        "#comment at the end<EOF>",
      "visitRow # example comment\n",
      "visitComment # example comment\n",
      "visitEol \n",
      "visitRow foo=bar\n",
      "visitLine foo=bar\n",
      "visitKey foo",
      "visitKey bar",
      "visitEol \n",
      "visitRow #comment at the end<EOF>",
      "visitComment #comment at the end<EOF>",
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
      "visitPropertiesFile mail.defaultRecipients[0]=admin@mail.com\n" +
        "mail.defaultRecipients[1]=owner@mail.com<EOF>",
      "visitRow mail.defaultRecipients[0]=admin@mail.com\n",
      "visitLine mail.defaultRecipients[0]=admin@mail.com\n",
      "visitKey mail.defaultRecipients[0]",
      "visitKey admin@mail.com",
      "visitEol \n",
      "visitRow mail.defaultRecipients[1]=owner@mail.com<EOF>",
      "visitLine mail.defaultRecipients[1]=owner@mail.com<EOF>",
      "visitKey mail.defaultRecipients[1]",
      "visitKey owner@mail.com",
      "visitEol <EOF>");
  }

  @Test
  void shouldParseDifferentNewLines() {
    var code = "foo1=bar1\nfoo2=bar2\rfoo3=bar3\r\nfoo4=4\n";

    parseProperties(code);

    assertThat(visitor.visited()).containsExactly(
      "visitPropertiesFile foo1=bar1\n" +
      // the character after \r is missing
        "foo2=bar2oo3=bar3\r\n" +
        "foo4=4\n",
      "visitRow foo1=bar1\n",
      "visitLine foo1=bar1\n",
      "visitKey foo1",
      "visitKey bar1",
      "visitEol \n",
      "visitRow foo2=bar2oo3=bar3\r\n",
      "visitLine foo2=bar2oo3=bar3\r\n",
      "visitKey foo2",
      "visitKey bar2oo3=bar3",
      "visitEol \r\n",
      "visitRow foo4=4\n",
      "visitLine foo4=4\n",
      "visitKey foo4",
      "visitKey 4",
      "visitEol \n");
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
      visited.add("visitPropertiesFile " + ctx.getText());
      return super.visitPropertiesFile(ctx);
    }

    @Override
    public Void visitRow(PropertiesParser.RowContext ctx) {
      visited.add("visitRow " + ctx.getText());
      return super.visitRow(ctx);
    }

    @Override
    public Void visitLine(PropertiesParser.LineContext ctx) {
      visited.add("visitLine " + ctx.getText());
      return super.visitLine(ctx);
    }

    @Override
    public Void visitKey(PropertiesParser.KeyContext ctx) {
      visited.add("visitKey " + ctx.getText());
      return super.visitKey(ctx);
    }

    @Override
    public Void visitEol(PropertiesParser.EolContext ctx) {
      visited.add("visitEol " + ctx.getText());
      return super.visitEol(ctx);
    }

    @Override
    public Void visitComment(PropertiesParser.CommentContext ctx) {
      visited.add("visitComment " + ctx.getText());
      return super.visitComment(ctx);
    }

    public List<String> visited() {
      return visited;
    }
  }
}
