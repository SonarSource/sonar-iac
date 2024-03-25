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

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.helm.ShiftedMarkedYamlEngineException;
import org.sonar.iac.helm.tree.api.FieldNode;
import org.sonar.iac.helm.tree.api.Location;
import org.sonar.iac.helm.tree.impl.LocationImpl;
import org.sonar.iac.helm.tree.utils.GoTemplateAstHelper;

import static org.sonar.iac.common.yaml.YamlFileUtils.splitLines;

/**
 * This class is used to store all lines that has to be shifted.<p/>
 * The data are stored into this class through methods {@link #addLineSize(HelmInputFileContext, int, int)} and {@link #addShiftedLine(HelmInputFileContext, int, int, int)}.
 * Then we can use those data through the method {@link #computeShiftedLocation(HelmInputFileContext, TextRange)}, which for a given {@link TextRange} will provide
 * a shifted {@link TextRange}.
 * Every store or access methods is required to provide the concerned {@link HelmInputFileContext}, as the data are stored contextually to this object.
 * (It is more specifically using it's stored {@link InputFile#uri()})
 * This is especially used in helm context, when the issue we are detecting on the transformed code should be raised on the original code.
 */
public class LocationShifter {
  private static final Logger LOG = LoggerFactory.getLogger(LocationShifter.class);

  private LocationShifter() {
    throw new IllegalStateException("Utility class");
  }

  public static void addShiftedLine(HelmInputFileContext ctx, int transformedLine, int targetStartLine, int targetEndLine) {
    var shifting = ctx.sourceMap();
    var linesData = shifting.linesData(transformedLine);
    linesData.targetStartLine = targetStartLine;
    linesData.targetEndLine = targetEndLine;
  }

  public static void readLinesSizes(String source, HelmInputFileContext ctx) {
    var lines = splitLines(source);
    for (var lineNumber = 1; lineNumber <= lines.length; lineNumber++) {
      addLineSize(ctx, lineNumber, lines[lineNumber - 1].length());
    }
  }

  // default scope for testing
  static void addLineSize(HelmInputFileContext ctx, int originalLine, int size) {
    ctx.sourceMap().originalLinesSizes.put(originalLine, size);
  }

