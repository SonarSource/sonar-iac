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
package org.sonar.iac.terraform.parser;

import com.eclipsesource.json.JsonHandler;
import com.eclipsesource.json.Location;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.tree.impl.SeparatedTreesImpl;
import org.sonar.iac.terraform.tree.impl.json.JsonLiteralExprTreeImpl;
import org.sonar.iac.terraform.tree.impl.json.JsonObjectElementTreeImpl;
import org.sonar.iac.terraform.tree.impl.json.JsonObjectTreeImpl;
import org.sonar.iac.terraform.tree.impl.json.JsonSyntaxTokenImpl;
import org.sonar.iac.terraform.tree.impl.json.JsonTupleTreeImpl;

/**
 * The minimal-json {@link JsonHandler} that builds the Terraform JSON tree representation while preserving
 * per-node text ranges. Translates the parser's body-local {@link Location} (1-indexed line + 1-indexed column)
 * into absolute file coordinates using a single offset: the line of the surrounding {@code <<TAG} marker.
 *
 * <p>Body's line 1 is the file line right after the heredoc start ({@code heredocStartLine + 1}); column
 * indexing matches one-to-one (minimal-json column = file lineOffset + 1). Synthetic separators (commas)
 * and the colon between key and value cannot be precisely located through the handler API and fall back
 * to a 1-char range derived from a nearby tracked location.
 */
final class JsonBodyHandler extends JsonHandler<List<ExpressionTree>, JsonBodyHandler.ObjectState> {

  private final int heredocStartLine;

  private final Deque<Location> valueStartStack = new ArrayDeque<>();
  private ExpressionTree currentValue;

  JsonBodyHandler(int heredocStartLine) {
    this.heredocStartLine = heredocStartLine;
  }

  ExpressionTree getResult() {
    return currentValue;
  }

  // ----- scalars -----

  @Override
  public void startNull() {
    valueStartStack.push(getLocation());
  }

  @Override
  public void endNull() {
    finishScalar(Kind.NULL_LITERAL, "null");
  }

  @Override
  public void startBoolean() {
    valueStartStack.push(getLocation());
  }

  @Override
  public void endBoolean(boolean value) {
    finishScalar(Kind.BOOLEAN_LITERAL, Boolean.toString(value));
  }

  @Override
  public void startString() {
    valueStartStack.push(getLocation());
  }

  @Override
  public void endString(String string) {
    finishScalar(Kind.STRING_LITERAL, string);
  }

  @Override
  public void startNumber() {
    valueStartStack.push(getLocation());
  }

  @Override
  public void endNumber(String string) {
    finishScalar(Kind.NUMERIC_LITERAL, string);
  }

  private void finishScalar(Kind kind, String value) {
    Location start = valueStartStack.pop();
    Location end = getLocation();
    TextRange range = rangeBetween(start, end);
    SyntaxToken token = new JsonSyntaxTokenImpl(value, range);
    currentValue = new JsonLiteralExprTreeImpl(kind, value, token, range);
  }

  // ----- arrays -----

  @Override
  public List<ExpressionTree> startArray() {
    valueStartStack.push(getLocation());
    return new ArrayList<>();
  }

  @Override
  public void endArrayValue(List<ExpressionTree> array) {
    array.add(currentValue);
  }

  @Override
  public void endArray(List<ExpressionTree> array) {
    Location start = valueStartStack.pop();
    Location end = getLocation();
    TextRange range = rangeBetween(start, end);
    SyntaxToken open = new JsonSyntaxTokenImpl("[", singleCharAt(start));
    SyntaxToken close = new JsonSyntaxTokenImpl("]", singleCharBefore(end));
    SeparatedTrees<ExpressionTree> elements = new SeparatedTreesImpl<>(array, fakeSeparators(array.size(), range));
    currentValue = new JsonTupleTreeImpl(elements, open, close, range);
  }

  // ----- objects -----

  /** Per-object scratch state held in the accumulator slot — keeps nested objects independent. */
  static final class ObjectState {
    final List<ObjectElementTree> members = new ArrayList<>();
    Location memberStart;
    ExpressionTree pendingKey;
    SyntaxToken pendingColon;
  }

  @Override
  public ObjectState startObject() {
    valueStartStack.push(getLocation());
    return new ObjectState();
  }

  @Override
  public void startObjectName(ObjectState state) {
    state.memberStart = getLocation();
    valueStartStack.push(getLocation());
  }

  @Override
  public void endObjectName(ObjectState state, String name) {
    Location start = valueStartStack.pop();
    Location end = getLocation();
    TextRange keyRange = rangeBetween(start, end);
    SyntaxToken keyToken = new JsonSyntaxTokenImpl(name, keyRange);
    state.pendingKey = new JsonLiteralExprTreeImpl(Kind.STRING_LITERAL, name, keyToken, keyRange);
    // Colon comes between endObjectName and startObjectValue, after possible whitespace.
    // We approximate it as a 1-char range at endObjectName's location.
    state.pendingColon = new JsonSyntaxTokenImpl(":", singleCharAt(end));
  }

  @Override
  public void endObjectValue(ObjectState state, String name) {
    Location end = getLocation();
    TextRange memberRange = rangeBetween(state.memberStart, end);
    state.members.add(new JsonObjectElementTreeImpl(state.pendingKey, state.pendingColon, currentValue, memberRange));
    state.memberStart = null;
    state.pendingKey = null;
    state.pendingColon = null;
  }

  @Override
  public void endObject(ObjectState state) {
    Location start = valueStartStack.pop();
    Location end = getLocation();
    TextRange range = rangeBetween(start, end);
    SyntaxToken open = new JsonSyntaxTokenImpl("{", singleCharAt(start));
    SyntaxToken close = new JsonSyntaxTokenImpl("}", singleCharBefore(end));
    SeparatedTrees<ObjectElementTree> elements = new SeparatedTreesImpl<>(state.members, fakeSeparators(state.members.size(), range));
    currentValue = new JsonObjectTreeImpl(elements, open, close, range);
  }

  // ----- helpers -----

  private TextRange rangeBetween(Location start, Location end) {
    return new TextRange(toTextPointer(start), toTextPointer(end));
  }

  private TextPointer toTextPointer(Location loc) {
    return new TextPointer(heredocStartLine + loc.line, loc.column - 1);
  }

  private TextRange singleCharAt(Location loc) {
    int line = heredocStartLine + loc.line;
    int col = loc.column - 1;
    return new TextRange(new TextPointer(line, col), new TextPointer(line, col + 1));
  }

  private TextRange singleCharBefore(Location loc) {
    int line = heredocStartLine + loc.line;
    int col = loc.column - 1;
    return new TextRange(new TextPointer(line, col - 1), new TextPointer(line, col));
  }

  private static List<SyntaxToken> fakeSeparators(int elementCount, TextRange fallback) {
    if (elementCount <= 1) {
      return Collections.emptyList();
    }
    List<SyntaxToken> separators = new ArrayList<>(elementCount - 1);
    for (int i = 0; i < elementCount - 1; i++) {
      separators.add(new JsonSyntaxTokenImpl(",", fallback));
    }
    return separators;
  }
}
