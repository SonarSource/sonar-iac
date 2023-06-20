package org.sonar.iac.common.api.checks;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.checks.CommonTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;


class SecondaryLocationTest {

  @Test
  void shouldCreateSecondaryInstance() {
    SecondaryLocation location = SecondaryLocation.secondary(2, 5, 8, 12, "message");

    assertThat(location.message).isEqualTo("message");
    assertThat(location.textRange).hasRange(2,5,8,12);
  }

  @Test
  void shouldCreateOfInstance() {
    HasTextRange tree = new CommonTestUtils.TestTextTree("value", List.of());
    SecondaryLocation location = SecondaryLocation.of(tree, "message");

    assertThat(location.message).isEqualTo("message");
    assertThat(location.textRange).hasRange(1,0,1,5);
  }

  @Test
  void shouldTestEquals() {
    SecondaryLocation location1 = SecondaryLocation.secondary(2, 5, 8, 12, "message");
    SecondaryLocation location2 = SecondaryLocation.secondary(2, 5, 8, 12, "message");
    SecondaryLocation location3 = SecondaryLocation.secondary(2, 5, 8, 12, "abc");
    SecondaryLocation location4 = SecondaryLocation.secondary(0, 1, 2, 3, "message");

    assertThat(location1.equals(location1)).isTrue();
    assertThat(location1.equals(location2)).isTrue();
    assertThat(location1.equals(new Object())).isFalse();
    assertThat(location1.equals(null)).isFalse();
    assertThat(location1.equals(location3)).isFalse();
    assertThat(location1.equals(location4)).isFalse();
  }

  @Test
  void shouldTestHashCode() {
    SecondaryLocation location1 = SecondaryLocation.secondary(2, 5, 8, 12, "message");
    SecondaryLocation location2 = SecondaryLocation.secondary(2, 5, 8, 12, "message");

    assertThat(location1).hasSameHashCodeAs(location2.hashCode());
  }
}
