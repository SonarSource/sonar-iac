package org.sonar.iac.helm.jna.mapping;

import com.sun.jna.Structure;

@Structure.FieldOrder({"Name"})
public class ExampleData extends Structure {
  public static class ByValue extends ExampleData implements Structure.ByValue {
  }

  public String Name;
}
