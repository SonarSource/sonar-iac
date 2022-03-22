/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PrivilegeEscalationVector {

  CREATE_POLICY_VERSION("iam:CreatePolicyVersion"),
  SET_DEFAULT_POLICY_VERSION("iam:SetDefaultPolicyVersion"),
  CREATE_ACCESS_KEY("iam:CreateAccessKey"),
  CREATE_LOGIN_PROFILE("iam:CreateLoginProfile"),
  UPDATE_LOGIN_PROFILE("iam:UpdateLoginProfile"),
  ATTACH_USER_POLICY("iam:AttachUserPolicy"),
  ATTACH_GROUP_POLICY("iam:AttachGroupPolicy"),
  ATTACH_ROLE_POLICY("iam:AttachRolePolicy", "sts:AssumeRole"),
  PUT_USER_POLICY("iam:PutUserPolicy"),
  PUT_GROUP_POLICY("iam:PutGroupPolicy"),
  PUT_ROLE_POLICY("iam:PutRolePolicy", "sts:AssumeRole"),
  ADD_USER_TO_GROUP("iam:AddUserToGroup"),
  UPDATE_ASSUME_ROLE_POLICY("iam:UpdateAssumeRolePolicy", "sts:AssumeRole"),
  EC2("iam:PassRole", "ec2:RunInstances"),
  LAMBDA_CREATE_AND_INVOKE("iam:PassRole", "lambda:CreateFunction", "lambda:InvokeFunction"),
  LAMBDA_CREATE_AND_ADD_PERMISSION("iam:PassRole", "lambda:CreateFunction", "lambda:AddPermission"),
  LAMBDA_TRIGGERED_WITH_AN_EXTERNAL_EVENT("iam:PassRole", "lambda:CreateFunction", "lambda:CreateEventSourceMapping"),
  CLOUD_FORMATION("iam:PassRole", "cloudformation:CreateStack"),
  DATA_PIPELINE("iam:PassRole", "datapipeline:CreatePipeline", "datapipeline:PutPipelineDefinition"),
  GLUE_DEVELOPMENT_ENDPOINT("iam:PassRole", "glue:CreateDevEndpoint"),
  UPDATE_GLUE_DEV_ENDPOINT("glue:UpdateDevEndpoint"),
  UPDATE_LAMBDA_CODE("lambda:UpdateFunctionCode");

  private final List<Permission.SimplePermission> permissions;

  PrivilegeEscalationVector(String... permissions) {
    this.permissions =  Stream.of(permissions).map(Permission.SimplePermission::new).collect(Collectors.toList());
  }

  public boolean isSubsetOf(Collection<Permission> actionPermissions) {
    return permissions.stream().allMatch(p -> actionPermissions.stream().anyMatch(p::isCoveredBy));
  }

  public static boolean isSupersetOfAnEscalationVector(Stream<String> actionPermissions) {
    Set<Permission> permissionVector = actionPermissions.map(Permission::of).collect(Collectors.toSet());
    return Stream.of(PrivilegeEscalationVector.values()).anyMatch(vector -> vector.isSubsetOf(permissionVector));
  }

  public abstract static class Permission {
    protected final String permissionName;

    protected Permission(String permissionName) {
      this.permissionName = permissionName;
    }

    public static Permission of(String permissionName) {
      return permissionName.endsWith("*") ? new Permission.WildCardPermission(permissionName) : new Permission.SimplePermission(permissionName);
    }

    static class SimplePermission extends Permission {

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
