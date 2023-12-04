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
package org.sonar.iac.helm.jna.library;

import com.google.protobuf.InvalidProtocolBufferException;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.sonar.iac.helm.jna.mapping.GoString;
import org.sonarsource.iac.helm.TemplateEvaluationResult;

/**
 * This interface is used to call the native library.
 * Methods listed here should match the ones defined in the Go library.
 * Corresponding C signatures can be found in a file 'sonar-helm-for-iac-*.h' generated during Go build.
 */
public interface IacHelmLibrary extends Library {
  EvaluateTemplate_return.ByValue evaluateTemplate(GoString.ByValue path, GoString.ByValue content, GoString.ByValue valuesFileContent);

  default TemplateEvaluationResult evaluateTemplate(String path, String content, String valuesFileContent) {
    var rawEvaluationResult = evaluateTemplate(
      new GoString.ByValue(path), new GoString.ByValue(content), new GoString.ByValue(valuesFileContent))
        .getByteArray();

    if (rawEvaluationResult.length == 0) {
      throw new IllegalStateException("Empty evaluation result (serialization failed?)");
    }

    try {
      var evaluationResult = TemplateEvaluationResult.parseFrom(rawEvaluationResult);
      if (!evaluationResult.getError().isEmpty()) {
        throw new IllegalStateException("[go] " + evaluationResult.getError());
      }
      return evaluationResult;
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalStateException("Deserialization error", e);
    }
  }

  @Structure.FieldOrder({"r0", "r1"})
  class EvaluateTemplate_return extends Structure {
    public static class ByValue extends EvaluateTemplate_return implements Structure.ByValue {
    }

    public Pointer r0;
    public int r1;

    public byte[] getByteArray() {
      return r0.getByteArray(0, r1);
    }
  }
}
