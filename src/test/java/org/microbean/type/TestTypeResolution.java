/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2020–2021 microBean™.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jboss.weld.util.collections.ImmutableHashSet;

import org.jboss.weld.util.reflection.HierarchyDiscovery;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestTypeResolution {

  private TestTypeResolution() {
    super();
  }

  @Test
  final void testToTypes() {
    final ParameterizedType concreteFactoryParameterizedType = (ParameterizedType)Types.normalize(ConcreteFactory.class);
    final ParameterizedType weldConcreteFactoryParameterizedType = (ParameterizedType)org.jboss.weld.util.Types.getCanonicalType(ConcreteFactory.class);
    assertEquals(weldConcreteFactoryParameterizedType, concreteFactoryParameterizedType);
    final TypeSet typeSet = Types.toTypes(concreteFactoryParameterizedType);
    final Set<Type> typeClosure = new HierarchyDiscovery(weldConcreteFactoryParameterizedType).getTypeClosure();
    assertEquals(typeClosure, HierarchyDiscovery.forNormalizedType(ConcreteFactory.class).getTypeClosure());
    assertEquals(typeClosure.size(), typeSet.size());
    assertTrue(typeSet.containsAll(typeClosure));
    // The Set<Type> returned by HierarchyDiscovery::getTypeClosure
    // has broken contains() behavior so we have to do it manually.
    // See
    // https://github.com/weld/core/blob/a2688d7680c9d0edf5125f670899978b5284b64b/impl/src/main/java/org/jboss/weld/util/collections/ImmutableHashSet.java#L116-L124
    for (final Type typeSetType : typeSet) {
      assertTypeClosureContains(typeClosure, typeSetType);
    }
  }

  private static final void assertTypeClosureContains(final Set<Type> typeClosure, final Type type) {
    try {
      assertTrue(typeClosure.contains(type));
      return;
    } catch (final AssertionError oops) {
      if (typeClosure.getClass() != ImmutableHashSet.class) {
        throw oops;
      }
    }
    // The Set<Type> returned by HierarchyDiscovery::getTypeClosure is
    // an ImmutableHashSet which has EXTREMELY weird contains(Object)
    // behavior, so we can't just see if
    // typeClosure.contains(typeSetType).  We need to tread carefully
    // here by explicitly comparing all the entries by hand and
    // checking equality in both directions.
    boolean result = false;
    for (final Type typeClosureType : typeClosure) {
      assertNotNull(typeClosureType);
      if (typeClosureType.equals(type)) {
        assertEquals(type, typeClosureType);
        assertFalse(result);
        result = true;
      } else {
        assertNotEquals(type, typeClosureType);
      }
    }
    assertTrue(result);
  }

  // These classes model a real-world use case that caused this bug to
  // be discovered.

  private static interface Maker<T> {

    public T make();

  }

  private static interface AltMaker<T> extends Maker<T> {

  }

  /* *** */

  private static interface Factory<T> {

    public T create();

  }

  private static abstract class AbstractFactory<T> implements Factory<T> {

  }

  /* *** */

  private static abstract class AbstractFactoryAndAltMaker<T> extends AbstractFactory<T> implements AltMaker<T> {

  }

  /* *** */

  private static interface Getter<T> {

  }

  private static interface Iterator<T> extends Getter<T> {

  }

  private static final class ConcreteIterator<T> implements Iterator<T> {

  }

  /* *** */

  private static final class ConcreteFactory<T> extends AbstractFactoryAndAltMaker<ConcreteIterator<T>> {

    @Override // Maker, AltMaker
    public ConcreteIterator<T> make() {
      return create();
    }

    @Override // Factory
    public ConcreteIterator<T> create() {
      return null;
    }

  }

}
