/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2022 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.type;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.microbean.type.Type.CovariantSemantics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestTypeEquals {

  private TestTypeEquals() {
    super();
  }

  @Test
  final void testEqualsBasics() {
    Type<?> t1 = JavaType.of(String.class);
    Type<?> t2 = JavaType.of(String.class);
    assertTrue(Type.equals(t1, t2));
    t1 = JavaType.of(Integer.class);
    assertFalse(Type.equals(t1, t2));
  }

  @Test
  final void testCustomSupertyped() {
    Type<?> t1 = JavaType.ofExactly(true, List.of(Number.class, new DefaultParameterizedType(null, Comparable.class, Integer.class)));
    Type<?> t2 = JavaType.of(Number.class);
    assertFalse(Type.equals(t1, t2));
    final List<?> t1Supertypes = t1.supertypes();
    assertEquals(2, t1Supertypes.size());
    final JavaType numberClass = (JavaType)t1Supertypes.get(0);
    assertEquals(Number.class, numberClass.object());
    final JavaType comparableIntegerType = (JavaType)t1Supertypes.get(1);
    assertEquals(new DefaultParameterizedType(null, Comparable.class, Integer.class), comparableIntegerType.object());
    t2 = JavaType.ofExactly(true, List.of(Number.class, new DefaultParameterizedType(null, Comparable.class, Integer.class)));
    assertNotSame(t1, t2);
    assertTrue(Type.equals(t1, t2));
    t1 = JavaType.ofExactly(true, Number.class);
    t2 = JavaType.ofExactly(true, Number.class);
    assertNotSame(t1, t2);
    assertTrue(Type.equals(t1, t2));
  }

  @Test
  final void testCustomSupertypedAssignability() {
    Type<?> t1 = JavaType.of(Number.class);
    Type<?> t2 = JavaType.ofExactly(true, List.of(Number.class, new DefaultParameterizedType(null, Comparable.class, Integer.class)));
    assertTrue(CovariantSemantics.INSTANCE.assignable(t1, t2));
    t1 = JavaType.of(Object.class);
    assertFalse(CovariantSemantics.INSTANCE.assignable(t1, t2));
  }

}
