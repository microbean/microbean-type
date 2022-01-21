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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestGenericSupertype {

  private TestGenericSupertype() {
    super();
  }

  @Test
  final void testGenericSupertypeCanBeARegularClass() {
    assertSame(Number.class, Integer.class.getGenericSuperclass());
  }

  @Test
  final void testGenericSupertypeCanBeAParameterizedType() {
    final ParameterizedType pt = (ParameterizedType)DummyClassValue.class.getGenericSuperclass();
    assertSame(ClassValue.class, pt.getRawType());
    TypeVariable<?>[] tvs = DummyClassValue.class.getTypeParameters();
    assertEquals(1, tvs.length);
    assertEquals("X", tvs[0].getName());
    tvs = ClassValue.class.getTypeParameters();
    assertEquals(1, tvs.length);
    assertEquals("T", tvs[0].getName());
  }

  private static final class DummyClassValue<X> extends ClassValue<X> {

    @Override
    protected final X computeValue(final Class<?> c) {
      return null;
    }
    
  }
}
