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
package org.sonar.iac.kubernetes.visitors;

import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.helm.ShiftedMarkedYamlEngineException;

import static org.sonar.iac.common.yaml.YamlFileUtils.splitLines;

/**
 * This class is used to store all lines that has to be shifted.<p/>
 * The data are stored into this class through methods {@link #addLineSize(InputFileContext, int, int)} and {@link #addShiftedLine(InputFileContext, int, int)}.
 * Then we can use those data through the method {@link #computeShiftedLocation(InputFileContext, TextRange)}, which for a given {@link TextRange} will provide
 * a shifted {@link TextRange}.
 * Every store or access methods is required to provide the concerned {@link InputFileContext}, as the data are stored contextually to this object.
 * (It is more specifically using it's stored {@link InputFile#uri()})
 * This is especially used in helm context, when the issue we are detecting on the transformed code should be raised on the original code.
 */
public class LocationShifter {

  private final Map<URI, LinesShifting> linesShiftingPerContext = new HashMap<>();

  public void addShiftedLine(InputFileContext ctx, int transformedLine, int targetLine) {
    addShiftedLine(ctx, transformedLine, targetLine, targetLine);
  }

  public void addShiftedLine(InputFileContext ctx, int transformedLine, int targetStartLine, int targetEndLine) {
    var shifting = getOrCreateLinesShifting(ctx);
    var linesData = shifting.getOrCreateLinesData(transformedLine);
    linesData.targetStartLine = targetStartLine;
    linesData.targetEndLine = targetEndLine;
  }

  public void readLinesSizes(String source, InputFileContext ctx) {
    var lines = splitLines(source);
    for (var lineNumber = 1; lineNumber <= lines.length; lineNumber++) {
      addLineSize(ctx, lineNumber, lines[lineNumber - 1].length());
    }
  }

  // default scope for testing
  void addLineSize(InputFileContext ctx, int originalLine, int size) {
    getOrCreateLinesShifting(ctx).originalLinesSizes.put(originalLine, size);
  }

  /**
   * Adjusts the given {@link TextRange} to the original file. In case there is already a line number or line numbers range associated with
   * the given line (i.e. the line is directly followed by a comment #X:Y), use this value. In case there is no comment, this means that the line
   * appeared during rendering of Helm templates. Then, use the value from the next line with a comment.<p/>
   *
   * The following example illustrates this:<br/>
   * <code>
   * {{ include "another.tmpl" }} #1<br/>
   * foo: bar #2<br/>
   * </code>
   * <p/>
   *
   * After rendering, we can get something like
   * <code>
   * genFoo: bar<br/>
   * genFoo2: bar<br/>
   * genFoo3: bar<br/>
   * genFoo4: bar #1<br/>
   * foo: bar #2<br/>
   * </code>
   * <p/>
   *
   * And now we want to raise an issue on `genFoo3`, which is line 3, but originates from line 1.
   * We need to find the next line number comment to get its original line correctly
   */
  public TextRange computeShiftedLocation(InputFileContext ctx, TextRange textRange) {
    if (!linesShiftingPerContext.containsKey(ctx.inputFile.uri())) {
      // No location shifting is recorded for this file, we are in a regular Kubernetes context.
      return textRange;
    }

    var shifting = getOrCreateLinesShifting(ctx);
    int lineStart = textRange.start().line();
    int lineEnd = textRange.end().line();

    var rangeStart = shifting.getClosestLineData(lineStart)
      .map(p -> p.targetStartLine)
      .orElse(shifting.getLastOriginalLine());
    var start = new TextPointer(rangeStart, 0);

    var endLineData = shifting.getClosestLineData(lineEnd);
    var rangeEnd = endLineData
      .map(p -> p.targetEndLine)
      .orElse(shifting.getLastOriginalLine());
    var rangeEndLineLength = getOrCreateLinesShifting(ctx).originalLinesSizes.getOrDefault(rangeEnd, 0);
    var end = new TextPointer(rangeEnd, rangeEndLineLength);

    return new TextRange(start, end);
  }

  public MarkedYamlEngineException shiftMarkedYamlException(InputFileContext inputFileContext, MarkedYamlEngineException exception) {
    var problemMark = exception.getProblemMark();
    if (problemMark.isPresent()) {
      var markInTransformedCode = problemMark.get();
      // snakeyaml has a single point in problem mark, which it expands into a small piece of surrounding text for readability.
      // There is no actual range to highlight exception at. Note: snakeyaml uses 0-based indexing, but we don't need to adjust, as
      // we will handle this exception later as if it came from snakeyaml.
      var rangeInException = TextRanges.range(
        markInTransformedCode.getLine(),
        markInTransformedCode.getColumn(),
        markInTransformedCode.getLine(),
        markInTransformedCode.getColumn());
      var shiftedRange = computeShiftedLocation(inputFileContext, rangeInException);
      var shiftedMark = new Mark(
        markInTransformedCode.getName(),
        markInTransformedCode.getIndex(),
        shiftedRange.start().line(),
        shiftedRange.start().lineOffset(),
        markInTransformedCode.getBuffer(),
        markInTransformedCode.getPointer());
      return new ShiftedMarkedYamlEngineException(exception, shiftedMark);
    }
    return exception;
  }

  private LinesShifting getOrCreateLinesShifting(InputFileContext ctx) {
    return linesShiftingPerContext.computeIfAbsent(ctx.inputFile.uri(), context -> new LinesShifting());
  }

  /**
   * Store information related to an original line number.
   * The {@link #linesData} Map contain the original line number as the key.
   * The original line length and target line number are stored in the value, as a {@link LineData} object.
   */
  static class LinesShifting {

    /**
     * The key is line number 1-based - first line number is 1.
     */
    private final Map<Integer, LineData> linesData = new TreeMap<>();

    /**
     * The key is line number 1-based - first line number is 1.
     */
    private final Map<Integer, Integer> originalLinesSizes = new HashMap<>();

    private LineData getOrCreateLinesData(Integer lineNumber) {
      return linesData.computeIfAbsent(lineNumber, line -> new LineData());
    }

    private Optional<LineData> getClosestLineData(Integer lineNumber) {
      return linesData.entrySet().stream()
        .dropWhile(p -> p.getKey() < lineNumber)
        .findFirst()
        .map(Map.Entry::getValue);
    }

    private Integer getLastOriginalLine() {
      return originalLinesSizes.keySet().stream()
        .sorted(Comparator.reverseOrder())
        .mapToInt(i -> i)
        .findFirst()
        .orElse(0);
    }
  }

  static class LineData {
    private Integer targetStartLine;
    private Integer targetEndLine;
  }
}
