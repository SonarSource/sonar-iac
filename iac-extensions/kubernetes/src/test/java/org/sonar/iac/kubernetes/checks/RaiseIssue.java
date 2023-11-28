/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.kubernetes.checks;

import java.util.List;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.yaml.tree.FileTree;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class dedicated to raise an issue at a specific location for test purpose.
 * It is a class which provide 4 subclass, each of them dedicated to use a different method implementation of
 * {@link org.sonar.iac.common.api.checks.CheckContext} reportIssue.
 * The base class {@link RaiseIssue} is providing null {@link TextRange}, to support the use case where the primary location has no {@link TextRange}.
 */
public class RaiseIssue implements IacCheck {

  protected final String message;

  public RaiseIssue(String message) {
    this.message = message;
  }

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, tree) -> {
      ctx.reportIssue((TextRange) null, message);
    });
  }

  public abstract static class RaiseIssueOnLocation extends RaiseIssue {

    protected final int startLine;
    protected final int startOffset;
    protected final int endLine;
    protected final int endOffset;

    public RaiseIssueOnLocation(int startLine, int startOffset, int endLine, int endOffset, String message) {
      super(message);
      this.startLine = startLine;
      this.startOffset = startOffset;
      this.endLine = endLine;
      this.endOffset = endOffset;
    }
  }

  public static class RaiseIssueOnTextRange extends RaiseIssueOnLocation {
    public RaiseIssueOnTextRange(int startLine, int startOffset, int endLine, int endOffset, String message) {
      super(startLine, startOffset, endLine, endOffset, message);
    }

    @Override
    public void initialize(InitContext init) {
      init.register(FileTree.class, (ctx, tree) -> {
        var textRange = TextRanges.range(startLine, startOffset, endLine, endOffset);
        ctx.reportIssue(textRange, message);
      });
    }
  }

  public static class RaiseIssueOnHasTextRange extends RaiseIssueOnLocation {
    public RaiseIssueOnHasTextRange(int startLine, int startOffset, int endLine, int endOffset, String message) {
      super(startLine, startOffset, endLine, endOffset, message);
    }

    @Override
    public void initialize(InitContext init) {
      init.register(FileTree.class, (ctx, tree) -> {
        var hasTextRange = mock(HasTextRange.class);
        when(hasTextRange.textRange()).thenReturn(TextRanges.range(startLine, startOffset, endLine, endOffset));
        ctx.reportIssue(hasTextRange, message);
      });
    }
  }

  public static class RaiseIssueOnSecondaryLocation extends RaiseIssueOnLocation {
    private final SecondaryLocation secondaryLocation;

    public RaiseIssueOnSecondaryLocation(int startLine, int startOffset, int endLine, int endOffset, String message, SecondaryLocation secondaryLocation) {
      super(startLine, startOffset, endLine, endOffset, message);
      this.secondaryLocation = secondaryLocation;
    }

    @Override
    public void initialize(InitContext init) {
      init.register(FileTree.class, (ctx, tree) -> {
        var hasTextRange = mock(HasTextRange.class);
        when(hasTextRange.textRange()).thenReturn(TextRanges.range(startLine, startOffset, endLine, endOffset));
        ctx.reportIssue(hasTextRange, message, secondaryLocation);
      });
    }
  }

  public static class RaiseIssueOnSecondaryLocations extends RaiseIssueOnLocation {
    private final List<SecondaryLocation> secondaryLocations;

    public RaiseIssueOnSecondaryLocations(int startLine, int startOffset, int endLine, int endOffset, String message, List<SecondaryLocation> secondaryLocations) {
      super(startLine, startOffset, endLine, endOffset, message);
      this.secondaryLocations = secondaryLocations;
    }

    @Override
    public void initialize(InitContext init) {
      init.register(FileTree.class, (ctx, tree) -> {
        var hasTextRange = mock(HasTextRange.class);
        when(hasTextRange.textRange()).thenReturn(TextRanges.range(startLine, startOffset, endLine, endOffset));
        ctx.reportIssue(hasTextRange, message, secondaryLocations);
      });
    }
  }
}
