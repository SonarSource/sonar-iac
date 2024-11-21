/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
  private Deque<CommandPredicate> predicatesStack;
  private List<ArgumentResolution> argumentsToReport;
  private final List<ArgumentResolution> containsArgumentsToReport;
  private CommandPredicate currentPredicate;

  public PredicateContext(Deque<ArgumentResolution> argumentStack, List<CommandPredicate> detectorPredicates) {
    this.argumentStack = argumentStack;
    this.detectorPredicates = detectorPredicates;
    this.containsArgumentsToReport = new ArrayList<>();
  }

  public void startNewFullMatch() {
    this.predicatesStack = new LinkedList<>(detectorPredicates);
    this.argumentsToReport = new ArrayList<>();
  }

  public boolean remainingPredicatesAreOptional() {
    predicatesStack.addFirst(currentPredicate);
    return predicatesStack.stream().noneMatch(predicate -> predicate.hasType(MATCH));
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

  public CommandPredicateResult matchOnCurrentPredicate() {
    return currentPredicate.match(this);
  }

  public void addAsArgumentToReport(ArgumentResolution resolution) {
    argumentsToReport.add(resolution);
  }

  public List<CommandPredicate> getDetectorPredicates() {
    return detectorPredicates;
  }

  public List<ArgumentResolution> getArgumentsToReport() {
    argumentsToReport.addAll(containsArgumentsToReport);
    return argumentsToReport.stream().distinct().toList();
  }

  public void addContainsArgumentsToReport(ArgumentResolution resolution) {
    containsArgumentsToReport.add(resolution);
  }

  public CommandPredicate getCurrentPredicate() {
    return currentPredicate;
  }

  public enum Status {
    CONTINUE, ABORT, FOUND_NO_PREDICATE_MATCH
  }
}
