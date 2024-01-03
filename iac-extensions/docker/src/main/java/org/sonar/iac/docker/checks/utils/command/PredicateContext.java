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
package org.sonar.iac.docker.checks.utils.command;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import org.sonar.iac.docker.symbols.ArgumentResolution;

import static org.sonar.iac.docker.checks.utils.command.CommandPredicate.Type.MATCH;

public class PredicateContext {

  private final Deque<ArgumentResolution> argumentStack;
  private final List<CommandPredicate> detectorPredicates;
  private Status status;
  private Deque<CommandPredicate> predicatesStack;
  private List<ArgumentResolution> argumentsToReport;
  private CommandPredicate currentPredicate;

  public PredicateContext(Deque<ArgumentResolution> argumentStack, List<CommandPredicate> detectorPredicates) {
    this.argumentStack = argumentStack;
    this.detectorPredicates = detectorPredicates;
    this.status = Status.CONTINUE;
  }

  public void startNewfullMatchOn(List<CommandPredicate> detectorPredicates) {
    this.predicatesStack = new LinkedList<>(detectorPredicates);
    this.argumentsToReport = new ArrayList<>();
  }

  public boolean is(Status... statusArray) {
    for (Status specificStatus : statusArray) {
      if (this.status.equals(specificStatus)) {
        return true;
      }
    }
    return false;
  }

  public boolean remainingPredicatesAreOptional() {
    predicatesStack.addFirst(currentPredicate);
    return predicatesStack.stream().noneMatch(predicate -> predicate.hasType(MATCH));
  }

  public boolean areNoArgumentsToHandle() {
    return argumentStack.isEmpty();
  }

  public ArgumentResolution getNextArgumentToHandle() {
    return argumentStack.peekFirst();
  }

  public ArgumentResolution getNextArgumentToHandleAndRemoveFromList() {
    return argumentStack.pollFirst();
  }

  public void argumentShouldBeMatchedAgain(ArgumentResolution resolution) {
    argumentStack.addFirst(resolution);
  }

  public boolean arePredicatesToDetectLeft() {
    return !predicatesStack.isEmpty();
  }

  public void provideNextPredicate() {
    currentPredicate = predicatesStack.pollFirst();
  }

  public void detectCurrentPredicateAgain() {
    predicatesStack.addFirst(currentPredicate);
  }

  public void matchOnCurrentPredicate() {
    currentPredicate.match(this);
  }

  public int numberOfArgumentsToReport() {
    return argumentsToReport.size();
  }

  public void addAsArgumentToReport(ArgumentResolution resolution) {
    argumentsToReport.add(resolution);
  }

  public Deque<ArgumentResolution> getArgumentStack() {
    return argumentStack;
  }

  public List<CommandPredicate> getDetectorPredicates() {
    return detectorPredicates;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public List<ArgumentResolution> getArgumentsToReport() {
    return argumentsToReport;
  }

  public CommandPredicate getCurrentPredicate() {
    return currentPredicate;
  }

  public enum Status {
    CONTINUE, ABORT, FOUND_NO_PREDICATE_MATCH
  }
}
