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

import java.io.Serializable;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.util.TypeLiteral;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class TestGetSupertypes {

  private TestGetSupertypes() {
    super();
  }

  // @Disabled // TODO: causes infinite loops
  @Test
  final void testGetSupertypesListString() {
    final ParameterizedType listString = (ParameterizedType)new TypeLiteral<List<String>>() {}.getType();
    final Type[] supertypes = Types.getSupertypes(listString);
    boolean found = false;
    for (final Type supertype : supertypes) {
      assertNotNull(supertype);
      if (!found && Types.equals(listString, supertype)) {
        found = true;
      }
    }
    if (!found) {
      fail("Never found " + Types.toString(listString) + " in " + Types.toString(supertypes));
    }
  }

  // @Disabled // TODO: causes infinite loops
  @Test
  final void testGetSupertypesIterableCAPExtendsObject() {
    final Types.FreshTypeVariable CAP = new Types.FreshTypeVariable();
    final ParameterizedType iterableCAP = new DefaultParameterizedType(null, Iterable.class, CAP);
    final Type[] supertypes = Types.getSupertypes(iterableCAP);
  }

  // @Disabled // TODO: causes infinite loops
  @Test
  final void testGetSupertypesIterableExtendsObject() {
    final ParameterizedType type = (ParameterizedType)new TypeLiteral<Iterable<?>>() {}.getType();
    final Type[] supertypes = Types.getSupertypes(type);
  }

  // @Disabled // TODO: causes infinite loops
  @Test
  final void testGetSupertypesIterableObjectArray() {
    final GenericArrayType type = (GenericArrayType)new TypeLiteral<Iterable<Object>[]>() {}.getType();
    final Type[] supertypes = Types.getSupertypes(type);
  }

  // @Disabled // TODO: causes infinite loops
  @Test
  final void testGetSupertypesCollectionArray() {
    final Type[] supertypes = Types.getSupertypes(Collection[].class);
    assertEquals(13, supertypes.length, Types.toString(supertypes));
    final Collection<Type> dsc = Arrays.asList(supertypes);
    assertTrue(dsc.contains(Collection[].class)); // reflexive
    assertTrue(dsc.contains(Iterable[].class));
    assertTrue(dsc.contains(Object[].class));
    assertTrue(dsc.contains(Cloneable.class));
    assertTrue(dsc.contains(Object.class));
    assertTrue(dsc.contains(Serializable.class));
  }

}
