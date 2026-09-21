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
package org.sonar.iac.common.predicates;

import com.eclipsesource.json.JsonHandler;
import com.eclipsesource.json.JsonParser;
import com.eclipsesource.json.ParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Configuration;
import org.sonar.iac.common.extension.SharedFileHeadReader;

/**
 * Matches Bicep-generated ARM JSON files that have not been explicitly included in the applicable source scope.
 */
public class BicepGeneratedArmJsonFilePredicate implements FilePredicate {
  private static final Logger LOG = LoggerFactory.getLogger(BicepGeneratedArmJsonFilePredicate.class);
  private static final String BICEP_GENERATOR_NAME = "bicep";
  private static final String MAIN_EXCLUSIONS_KEY = "sonar.exclusions";
  private static final String MAIN_INCLUSIONS_KEY = "sonar.inclusions";
  private static final String TEST_EXCLUSIONS_KEY = "sonar.test.exclusions";
  private static final String TEST_INCLUSIONS_KEY = "sonar.test.inclusions";

  private final FilePredicate armJsonFilePredicate;
  private final FilePredicates filePredicates;
  private final Configuration config;
  private final SharedFileHeadReader sharedFileHeadReader;

  /**
   * Creates a predicate for Bicep-generated ARM JSON files.
   *
   * @param filePredicates creates scanner-compatible file predicates
   * @param config provides the effective analysis configuration
   * @param enablePredicateDebugLogs whether failed identifier checks should be logged
   * @param sharedFileHeadReader reads bounded file heads shared with classification predicates
   * @return a predicate that matches generated ARM JSON files
   */
  public static FilePredicate create(FilePredicates filePredicates, Configuration config,
    boolean enablePredicateDebugLogs, SharedFileHeadReader sharedFileHeadReader) {
    var armJsonFilePredicate = ArmJsonFilePredicate.createRecognitionPredicate(filePredicates, config,
      enablePredicateDebugLogs, sharedFileHeadReader);
    return new BicepGeneratedArmJsonFilePredicate(armJsonFilePredicate, filePredicates, config, sharedFileHeadReader);
  }

  /**
   * Creates a predicate for generated ARM JSON files.
   *
   * @param armJsonFilePredicate recognizes ARM JSON files
   * @param filePredicates creates scanner-compatible path predicates
   * @param config provides the effective analysis configuration
   * @param sharedFileHeadReader reads bounded file heads shared with classification predicates
   */
  private BicepGeneratedArmJsonFilePredicate(FilePredicate armJsonFilePredicate, FilePredicates filePredicates,
    Configuration config, SharedFileHeadReader sharedFileHeadReader) {
    this.armJsonFilePredicate = armJsonFilePredicate;
    this.filePredicates = filePredicates;
    this.config = config;
    this.sharedFileHeadReader = sharedFileHeadReader;
  }

  @Override
  public boolean apply(InputFile inputFile) {
    return armJsonFilePredicate.apply(inputFile)
      && hasBicepGeneratorMarker(inputFile)
      && !isExplicitlyIncluded(inputFile);
  }

  /**
   * Checks whether the bounded JSON head contains Bicep's top-level generator marker.
   *
   * @param inputFile the ARM JSON file to inspect
   * @return whether the top-level marker identifies Bicep as the generator
   */
  private boolean hasBicepGeneratorMarker(InputFile inputFile) {
    try {
      var json = sharedFileHeadReader.readText(inputFile);
      if (json.startsWith("\uFEFF")) {
        json = json.substring(1);
      }
      new JsonParser(new BicepGeneratorMarkerHandler()).parse(json);
      return false;
    } catch (BicepGeneratorMarkerFound e) {
      return true;
    } catch (IOException e) {
      LOG.warn("Unable to read file: {}.", inputFile);
      LOG.warn(e.getMessage());
      return false;
    } catch (ParseException e) {
      return false;
    }
  }

