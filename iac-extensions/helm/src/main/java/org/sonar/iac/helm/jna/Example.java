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
package org.sonar.iac.helm.jna;

import com.sun.jna.Native;
import org.sonar.iac.helm.jna.library.Template;
import org.sonar.iac.helm.jna.mapping.ExampleData;
import org.sonar.iac.helm.jna.mapping.GoString;

public class Example {
    public static void main(String[] args) {
    var extension = System.getProperty("os.name").toLowerCase().startsWith("win") ? ".dll" : ".so";
    System.out.println("Template test:");
    Template template = Native.loadLibrary(
      Example.class.getResource("/golang-template").getPath(),
      Template.class);
    var goName = new GoString.ByValue("test");
    var goExpression = new GoString.ByValue("Will this be evaluated: {{.Name}} ?");
    var tId = template.NewHandleID(goName, goExpression);
    var templateName = template.GetLastTemplateNameByHandle(tId);
    System.out.println(templateName);
    var data = new ExampleData.ByValue();
    data.Name = "yes!";
    var result = template.Execute(tId, data);
    System.out.println(result);
  }
}
