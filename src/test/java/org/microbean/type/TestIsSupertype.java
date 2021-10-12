/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2021 microBean™.
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.List;

import jakarta.enterprise.util.TypeLiteral;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestIsSupertype {

  private TestIsSupertype() {
    super();
  }

  @Test
  final void testIsSupertypeSimpleClasses() {
    assertTrue(Types.isSupertype(String.class, String.class));
    assertTrue(Types.isSupertype(Object.class, String.class));
    assertFalse(Types.isSupertype(String.class, Object.class));
  }

  @Test
  final void testIsSupertypeRawTypes() {
    assertTrue(Types.isSupertype(List.class, List.class));
    final Type listString = new TypeLiteral<List<String>>() {}.getType();
    assertTrue(Types.isSupertype(List.class, listString));
    assertTrue(Types.isSupertype(listString, listString));
  }

  @Test
  final void testNoParameterizedTypeSubtyping() {
    final Type listString = new TypeLiteral<List<String>>() {}.getType();
    final Type listObject = new TypeLiteral<List<Object>>() {}.getType();
    assertFalse(Types.isSupertype(listObject, listString));
  }

  @Test
  final void testArrayTypes() {
    assertTrue(Types.isSupertype(Object[].class, Object[].class));
    assertTrue(Types.isSupertype(Object[].class, Integer[].class));
    assertFalse(Types.isSupertype(Integer[].class, Object[].class));
    assertTrue(Types.isSupertype(Object.class, Object[].class));
    final Type listStringArray = new TypeLiteral<List<String>[]>() {}.getType();
    assertTrue(Types.isSupertype(listStringArray, listStringArray));
    assertTrue(Types.isSupertype(Object.class, listStringArray));
  }
  
}