  /**
   * It calculates shifted location in 3 steps:<br/>
   * <ul>
   *   <li>see {@link LocationShifter#computeShiftedLocation(HelmInputFileContext, TextRange)}</li>
   *   <li>see {@link LocationShifter#computeHelmValuePathTextRange(HelmInputFileContext, TextRange)}</li>
   *   <li>if the location from 1st and 2nd step is the same then first line location is taken and line offsets of original TextRange</li>
   * </ul>
   */
  public static TextRange shiftLocation(HelmInputFileContext currentCtx, TextRange textRange) {
    var shiftedToLine = computeShiftedLocation(currentCtx, textRange);
    var shiftedTextRange = computeHelmValuePathTextRange(currentCtx, shiftedToLine);
    if (shiftedTextRange.equals(shiftedToLine)) {
      // The shiftedTextRange doesn't contain Value path (Helm expression) so we can keep the line offsets
      return TextRanges.range(
        shiftedTextRange.start().line(),
        textRange.start().lineOffset(),
        shiftedTextRange.end().line(),
        textRange.end().lineOffset());
    }
    return shiftedTextRange;
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
  public static TextRange computeShiftedLocation(HelmInputFileContext ctx, TextRange textRange) {
    var shifting = ctx.sourceMap();
    if (shifting.isNotInitialized()) {
      // No location shifting is recorded for this file.
      return textRange;
    }

    int lineStart = textRange.start().line();
    int lineEnd = textRange.end().line();

    var rangeStart = shifting.getClosestLineData(lineStart)
      .map(p -> p.targetStartLine)
      .orElse(shifting.getLastOriginalLine());

    var endLineData = shifting.getClosestLineData(lineEnd);
    var rangeEnd = endLineData
      .map(p -> p.targetEndLine)
      .orElse(shifting.getLastOriginalLine());
    var rangeEndLineLength = ctx.sourceMap().originalLinesSizes.getOrDefault(rangeEnd, 0);

    return TextRanges.range(rangeStart, 0, rangeEnd, rangeEndLineLength);
  }

  /**
   * Adjust given {@link TextRange} to Helm template Value Path location.
   * If no Value Path is found at provided {@link TextRange} the original {@link TextRange} is returned. <p/>
   *
   * The following example illustrates this:<br/>
   * <code>
   * foo: {{ .Values.privilege }}
   * </code><br/><br/>
   *
   * What will be evaluated to:<br/>
   * <code>
   * foo: true #1
   * </code><br/><br/>
   *
   * For <code>TextRange(1,0,1,28)</code> (valid for original source) the following {@link TextRange} will be returned:<br/>
   * <pre>
   * foo: {{ .Values.privilege }}
   * #              ^^^^^^^^^^^^^
   * </pre><br/>
   *
   * The precision of highlighting depends on the precision of the nodes of Go template AST.
   */
  public static TextRange computeHelmValuePathTextRange(HelmInputFileContext helmContext, TextRange textRange) {
    var goTemplateTree = helmContext.getGoTemplateTree();
    var sourceWithComments = helmContext.getSourceWithComments();
    if (goTemplateTree != null && sourceWithComments != null) {
      try {
        var contents = helmContext.inputFile.contents();
        // The go template tree contains locations aligned to source code with additional trailing line numbers comments
        var textRanges = GoTemplateAstHelper.findValuePathNodes(goTemplateTree, textRange, sourceWithComments)
          .map(FieldNode::location)
          .map(LocationShifter::fixLocation)
          .map(location -> location.toTextRange(sourceWithComments))
          .toList();
        if (!textRanges.isEmpty()) {
          // The text range may be too big, so it needs to be adjusted to the original source code
          // TODO: When SONARIAC-1337 wil be implemented maybe this will be not needed anymore.
          return TextRanges.merge(textRanges).trimEndToText(contents);
        }
      } catch (IOException e) {
        var message = String.format("Unable to read file %s raising issue on less precise location", helmContext.inputFile);
        LOG.debug(message, e);
      }
    }
    return textRange;
  }

  // Currently the Go AST node location is not precise, and it includes usually new line what later {@link Location#toTextRange} cause that
  // issue is reported in 2 lines
  private static Location fixLocation(Location location) {
    if (location.length() > 1) {
      return new LocationImpl(location.position(), location.length() - 1);
    }
    return location;
  }

  public static SecondaryLocation computeShiftedSecondaryLocation(HelmInputFileContext ctx, SecondaryLocation secondaryLocation) {
    InputFile fileToRaiseOn = ctx.retrieveFileToRaiseOn(secondaryLocation);
    if (fileToRaiseOn == null || !fileToRaiseOn.equals(ctx.inputFile)) {
      return secondaryLocation;
    }
    var range = computeShiftedLocation(ctx, secondaryLocation.textRange);
    return new SecondaryLocation(range, secondaryLocation.message, secondaryLocation.filePath);
  }

  public static MarkedYamlEngineException shiftMarkedYamlException(HelmInputFileContext inputFileContext, MarkedYamlEngineException exception) {
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

  /**
   * Store information related to an original line number.
   * The {@link #linesData} Map contain the original line number as the key.
   * The original line length and target line number are stored in the value, as a {@link LineData} object.
   */
  public static class LinesShifting {

    /**
     * The key is line number 1-based - first line number is 1.
     */
    private final Map<Integer, LineData> linesData = new TreeMap<>();

    /**
     * The key is line number 1-based - first line number is 1.
     */
    private final Map<Integer, Integer> originalLinesSizes = new HashMap<>();

    private boolean isNotInitialized() {
      return linesData.isEmpty() && originalLinesSizes.isEmpty();
    }

    private LineData linesData(Integer lineNumber) {
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
