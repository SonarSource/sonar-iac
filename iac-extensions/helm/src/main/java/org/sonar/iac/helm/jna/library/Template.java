package org.sonar.iac.helm.jna.library;

import com.sun.jna.Library;
import org.sonar.iac.helm.jna.mapping.ExampleData;
import org.sonar.iac.helm.jna.mapping.GoString;

public interface Template extends Library {
  long NewHandleID(GoString.ByValue name, GoString.ByValue expression);

  String GetLastTemplateNameByHandle(long id);

  void Tree(long id);

  String PrintTree(long id);

  String Execute(long templateHandle, ExampleData value);

  String ExecuteWithValues(long templateHandle, GoString.ByValue valuesFilePath);
}
