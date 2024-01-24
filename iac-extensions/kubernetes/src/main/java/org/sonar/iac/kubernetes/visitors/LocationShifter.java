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
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.visitors.InputFileContext;

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
    var shifting = getOrCreateLinesShifting(ctx);
    var linesData = shifting.getOrCreateLinesData(transformedLine);
    linesData.targetStartLine = targetLine;
    linesData.targetEndLine = targetLine;
    linesData.originalLineSize = shifting.originalLinesSizes.getOrDefault(targetLine, 0);
  }

  public void addShiftedLine(InputFileContext ctx, int transformedLine, int targetStartLine, int targetEndLine) {
    var shifting = getOrCreateLinesShifting(ctx);
    var linesData = shifting.getOrCreateLinesData(transformedLine);
    linesData.targetStartLine = targetStartLine;
    linesData.targetEndLine = targetEndLine;
    linesData.originalLineSize = shifting.originalLinesSizes.getOrDefault(targetEndLine, 0);
  }

  public void addLineSize(InputFileContext ctx, int originalLine, int size) {
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
    var end = new TextPointer(rangeEnd,
      endLineData.map(p -> p.originalLineSize)
        .orElse(shifting.originalLinesSizes.getOrDefault(shifting.getLastOriginalLine(), 0)));

    return new TextRange(start, end);
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
    private final Map<Integer, LineData> linesData = new TreeMap<>();
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
    private int originalLineSize;
  }
}
