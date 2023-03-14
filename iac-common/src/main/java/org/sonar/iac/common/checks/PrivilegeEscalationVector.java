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
package org.sonar.iac.common.checks;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PrivilegeEscalationVector {

  CREATE_POLICY_VERSION("Create Policy Version", List.of("iam:CreatePolicyVersion")),
  SET_DEFAULT_POLICY_VERSION("Set Default Policy Version", List.of("iam:SetDefaultPolicyVersion")),
  CREATE_ACCESS_KEY("Create Access Key", List.of("iam:CreateAccessKey")),
  CREATE_LOGIN_PROFILE("Create Login Profile", List.of("iam:CreateLoginProfile")),
  UPDATE_LOGIN_PROFILE("Update Login Profile", List.of("iam:UpdateLoginProfile")),
  ATTACH_USER_POLICY("Attach User Policy", List.of("iam:AttachUserPolicy")),
  ATTACH_GROUP_POLICY("Attach Group Policy", List.of("iam:AttachGroupPolicy")),
  ATTACH_ROLE_POLICY("Attach Role Policy", List.of("iam:AttachRolePolicy", "sts:AssumeRole")),
  PUT_USER_POLICY("Put User Policy", List.of("iam:PutUserPolicy")),
  PUT_GROUP_POLICY("Put Group Policy", List.of("iam:PutGroupPolicy")),
  PUT_ROLE_POLICY("Put Role Policy", List.of("iam:PutRolePolicy", "sts:AssumeRole")),
  ADD_USER_TO_GROUP("Add User to Group", List.of("iam:AddUserToGroup")),
  UPDATE_ASSUME_ROLE_POLICY("Update Assume role Policy", List.of("iam:UpdateAssumeRolePolicy", "sts:AssumeRole")),
  EC2("EC2", List.of("iam:PassRole", "ec2:RunInstances")),
  LAMBDA_CREATE_AND_INVOKE("Lambda Create and Invoke", List.of("iam:PassRole", "lambda:CreateFunction", "lambda:InvokeFunction")),
  LAMBDA_CREATE_AND_ADD_PERMISSION("Lambda Create and Add Permission", List.of("iam:PassRole", "lambda:CreateFunction", "lambda:AddPermission")),
  LAMBDA_TRIGGERED_WITH_AN_EXTERNAL_EVENT("Lambda triggered with an external event", List.of("iam:PassRole", "lambda:CreateFunction", "lambda:CreateEventSourceMapping")),
  CLOUD_FORMATION("CloudFormation", List.of("iam:PassRole", "cloudformation:CreateStack")),
  DATA_PIPELINE("Data Pipeline", List.of("iam:PassRole", "datapipeline:CreatePipeline", "datapipeline:PutPipelineDefinition")),
  GLUE_DEVELOPMENT_ENDPOINT("Glue Development Endpoint", List.of("iam:PassRole", "glue:CreateDevEndpoint")),
  UPDATE_GLUE_DEV_ENDPOINT("Update Glue Dev Endpoint", List.of("glue:UpdateDevEndpoint")),
  UPDATE_LAMBDA_CODE("Update Lambda code", List.of("lambda:UpdateFunctionCode"));

  private final List<Permission.SimplePermission> permissions;
  private final String vectorName;

  public String getVectorName() {
    return vectorName;
  }

  PrivilegeEscalationVector(String vectorName, List<String> permissions) {
    this.vectorName = vectorName;
    this.permissions = permissions.stream().map(Permission.SimplePermission::new).collect(Collectors.toList());
  }

  public boolean isSubsetOf(Collection<Permission> actionPermissions) {
    return permissions.stream().allMatch(p -> actionPermissions.stream().anyMatch(p::isCoveredBy));
  }

  public static boolean isSupersetOfAnEscalationVector(Stream<String> actionPermissions) {
    return getEscalationVector(actionPermissions).isPresent();
  }

  public static Optional<PrivilegeEscalationVector> getEscalationVector(Stream<String> actionPermissions) {
    Set<Permission> permissionVector = actionPermissions.map(Permission::of).collect(Collectors.toSet());

    return Stream.of(PrivilegeEscalationVector.values())
      .filter(vector -> vector.isSubsetOf(permissionVector))
      .findFirst();
  }

  public List<Permission.SimplePermission> permissions() {
    return permissions;
  }

  public abstract static class Permission {

    protected final String permissionName;

    protected Permission(String permissionName) {
      this.permissionName = permissionName;
    }

    public static Permission of(String permissionName) {
      return permissionName.endsWith("*") ? new Permission.WildCardPermission(permissionName) : new Permission.SimplePermission(permissionName);
    }

    public static class SimplePermission extends Permission {

      protected SimplePermission(String permissionName) {
        super(permissionName);
      }

      public boolean isCoveredBy(Permission other) {
        if (other instanceof WildCardPermission) {
          return (permissionName.substring(0, permissionName.indexOf(':') + 1) + "*").equals(other.permissionName);
        }
        return this.permissionName.equals(other.permissionName);
      }
    }

    static class WildCardPermission extends Permission {

      protected WildCardPermission(String permissionName) {
        super(permissionName);
      }
    }
  }
}