  /**
   * Checks whether a file positively matches a non-blank applicable inclusion pattern.
   *
   * @param inputFile the file whose source scope should be checked
   * @return whether the applicable inclusion setting explicitly selects the file
   */
  private boolean isExplicitlyIncluded(InputFile inputFile) {
    return !matchesConfiguredPathPatterns(inputFile, exclusionKey(inputFile))
      && matchesConfiguredPathPatterns(inputFile, inclusionKey(inputFile));
  }

  /**
   * Checks whether a file matches a non-blank configured path-pattern list.
   *
   * @param inputFile the file to match against configured patterns
   * @param configurationKey the configuration key containing the patterns
   * @return whether a non-empty configured pattern list matches the file
   */
  private boolean matchesConfiguredPathPatterns(InputFile inputFile, String configurationKey) {
    var patterns = Arrays.stream(config.getStringArray(configurationKey))
      .filter(pattern -> !pattern.isBlank())
      .toArray(String[]::new);
    return patterns.length > 0 && filePredicates.matchesPathPatterns(patterns).apply(inputFile);
  }

  /**
   * Gets the inclusion setting that applies to a file type.
   *
   * @param inputFile the file whose type determines the setting
   * @return the applicable source- or test-inclusion key
   */
  private static String inclusionKey(InputFile inputFile) {
    return inputFile.type() == InputFile.Type.TEST ? TEST_INCLUSIONS_KEY : MAIN_INCLUSIONS_KEY;
  }

  /**
   * Gets the exclusion setting that applies to a file type.
   *
   * @param inputFile the file whose type determines the setting
   * @return the applicable source- or test-exclusion key
   */
  private static String exclusionKey(InputFile inputFile) {
    return inputFile.type() == InputFile.Type.TEST ? TEST_EXCLUSIONS_KEY : MAIN_EXCLUSIONS_KEY;
  }

  private static final class BicepGeneratorMarkerHandler extends JsonHandler<List<String>, ObjectContext> {
    private static final List<String> GENERATOR_PATH = List.of("metadata", "_generator");

    private List<String> pendingObjectPath = List.of();
    private boolean bicepNameValue;

    @Override
    public ObjectContext startObject() {
      var object = new ObjectContext(pendingObjectPath);
      pendingObjectPath = List.of();
      bicepNameValue = false;
      return object;
    }

    @Override
    public List<String> startArray() {
      var arrayPath = appendPath(pendingObjectPath, "[]");
      pendingObjectPath = arrayPath;
      bicepNameValue = false;
      return arrayPath;
    }

    @Override
    public void startArrayValue(List<String> arrayPath) {
      pendingObjectPath = arrayPath;
      bicepNameValue = false;
    }

    @Override
    public void startObjectValue(ObjectContext object, String name) {
      pendingObjectPath = appendPath(object.path(), name);
      bicepNameValue = GENERATOR_PATH.equals(object.path()) && "name".equals(name);
    }

    @Override
    public void endObjectValue(ObjectContext object, String name) {
      pendingObjectPath = List.of();
      bicepNameValue = false;
    }

    @Override
    public void startNull() {
      bicepNameValue = false;
    }

    @Override
    public void startBoolean() {
      bicepNameValue = false;
    }

    @Override
    public void startNumber() {
      bicepNameValue = false;
    }

    @Override
    public void endString(String value) {
      if (bicepNameValue && BICEP_GENERATOR_NAME.equals(value)) {
        throw new BicepGeneratorMarkerFound();
      }
      bicepNameValue = false;
    }

    /**
     * Appends a property name to an object path.
     *
     * @param path the parent object's property path
     * @param propertyName the direct child property name
     * @return the child property's path
     */
    private static List<String> appendPath(List<String> path, String propertyName) {
      var result = new ArrayList<>(path);
      result.add(propertyName);
      return result;
    }
  }

  private record ObjectContext(List<String> path) {
  }

  private static final class BicepGeneratorMarkerFound extends RuntimeException {
  }
}
