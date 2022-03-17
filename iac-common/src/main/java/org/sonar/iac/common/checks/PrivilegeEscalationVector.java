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


import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PrivilegeEscalationVector {

  private final Set<String> permissions;

  public PrivilegeEscalationVector(String... permissions) {
    this.permissions = Set.of(permissions);
  }

  private PrivilegeEscalationVector(Set<String> permissions) {
    this.permissions = permissions;
  }

  public Set<PrivilegeEscalationVector> getWildcardPermutations() {
    Set<PrivilegeEscalationVector> permutations = new HashSet<>();

    // Create a new PermissionEscalationVector where each permission in each case is replaced by a wildcard permission
    List<String> permissionList = List.copyOf(permissions);
    for (int i = 0; i < permissionList.size(); i++) {
      Set<String> wildcardPermission = new HashSet<>();
      for (int k = 0; k < permissionList.size(); k++) {
        wildcardPermission.add(k == i ? getWildcard(permissionList.get(k)) : permissionList.get(k));
      }
      PrivilegeEscalationVector permutation = new PrivilegeEscalationVector(wildcardPermission);
      permutations.add(permutation);
    }

    // Create one PermissionEscalationVector where every permission is replaced by a wildcard
    Set<String> allWildcardPermission = permissions.stream().map(PrivilegeEscalationVector::getWildcard).collect(Collectors.toSet());
    permutations.add(new PrivilegeEscalationVector(allWildcardPermission));

    return permutations;
  }

  private static String getWildcard(String permission) {
    return permission.substring(0, permission.indexOf(':') + 1) + "*";
  }

  public boolean appliesToActionPermissions(Set<String> actionPermissions) {
    return actionPermissions.containsAll(permissions);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PrivilegeEscalationVector that = (PrivilegeEscalationVector) o;
    return Objects.equals(permissions, that.permissions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(permissions.stream().sorted().collect(Collectors.toList()));
  }

  public static Set<PrivilegeEscalationVector> getEscalationVectorsWithWildcard(Set<PrivilegeEscalationVector> vectors) {
    Set<PrivilegeEscalationVector> escalationVectors = new HashSet<>(vectors);
    vectors.forEach(vector -> escalationVectors.addAll(vector.getWildcardPermutations()));
    return escalationVectors;
  }
}
