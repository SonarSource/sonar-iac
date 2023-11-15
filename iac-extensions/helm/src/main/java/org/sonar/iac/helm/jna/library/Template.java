package org.sonar.iac.helm.jna.library;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.sonar.iac.helm.jna.mapping.ExampleData;
import org.sonar.iac.helm.jna.mapping.GoString;

public interface Template extends Library {
  long NewHandleID(GoString.ByValue name, GoString.ByValue expression);

  String GetLastTemplateNameByHandle(long id);

  void Tree(long id);

  SerializeToProtobufBytes_return.ByValue SerializeToProtobufBytes(long id);

  // void SerializeToFile(long id);

  String PrintTree(long id);

  String Execute(long templateHandle, ExampleData value);

  String ExecuteWithValues(long templateHandle, GoString.ByValue valuesFilePath);

  @Structure.FieldOrder({"r0", "r1"})
  public class SerializeToProtobufBytes_return extends Structure {
    public static class ByValue extends SerializeToProtobufBytes_return implements Structure.ByValue {
    }

    public Pointer r0;
    public int r1;

    public byte[] getByteArray() {
      return r0.getByteArray(0, r1);
    }
  }
}
