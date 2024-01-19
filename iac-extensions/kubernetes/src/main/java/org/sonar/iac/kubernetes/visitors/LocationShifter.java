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
import java.util.HashMap;
import java.util.Map;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.visitors.InputFileContext;

/**
 * This class is used to store all lines that has to be shifted.<p/>
 * "Original" refers to the line in the processed file, i.e. the one that is being analyzed.
 * "Target" refers to the line in the original file, i.e. the one that should be used to raise issues.<p/>
 * The data are stored into this class through methods {@link #addLineSize(InputFileContext, int, int)} and {@link #addShiftedLine(InputFileContext, int, int)}.
 * Then we can use those data through the method {@link #computeShiftedLocation(InputFileContext, TextRange)}, which for a given {@link TextRange} will provide
 * a shifted {@link TextRange}.
 * Every store or access methods is required to provide the concerned {@link InputFileContext}, as the data are stored contextually to this object.
 * (It is more specifically using it's stored {@link InputFile#uri()})
 * This is especially used in helm context, when the issue we are detecting on the transformed code should be raised on the original code.
 */
public class LocationShifter {

  private final Map<URI, LinesShifting> linesShiftingPerContext = new HashMap<>();

  public void addShiftedLine(InputFileContext ctx, int originalLine, int targetLine) {
    var linesData = getOrCreateLinesShifting(ctx)
      .getOrCreateLinesData(originalLine);
    linesData.targetStartLine = targetLine;
    linesData.targetEndLine = targetLine;
  }

  public void addShiftedLine(InputFileContext ctx, int originalLine, int targetStartLine, int targetEndLine) {
    var linesData = getOrCreateLinesShifting(ctx)
      .getOrCreateLinesData(originalLine);
    linesData.targetStartLine = targetStartLine;
    linesData.targetEndLine = targetEndLine;
  }

  public void addLineSize(InputFileContext ctx, int originalLine, int size) {
    getOrCreateLinesShifting(ctx)
      .getOrCreateLinesData(originalLine).originalLineSize = size;
  }

  public TextRange computeShiftedLocation(InputFileContext ctx, TextRange textRange) {
    int lineStart = textRange.start().line();
    int lineEnd = textRange.end().line();
    var shifting = getOrCreateLinesShifting(ctx);

    if (!isShifted(shifting, lineStart) && !isShifted(shifting, lineEnd)) {
      return textRange;
    }

    TextPointer start;
    TextPointer end;

    if (isShifted(shifting, lineStart)) {
      start = new TextPointer(shifting.linesData.get(lineStart).targetStartLine, 0);
    } else {
      start = new TextPointer(textRange.start().line(), textRange.start().lineOffset() - 1);
    }

    if (isShifted(shifting, lineEnd)) {
      var lineEndData = shifting.linesData.get(lineEnd);
      int targetEndLine = lineEndData.targetEndLine;
      end = new TextPointer(targetEndLine, shifting.linesData.get(targetEndLine).originalLineSize);
    } else {
      end = textRange.end();
    }

    return new TextRange(start, end);
  }

  private static boolean isShifted(LinesShifting shifting, int lineStart) {
    var linesDataShifting = shifting.linesData;
    var lineData = linesDataShifting.get(lineStart);
    return lineData != null && lineData.targetStartLine != null;
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
    private final Map<Integer, LineData> linesData = new HashMap<>();

    private LineData getOrCreateLinesData(Integer originalLine) {
      return linesData.computeIfAbsent(originalLine, line -> new LineData());
    }
  }

  static class LineData {
    private Integer targetStartLine;
    private Integer targetEndLine;
    private int originalLineSize;
  }
}
