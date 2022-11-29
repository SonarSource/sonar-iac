package org.sonar.iac.common.checks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sonar.iac.common.api.tree.Tree;

public final class ResourceAccessPolicyVector {
  private static final JSONArray RESOURCE_ACCESS_POLICIES = loadResourceAccessPolicies();

  private static JSONArray loadResourceAccessPolicies() {
    try (InputStream input = ResourceAccessPolicyVector.class.getClassLoader().getResourceAsStream("withResources.json")) {
      if (input == null) {
        throw new IOException("No able to load ResourceAccessPolicyVector json");
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[4_096];
      for (int read = input.read(buffer); read != -1; read = input.read(buffer)) {
        out.write(buffer, 0, read);
      }

      JSONParser parser = new JSONParser();
      return  (JSONArray) parser.parse(out.toString(StandardCharsets.UTF_8));
    } catch (IOException | ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean isResourceAccessPolicy(Tree action) {
    return TextUtils.matchesValue(action, RESOURCE_ACCESS_POLICIES::contains).isTrue();
  }
}
