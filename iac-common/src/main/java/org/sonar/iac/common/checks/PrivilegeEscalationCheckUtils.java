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

public class PrivilegeEscalationCheckUtils {

  private PrivilegeEscalationCheckUtils() {
    // utils class
  }

  private static final Set<PrivilegeEscalationVector> ESCALATION_VECTORS = Set.of(
    new PrivilegeEscalationVector("iam:CreatePolicyVersion"),
    new PrivilegeEscalationVector("iam:SetDefaultPolicyVersion"),
    new PrivilegeEscalationVector("iam:CreateAccessKey"),
    new PrivilegeEscalationVector("iam:CreateLoginProfile"),
    new PrivilegeEscalationVector("iam:UpdateLoginProfile"),
    new PrivilegeEscalationVector("iam:AttachUserPolicy"),
    new PrivilegeEscalationVector("iam:AttachGroupPolicy"),
    new PrivilegeEscalationVector("iam:AttachRolePolicy", "sts:AssumeRole"),
    new PrivilegeEscalationVector("iam:PutUserPolicy"),
    new PrivilegeEscalationVector("iam:PutGroupPolicy"),
    new PrivilegeEscalationVector("iam:PutRolePolicy", "sts:AssumeRole"),
    new PrivilegeEscalationVector("iam:AddUserToGroup"),
    new PrivilegeEscalationVector("iam:UpdateAssumeRolePolicy", "sts:AssumeRole"),
    new PrivilegeEscalationVector("iam:PassRole", "ec2:RunInstances"),
    new PrivilegeEscalationVector("iam:PassRole", "lambda:CreateFunction", "lambda:InvokeFunction"),
    new PrivilegeEscalationVector("iam:PassRole", "lambda:CreateFunction", "lambda:AddPermission"),
    new PrivilegeEscalationVector("iam:PassRole", "lambda:CreateFunction", "lambda:CreateEventSourceMapping"),
    new PrivilegeEscalationVector("iam:PassRole", "cloudformation:CreateStack"),
    new PrivilegeEscalationVector("iam:PassRole", "datapipeline:CreatePipeline", "datapipeline:PutPipelineDefinition"),
    new PrivilegeEscalationVector("iam:PassRole", "glue:CreateDevEndpoint"),
    new PrivilegeEscalationVector("glue:UpdateDevEndpoint"),
    new PrivilegeEscalationVector("lambda:UpdateFunctionCode")
  );

  public static boolean escalationVectorApply(Stream<String> actionPermissions) {
    Set<Permission> permissionVector = actionPermissions.map(Permission::of).collect(Collectors.toSet());
    return ESCALATION_VECTORS.stream().anyMatch(vector -> vector.isSubsetOf(permissionVector));
  }

  public static class PrivilegeEscalationVector {

    private final List<Permission.SimplePermission> permissions;

    public PrivilegeEscalationVector(String... permissions) {
      this.permissions =  Stream.of(permissions).map(Permission.SimplePermission::new).collect(Collectors.toList());
    }

    public boolean isSubsetOf(Collection<Permission> actionPermissions) {
      return permissions.stream().allMatch(p -> actionPermissions.stream().anyMatch(p::isCoveredBy));
    }
  }

  public abstract static class Permission {
    protected final String permissionName;

    protected Permission(String permissionName) {
      this.permissionName = permissionName;
    }

    public static Permission of(String permissionName) {
      return permissionName.endsWith("*") ? new WildCardPermission(permissionName) : new SimplePermission(permissionName);
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
