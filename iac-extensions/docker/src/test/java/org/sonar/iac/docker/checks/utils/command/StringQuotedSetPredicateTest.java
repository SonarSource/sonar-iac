package org.sonar.iac.docker.checks.utils.command;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringQuotedSetPredicateTest {

  @Test
  void testSingleValue() {
    StringQuotedSetPredicate predicate = new StringQuotedSetPredicate("value");
    assertThat(predicate.test("value")).isTrue();
    assertThat(predicate.test("\"value\"")).isTrue();
    assertThat(predicate.test("'value'")).isTrue();

    assertThat(predicate.test(" value")).isFalse();
    assertThat(predicate.test("value ")).isFalse();
    assertThat(predicate.test(" value ")).isFalse();
    assertThat(predicate.test("other")).isFalse();
    assertThat(predicate.test("valueother")).isFalse();
    assertThat(predicate.test("value other")).isFalse();
    assertThat(predicate.test("\"value")).isFalse();
    assertThat(predicate.test("value\"")).isFalse();
    assertThat(predicate.test("'value")).isFalse();
    assertThat(predicate.test("value'")).isFalse();
  }

  @Test
  void testMultipleValue() {
    StringQuotedSetPredicate predicate = new StringQuotedSetPredicate(Set.of("string", "value"));
    assertThat(predicate.test("string")).isTrue();
    assertThat(predicate.test("\"string\"")).isTrue();
    assertThat(predicate.test("'string'")).isTrue();

    assertThat(predicate.test("value")).isTrue();
    assertThat(predicate.test("\"value\"")).isTrue();
    assertThat(predicate.test("'value'")).isTrue();

    assertThat(predicate.test(" other ")).isFalse();
  }
}
