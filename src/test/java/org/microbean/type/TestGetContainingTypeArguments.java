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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.util.TypeLiteral;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestGetContainingTypeArguments {

  private TestGetContainingTypeArguments() {
    super();
  }

  @Test
  final void testGetContainingTypeArgumentsString() {
    // These are values for type arguments that can "contain" String.class.
    final Type[] containingTypeArguments = Types.getContainingTypeArguments(String.class);
    final Collection<Type> c = Arrays.asList(containingTypeArguments);
    assertEquals(7, containingTypeArguments.length, "" + c); // TODO: this will fail and need to change when we go to JDK 12+ thanks to Constable, ConstantDesc, etc.
    assertTrue(c.contains(String.class));
    assertTrue(c.contains(new UpperBoundedWildcardType(String.class)));
    assertTrue(c.contains(new UpperBoundedWildcardType(Serializable.class)));
    assertTrue(c.contains(new UpperBoundedWildcardType(Comparable.class)));
    assertTrue(c.contains(new UpperBoundedWildcardType(CharSequence.class)));
    assertTrue(c.contains(new LowerBoundedWildcardType(String.class)));
    assertTrue(c.contains(UnboundedWildcardType.INSTANCE));
  }

  @Disabled // TODO: causes infinite loops
  @Test
  final void testGetContainingTypeArgumentsListString() {
    final ParameterizedType type = (ParameterizedType)new TypeLiteral<List<String>>() {}.getType();
    final Type[] containingTypeArguments = Types.getContainingTypeArguments(type);
    final Collection<Type> c = Arrays.asList(containingTypeArguments);
    /*
    for (final Type ct : c) {
      System.out.println("  " + Types.toString(ct));
    }
    */
  }

  @Test
  final void testGetContainingTypeArgumentsUnboundedWildcard() {
    final WildcardType type = UnboundedWildcardType.INSTANCE;
    final Type[] containingTypeArguments = Types.getContainingTypeArguments(type);
    assertEquals(1, containingTypeArguments.length);
    assertEquals(UnboundedWildcardType.INSTANCE, type);
  }

  @Test
  final void testGetContainingTypeArgumentsExtendsObject() {
    final WildcardType type = new UpperBoundedWildcardType(Object.class);
    assertNotSame(UnboundedWildcardType.INSTANCE, type);
    final Type[] containingTypeArguments = Types.getContainingTypeArguments(type);
    assertEquals(1, containingTypeArguments.length);
    assertEquals(UnboundedWildcardType.INSTANCE, type);
  }

  @Test
  final void testGetContainingTypeArgumentsObject() {
    final Class<?> type = Object.class;
    final Type[] containingTypeArguments = Types.getContainingTypeArguments(type);
    final Collection<? extends Type> c = Arrays.asList(containingTypeArguments);
    assertEquals(3, containingTypeArguments.length, Types.toString(c));
    assertTrue(c.contains(Object.class));
    assertTrue(c.contains(UnboundedWildcardType.INSTANCE));
    assertTrue(c.contains(new LowerBoundedWildcardType(Object.class)));
  }


}
