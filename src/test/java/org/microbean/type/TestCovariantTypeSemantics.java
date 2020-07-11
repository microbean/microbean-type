/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2020 microBean™.
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
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java.util.concurrent.FutureTask;

import java.util.function.Function;
import java.util.function.Predicate;

import jakarta.enterprise.context.spi.Contextual;

import jakarta.enterprise.util.TypeLiteral;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.microbean.development.annotation.Issue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestIsAssignable {

  private TypeSemantics invariantTypeSemantics;

  private CovariantTypeSemantics covariantTypeSemantics;

  private TestIsAssignable() {
    super();
  }

  @BeforeEach
  private final void setupTypeSemantics() {
    this.covariantTypeSemantics = new CovariantTypeSemantics(true);
    this.invariantTypeSemantics = this.covariantTypeSemantics.getInvariantTypeSemantics();        
  }

  @Test
  final <T, U extends TypeSet> void testLegalBeanTypeScenario2() {
    final Type receiverType = new TypeLiteral<Function<? super Contextual<?>, ? extends TypeSet>>() {}.getType();
    final Type payloadType = new TypeLiteral<Function<Contextual<T>, U>>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));
  }

  @Test
  final void testParameterizedReceiverTypeRawPayloadType() {
    final Type receiverType = new TypeLiteral<Predicate<Contextual<?>>>() {}.getType();
    final Type payloadType = Predicate.class;
    assertTrue(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));
  }

  @Test
  final void testRawReceiverTypeParameterizedPayloadType() {
    final Type receiverType = Predicate.class;
    final Type payloadType = new TypeLiteral<Predicate<Contextual<?>>>() {}.getType();
    assertTrue(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));
  }

  @Test
  final void testLowerBoundedWildcardCase() {
    final Type receiverType = new TypeLiteral<Predicate<? super Contextual<?>>>() {}.getType();
    final Type payloadType = new TypeLiteral<Predicate<Contextual<?>>>() {}.getType();
    assertTrue(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));
  }

  @Test
  final void testResolvedWildcardCase() {
    final Type receiverType = new TypeLiteral<Predicate<Object>>() {}.getType();
    final Type payloadType = new TypeLiteral<Predicate<Contextual<Object>>>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));
  }

  @Test
  final <T extends String> void testStringIsAssignableFromTypeVariableThatExtendsString() {
    final Type receiverType = String.class;
    final Type payloadType = new TypeLiteral<T>() {}.getType();
    assertTrue(payloadType instanceof TypeVariable);
    assertEquals(String.class, ((TypeVariable<?>)payloadType).getBounds()[0]);
  }

  @Test
  final void testArrayImpliedInterfaces() {
    // See https://issues.redhat.com/browse/WELD-2614
    assertTrue(Cloneable.class.isAssignableFrom(Integer[].class));
    assertTrue(Serializable.class.isAssignableFrom(Integer[].class));
    final Collection<Class<?>> interfaces = Arrays.asList(Integer[].class.getInterfaces());
    assertTrue(interfaces.contains(Cloneable.class));
    assertTrue(interfaces.contains(Serializable.class));
  }


  /*
   * COVARIANT semantics (for the most part).
   */


  @Test
  final void testRawTypeAssignableFromRawType() {
    assertTrue(this.covariantTypeSemantics.isAssignable(Number.class, Integer.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(Number.class, Number.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(int.class, int.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(Object.class, int.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(Integer.class, Number.class));
  }

  @Test
  final void testRawArrayAssignableFromRawArray() {
    final Type numbers = new Number[0].getClass();
    final Type numbersOfNumbers = new Number[0][].getClass();
    final Type integers = new Integer[0].getClass();
    final Type integersOfIntegers = new Integer[0][].getClass();
    final Type ints = new int[0].getClass();
    assertTrue(this.covariantTypeSemantics.isAssignable(Object.class, numbers));
    assertTrue(this.covariantTypeSemantics.isAssignable(Object.class, integers));
    assertTrue(this.covariantTypeSemantics.isAssignable(Object.class, ints));
    assertTrue(this.covariantTypeSemantics.isAssignable(numbers, numbers));
    assertTrue(this.covariantTypeSemantics.isAssignable(numbers, integers));
    assertFalse(this.covariantTypeSemantics.isAssignable(numbers, ints));
    assertTrue(this.covariantTypeSemantics.isAssignable(integers, integers));
    assertFalse(this.covariantTypeSemantics.isAssignable(integers, ints));
    assertFalse(this.covariantTypeSemantics.isAssignable(numbers, Object.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(integers, Object.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(ints, Object.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(integers, numbers));
    assertFalse(this.covariantTypeSemantics.isAssignable(ints, numbers));
    assertTrue(this.covariantTypeSemantics.isAssignable(numbersOfNumbers, numbersOfNumbers));
    assertTrue(this.covariantTypeSemantics.isAssignable(numbersOfNumbers, new Number[0][].getClass()));
    assertTrue(this.covariantTypeSemantics.isAssignable(numbersOfNumbers, integersOfIntegers));
    assertFalse(this.covariantTypeSemantics.isAssignable(integersOfIntegers, numbersOfNumbers));
  }

  @Test
  final void testRawTypeAssignableFromParameterizedType() {
    assertTrue(this.covariantTypeSemantics.isAssignable(Map.class,
                                                        new DefaultParameterizedType(null,
                                                                                     Map.class,
                                                                                     String.class,
                                                                                     Integer.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(Map.class,
                                                        new DefaultParameterizedType(null,
                                                                                     Map.class,
                                                                                     Object.class,
                                                                                     Object.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(Map.class,
                                                        new DefaultParameterizedType(null,
                                                                                     HashMap.class,
                                                                                     Object.class,
                                                                                     Object.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(Map.class,
                                                         new DefaultParameterizedType(null,
                                                                                      List.class,
                                                                                      Object.class)));
  }

  @Test
  final <A, B extends Number, C extends Runnable & CharSequence> void testRawTypeAssignableFromTypeVariable() {
    final Type a = new TypeLiteral<A>() {}.getType();
    final Type b = new TypeLiteral<B>() {}.getType();
    final Type c = new TypeLiteral<C>() {}.getType();
    assertTrue(this.covariantTypeSemantics.isAssignable(Object.class, a));
    assertFalse(this.covariantTypeSemantics.isAssignable(Number.class, a));
    assertFalse(this.covariantTypeSemantics.isAssignable(Runnable.class, a));
    assertTrue(this.covariantTypeSemantics.isAssignable(Object.class, b));
    assertTrue(this.covariantTypeSemantics.isAssignable(Number.class, b));
    assertFalse(this.covariantTypeSemantics.isAssignable(Integer.class, b));
    assertFalse(this.covariantTypeSemantics.isAssignable(Runnable.class, b));
    assertTrue(this.covariantTypeSemantics.isAssignable(Object.class, c));
    assertTrue(this.covariantTypeSemantics.isAssignable(Runnable.class, c));
    assertTrue(this.covariantTypeSemantics.isAssignable(CharSequence.class, c));
    assertFalse(this.covariantTypeSemantics.isAssignable(String.class, c));
    assertFalse(this.covariantTypeSemantics.isAssignable(Integer.class, c));
  }

  @Test
  final void testRawTypeAssignableFromWildcardType() {
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Collection.class, Integer.class),
                                                         new DefaultParameterizedType(null,
                                                                                      Collection.class,
                                                                                      UnboundedWildcardType.INSTANCE)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Collection.class, Integer.class),
                                                         new DefaultParameterizedType(null,
                                                                                      Collection.class,
                                                                                      new UpperBoundedWildcardType(Number.class))));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Collection.class, Integer.class),
                                                         new DefaultParameterizedType(null,
                                                                                      Collection.class,
                                                                                      new UpperBoundedWildcardType(Integer.class))));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Collection.class, Integer.class),
                                                         new DefaultParameterizedType(null,
                                                                                      Collection.class,
                                                                                      new LowerBoundedWildcardType(Integer.class))));
  }

  @Test
  final void testRawTypeAssignableFromGenericArrayType() {
    @SuppressWarnings("rawtypes")
    final Class<?> rawListArrayClass = new List[0].getClass();
    @SuppressWarnings("rawtypes")
    final Class<?> rawArrayListArrayClass = new ArrayList[0].getClass();
    assertTrue(this.covariantTypeSemantics.isAssignable(rawListArrayClass,
                                                        new DefaultGenericArrayType(List.class, Object.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(rawListArrayClass,
                                                        new DefaultGenericArrayType(List.class, Integer.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(rawListArrayClass,
                                                        new DefaultGenericArrayType(ArrayList.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(rawArrayListArrayClass,
                                                         new DefaultGenericArrayType(List.class, Integer.class)));
  }

  @Test
  final void testParameterizedTypeAssignableFromRawType() {
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Map.class, Object.class, Object.class),
                                                        Map.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Map.class, Runnable.class, Exception.class),
                                                        Map.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Map.class, Object.class, Object.class),
                                                        HashMap.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Map.class, Runnable.class, Exception.class),
                                                        HashMap.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, String.class),
                                                         Collection.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Comparable.class, Integer.class),
                                                         Double.class));
  }

  @Test
  final void testParameterizedTypeAssignableFromParameterizedType() {
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Map.class, String.class, Integer.class),
                                                        new DefaultParameterizedType(null, Map.class, String.class, Integer.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Runnable.class),
                                                        new DefaultParameterizedType(null, List.class, Runnable.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Object.class),
                                                        new DefaultParameterizedType(null, List.class, Object.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Map.class, String.class, Integer.class),
                                                        new DefaultParameterizedType(null, HashMap.class, String.class, Integer.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Runnable.class),
                                                        new DefaultParameterizedType(null, ArrayList.class, Runnable.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Object.class),
                                                        new DefaultParameterizedType(null, ArrayList.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Object.class),
                                                         new DefaultParameterizedType(null, List.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Object.class),
                                                         new DefaultParameterizedType(null, List.class, Runnable.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Number.class),
                                                         new DefaultParameterizedType(null, List.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Number.class),
                                                         new DefaultParameterizedType(null, List.class, Runnable.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Integer.class),
                                                         new DefaultParameterizedType(null, List.class, Number.class)));
  }

  @Test
  final
    <A extends Collection<Number>,
     B extends List<Runnable> & Comparable<CharSequence>,
     C extends B>
    void testParameterizedTypeAssignableFromTypeVariable() {
    final Type a = new TypeLiteral<A>() {}.getType();
    final Type b = new TypeLiteral<B>() {}.getType();
    final Type c = new TypeLiteral<C>() {}.getType();
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Collection.class, Number.class), a));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Iterable.class, Number.class), a));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Number.class), a));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Iterable.class, Object.class), a));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Iterable.class, Integer.class), a));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Iterable.class, Runnable.class), b));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Collection.class, Runnable.class), b));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Runnable.class), b));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Comparable.class, CharSequence.class), b));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, ArrayList.class, Runnable.class), b));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Object.class), b));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, FutureTask.class), b));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Comparable.class, String.class), b));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Comparable.class, Object.class), b));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Iterable.class, Runnable.class), c));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Collection.class, Runnable.class), c));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Runnable.class), c));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Comparable.class, CharSequence.class), c));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, ArrayList.class, Runnable.class), c));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Object.class), c));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, FutureTask.class), c));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Comparable.class, String.class), c));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, Comparable.class, Object.class), c));
  }

  @Test
  final void testParameterizedTypeAssignableFromParameterizedWildcardType() {
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null,
                                                                                      Collection.class,
                                                                                      new DefaultParameterizedType(null,
                                                                                                                   Collection.class,
                                                                                                                   Integer.class)),
                                                         new DefaultParameterizedType(null,
                                                                                      Collection.class,
                                                                                      UnboundedWildcardType.INSTANCE)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null,
                                                                                      Collection.class,
                                                                                      new DefaultParameterizedType(null,
                                                                                                                   Collection.class,
                                                                                                                   Integer.class)),
                                                         new DefaultParameterizedType(null,
                                                                                      Collection.class,
                                                                                      new UpperBoundedWildcardType(Iterable.class))));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null,
                                                                                      Collection.class,
                                                                                      new DefaultParameterizedType(null,
                                                                                                                   Collection.class,
                                                                                                                   Integer.class)),
                                                         new DefaultParameterizedType(null,
                                                                                      Collection.class,
                                                                                      new UpperBoundedWildcardType(Collection.class))));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null,
                                                                                      Collection.class,
                                                                                      new DefaultParameterizedType(null,
                                                                                                                   Collection.class,
                                                                                                                   Integer.class)),
                                                         new DefaultParameterizedType(null,
                                                                                      Collection.class,
                                                                                      new LowerBoundedWildcardType(new DefaultParameterizedType(null,
                                                                                                                                                Collection.class,
                                                                                                                                                Integer.class)))));
  }

  @Test
  final void testParameterizedTypeAssignableFromGenericArrayType() {
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Integer.class),
                                                         new DefaultGenericArrayType(List.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultParameterizedType(null, List.class, Integer.class),
                                                         new DefaultGenericArrayType(ArrayList.class, Integer.class)));
  }

  @Test
  final <T, S extends Number> void testTypeVariableAssignableFromRawType() {
    final Type t = new TypeLiteral<T>() {}.getType();
    final Type s = new TypeLiteral<S>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(t, Object.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(t, List.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(s, Object.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(s, Number.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(s, Long.class));
  }

  @Test
  final <T, S extends List<Number>> void testTypeVariableAssignableFromParameterizedType() {
    final Type t = new TypeLiteral<T>() {}.getType();
    final Type s = new TypeLiteral<S>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(t,
                                                         new DefaultParameterizedType(null, List.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(t,
                                                         new DefaultParameterizedType(null, List.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(s,
                                                         new DefaultParameterizedType(null, List.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(s,
                                                         new DefaultParameterizedType(null, List.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(s,
                                                         new DefaultParameterizedType(null, List.class, Long.class)));
  }

  @Test
  final <A, B, C extends Number, D extends Integer> void testTypeVariableAssignableFromTypeVariable() {
    final int count = 5;
    final Type[] typeVariables = new Type[count];
    typeVariables[0] = new TypeLiteral<A>() {}.getType();
    typeVariables[1] = new TypeLiteral<B>() {}.getType();
    typeVariables[2] = new TypeLiteral<C>() {}.getType();
    typeVariables[3] = new TypeLiteral<D>() {}.getType();
    typeVariables[4] = new TypeLiteral<D[]>() {}.getType();
    for (int i = 0; i < count; i++) {
      for (int j = 0; j < count; j++) {
        if (i == j) {
          assertTrue(this.covariantTypeSemantics.isAssignable(typeVariables[i], typeVariables[j]));
        } else {
          assertFalse(this.covariantTypeSemantics.isAssignable(typeVariables[i], typeVariables[j]));
        }
      }
    }
  }

  @Test
  final <A, B extends A, C extends A, D extends C, E extends D> void testTypeVariableAssignableFromTypeVariable2() {
    final int count = 5;
    final Type[] typeVariables = new Type[count];
    typeVariables[0] = new TypeLiteral<A>() {}.getType();
    typeVariables[1] = new TypeLiteral<B>() {}.getType();
    typeVariables[2] = new TypeLiteral<C>() {}.getType();
    typeVariables[3] = new TypeLiteral<D>() {}.getType();
    typeVariables[4] = new TypeLiteral<E>() {}.getType();

    for (int i = 0; i < count; i++) {
      for (int j = 0; j < count; j++) {
        if (i == j || i == 0 || (i < j && i != 1)) {
          assertTrue(this.covariantTypeSemantics.isAssignable(typeVariables[i], typeVariables[j]));
        } else {
          assertFalse(this.covariantTypeSemantics.isAssignable(typeVariables[i], typeVariables[j]));
        }
      }
    }
  }

  @Test
  final <A, B extends Number> void testTypeVariableAssignableFromWildcard() {
    final Type a = new TypeLiteral<A>() {}.getType();
    final Type b = new TypeLiteral<B>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(a, UnboundedWildcardType.INSTANCE));
    assertFalse(this.covariantTypeSemantics.isAssignable(a, new UpperBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(a, new LowerBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(b, UnboundedWildcardType.INSTANCE));
    assertFalse(this.covariantTypeSemantics.isAssignable(b, new UpperBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(b, new LowerBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(b, new UpperBoundedWildcardType(Integer.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(b, new LowerBoundedWildcardType(Integer.class)));
  }

  @Test
  final <A, B extends List<Integer>> void testTypeVariableAssignableFromGenericArrayType() {
    final Type a = new TypeLiteral<A>() {}.getType();
    final Type b = new TypeLiteral<B>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(a, new DefaultGenericArrayType(List.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(a, new DefaultGenericArrayType(List.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(a, new DefaultGenericArrayType(List.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(b, new DefaultGenericArrayType(List.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(b, new DefaultGenericArrayType(List.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(b, new DefaultGenericArrayType(List.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(b, new DefaultGenericArrayType(ArrayList.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(b, new DefaultGenericArrayType(ArrayList.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(b, new DefaultGenericArrayType(ArrayList.class, Integer.class)));
  }

  @Test
  final void testWildcardAssignableFromRawType() {
    assertTrue(this.covariantTypeSemantics.isAssignable(UnboundedWildcardType.INSTANCE, Number.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Number.class), Number.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Integer.class), Number.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Number.class), Number.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Number.class), Object.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Number.class), Integer.class));
  }

  @Test
  final <A, B extends Number, C extends B, D extends Number & Serializable> void testWildcardWithTypeVariableAssignableFromRawType() {
    final Type a = new TypeLiteral<A>() {}.getType();
    final Type b = new TypeLiteral<B>() {}.getType();
    final Type c = new TypeLiteral<C>() {}.getType();
    final Type d = new TypeLiteral<D>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(a), Object.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(b), Object.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(b), Number.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(b), Integer.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(c), Object.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(c), Number.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(c), Integer.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(d), Number.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(d), Serializable.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(a), Object.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(a), Number.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(b), Object.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(b), Number.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(b), Integer.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(c), Object.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(c), Number.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(c), Integer.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(d), Object.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(d), Number.class));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(d), Serializable.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(d), Integer.class));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(d), Runnable.class));
  }

  @Test
  final void testWildcardAssignableFromParameterizedType() {
    assertTrue(this.covariantTypeSemantics.isAssignable(UnboundedWildcardType.INSTANCE,
                                  new DefaultParameterizedType(null, Collection.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                        new DefaultParameterizedType(null, Collection.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                        new DefaultParameterizedType(null, List.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                        new DefaultParameterizedType(null, ArrayList.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                         new DefaultParameterizedType(null, Collection.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                         new DefaultParameterizedType(null, Collection.class, Object.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                        new DefaultParameterizedType(null, Collection.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                        new DefaultParameterizedType(null, Iterable.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                         new DefaultParameterizedType(null, List.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                         new DefaultParameterizedType(null, ArrayList.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                         new DefaultParameterizedType(null, Collection.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                         new DefaultParameterizedType(null, Collection.class, Object.class)));

  }

  @Test
  final <A, B extends Number, C extends Runnable & Appendable> void testWildcardAssignableFromTypeVariable() {
    final Type a = new TypeLiteral<A>() {}.getType();
    final Type b = new TypeLiteral<B>() {}.getType();
    final Type c = new TypeLiteral<C>() {}.getType();
    assertTrue(this.covariantTypeSemantics.isAssignable(UnboundedWildcardType.INSTANCE, a));
    assertTrue(this.covariantTypeSemantics.isAssignable(UnboundedWildcardType.INSTANCE, b));
    assertTrue(this.covariantTypeSemantics.isAssignable(UnboundedWildcardType.INSTANCE, c));
    assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Number.class), b));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Integer.class), b));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Number.class), b));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Integer.class), b));
    assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Runnable.class), c));
    assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Appendable.class), c));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Runnable.class), c));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Appendable.class), c));
  }

  @Test
  final
    <A,
     B extends A,
     C extends A,
     D extends C,
     E extends D>
    void testWildcardWithTypeVariableAssignableFromTypeVariable() {
    final int count = 5;
    final Type[] typeVariables = new Type[count];
    typeVariables[0] = new TypeLiteral<A>() {}.getType();
    typeVariables[1] = new TypeLiteral<B>() {}.getType();
    typeVariables[2] = new TypeLiteral<C>() {}.getType();
    typeVariables[3] = new TypeLiteral<D>() {}.getType();
    typeVariables[4] = new TypeLiteral<E>() {}.getType();
    for (int i = 0; i < count; i++) {
      for (int j = 0; j < count; j++) {
        if (i == j || i == 0 || (i < j && i != 1)) {
          assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(typeVariables[i]), typeVariables[j]));
          assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(typeVariables[j]), typeVariables[i]));
        } else {
          assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(typeVariables[i]), typeVariables[j]));
          assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(typeVariables[j]), typeVariables[i]));
        }
      }
    }
  }

  @Test
  final void testWildcardAssignableFromWildcard() {
    assertTrue(this.covariantTypeSemantics.isAssignable(UnboundedWildcardType.INSTANCE, UnboundedWildcardType.INSTANCE));
    assertTrue(this.covariantTypeSemantics.isAssignable(UnboundedWildcardType.INSTANCE, new UpperBoundedWildcardType(Number.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Number.class), new UpperBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Number.class), UnboundedWildcardType.INSTANCE));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Integer.class), UnboundedWildcardType.INSTANCE));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Integer.class), new UpperBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Integer.class), new UpperBoundedWildcardType(Integer.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(UnboundedWildcardType.INSTANCE, new LowerBoundedWildcardType(Integer.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Number.class), new LowerBoundedWildcardType(Integer.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Integer.class), new LowerBoundedWildcardType(Integer.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Number.class), new LowerBoundedWildcardType(Number.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Integer.class), new LowerBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Number.class), new LowerBoundedWildcardType(Integer.class)));
  }

  @Test
  final <A extends Throwable, B extends A, C extends B, D extends Exception> void testWildcardAssignableFromWildcard2() {
    final int count = 4;
    Type[] typeVariables = new Type[count];
    final Type a = typeVariables[0] = new TypeLiteral<A>() {}.getType();
    final Type b = typeVariables[1] = new TypeLiteral<B>() {}.getType();
    final Type c = typeVariables[2] = new TypeLiteral<C>() {}.getType();
    final Type d = typeVariables[3] = new TypeLiteral<D>() {}.getType();
    for (int i = 0; i < count; i++) {
      assertTrue(this.covariantTypeSemantics.isAssignable(UnboundedWildcardType.INSTANCE,
                                                          new UpperBoundedWildcardType(typeVariables[i])));
      assertTrue(this.covariantTypeSemantics.isAssignable(UnboundedWildcardType.INSTANCE,
                                                          new LowerBoundedWildcardType(typeVariables[i])));
      for (int j = 0; j < count; j++) {
        assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(typeVariables[i]),
                                                             new LowerBoundedWildcardType(typeVariables[j])));
        assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(typeVariables[i]),
                                                             new UpperBoundedWildcardType(typeVariables[j])));
        if (i == j || (i < j && j != 3)) {
          assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(typeVariables[i]),
                                                              new UpperBoundedWildcardType(typeVariables[j])));
          assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(typeVariables[j]),
                                                              new LowerBoundedWildcardType(typeVariables[i])));
        } else {
          assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(typeVariables[i]),
                                                               new UpperBoundedWildcardType(typeVariables[j])));
          assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(typeVariables[j]),
                                                               new LowerBoundedWildcardType(typeVariables[i])));
        }
      }
    }
    assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Exception.class),
                                                        new UpperBoundedWildcardType(d)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(RuntimeException.class),
                                                         new UpperBoundedWildcardType(d)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Serializable.class),
                                                        new UpperBoundedWildcardType(b)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Throwable.class),
                                                        new UpperBoundedWildcardType(b)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Exception.class),
                                                         new UpperBoundedWildcardType(b)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(a),
                                                        new LowerBoundedWildcardType(Throwable.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(c),
                                                        new LowerBoundedWildcardType(Throwable.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(a),
                                                         new LowerBoundedWildcardType(Exception.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(c),
                                                         new LowerBoundedWildcardType(Exception.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(d),
                                                        new LowerBoundedWildcardType(Throwable.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(d),
                                                        new LowerBoundedWildcardType(Exception.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(d),
                                                         new LowerBoundedWildcardType(RuntimeException.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(RuntimeException.class),
                                                         new LowerBoundedWildcardType(a)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Throwable.class),
                                                         new LowerBoundedWildcardType(a)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(RuntimeException.class),
                                                         new LowerBoundedWildcardType(d)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Throwable.class),
                                                         new LowerBoundedWildcardType(d)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(Exception.class),
                                                         new LowerBoundedWildcardType(d)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(Exception.class),
                                                         new UpperBoundedWildcardType(d)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(d),
                                                         new UpperBoundedWildcardType(Exception.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(d),
                                                         new LowerBoundedWildcardType(Exception.class)));
  }

  @Test
  final void testWildcardAssignableFromGenericArrayType() {
    assertTrue(this.covariantTypeSemantics.isAssignable(UnboundedWildcardType.INSTANCE,
                                                        new DefaultGenericArrayType(List.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(new DefaultGenericArrayType(List.class, Number.class)),
                                                        new DefaultGenericArrayType(List.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(new DefaultGenericArrayType(List.class, Number.class)),
                                                        new DefaultGenericArrayType(ArrayList.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new UpperBoundedWildcardType(new DefaultGenericArrayType(List.class, Number.class)),
                                                         new DefaultGenericArrayType(Collection.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(new DefaultGenericArrayType(List.class, Number.class)),
                                                        new DefaultGenericArrayType(List.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(new DefaultGenericArrayType(List.class, Number.class)),
                                                        new DefaultGenericArrayType(Collection.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new LowerBoundedWildcardType(new DefaultGenericArrayType(List.class, Number.class)),
                                                         new DefaultGenericArrayType(ArrayList.class, Number.class)));
  }

  @Test
  final void testGenericArrayTypeAssignableFromRawType() {
    @SuppressWarnings("rawtypes")
    final Class<?> rawArrayListArrayClass = new ArrayList[0].getClass();
    @SuppressWarnings("rawtypes")
    final Class<?> rawListArrayClass = new List[0].getClass();
    @SuppressWarnings("rawtypes")
    final Class<?> rawObjectArrayClass = new Object[0].getClass();
    @SuppressWarnings("rawtypes")
    final Class<?> rawSetArrayClass = new Set[0].getClass();
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(List.class, Number.class),
                                                        rawListArrayClass));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(List.class, Number.class),
                                                        rawArrayListArrayClass));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(List.class, Number.class),
                                                         rawObjectArrayClass));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(List.class, Number.class),
                                                         rawSetArrayClass));
  }

  @Test
  final void testGenericArrayTypeAssignableFromParameterizedType() {
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(List.class, Number.class),
                                                         new DefaultParameterizedType(null, List.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(List.class, Number.class),
                                                         new DefaultParameterizedType(null, List.class, Number.class)));
  }

  @Test
  final <A, B extends List<Number>> void testGenericArrayTypeAssignableFromTypeVariable() {
    final Type a = new TypeLiteral<A>() {}.getType();
    final Type b = new TypeLiteral<B>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(List.class, Number.class), a));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(List.class, Number.class), b));
  }

  @Test
  final <A, B extends Number> void testGenericArrayTypeAssignableFromWildcard() {
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                         UnboundedWildcardType.INSTANCE));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                         new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class))));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                         new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class))));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                         new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Integer.class))));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                         new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Integer.class))));
  }

  @Test
  final void testGenericArrayTypeAssignableFromGenericArrayType() {
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                        new DefaultGenericArrayType(Collection.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                        new DefaultGenericArrayType(List.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                        new DefaultGenericArrayType(ArrayList.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(Collection.class, UnboundedWildcardType.INSTANCE),
                                                        new DefaultGenericArrayType(ArrayList.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                         new DefaultGenericArrayType(Iterable.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                         new DefaultGenericArrayType(Collection.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                         new DefaultGenericArrayType(Collection.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.isAssignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                         new DefaultGenericArrayType(Collection.class, Double.class)));

  }

  @Test
  final void testDifferentTypeParametersDoNotMatch() {
    final Type receiverType = new DefaultParameterizedType(null, Foo.class, Integer.class);
    final Type payloadType = new DefaultParameterizedType(null, Foo.class, String.class);
    assertFalse(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));
  }

  @Test
  final void testArrayAndComponentTypeIncompatibility() {
    final Type receiverType = new TypeLiteral<Foo<String>[]>() {}.getType();
    final Type payloadType = new TypeLiteral<Foo<String>>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));
  }

  @Test
  final void testIncompatibleParameterizedTypeArrays() {
    final Type receiverType = new TypeLiteral<Foo<Integer>[]>() {}.getType();
    final Type payloadType = new TypeLiteral<Foo<String>[]>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));
  }

  @Test
  final void testRealTypeParameterAssignableToWildcard() {
    final Type receiverType = new TypeLiteral<Foo<?>>() {}.getType();
    final Type payloadType = new TypeLiteral<Foo<String>>() {}.getType();
    assertTrue(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));
  }

  @Test
  final void testArrayOfRealParameterizedTypeIsAssignableToArrayOfWildcardParameterizedType() {
    final Type receiverType = new TypeLiteral<Foo<?>[]>() {}.getType();
    final Type payloadType = new TypeLiteral<Foo<String>[]>() {}.getType();
    assertTrue(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));
  }

  @Test
  final void testFooQuestionMarkArrayIsNotAssignableToFooStringArray() {
    final Type receiverType = new TypeLiteral<Foo<String>[]>() {}.getType();
    final Type payloadType = new TypeLiteral<Foo<?>[]>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));
  }

  @Test
  final void testArrayBoxNotAllowed() {
    assertFalse(this.covariantTypeSemantics.isAssignable(int[].class, Integer[].class));
  }

  @Test
  final void testMultidimensionalArrayTypeParameterCase() {
    final Type receiverType = new TypeLiteral<List<Number[][]>>() {}.getType();
    final Type payloadType = new TypeLiteral<List<Integer[][]>>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));
  }

  @Test
  final <T> void testFooTIsAssignableToFooStringWithBeanTypeSemantics() {
    final Type receiverType = new TypeLiteral<Foo<String>>() {}.getType();
    final Type payloadType = new TypeLiteral<Foo<T>>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));
  }

  @Test
  final void testFooObjectIsAssignableToFoo() {
    final Type payloadType = new TypeLiteral<Foo<Object>>() {}.getType();
    assertTrue(this.covariantTypeSemantics.isAssignable(Foo.class, payloadType));
  }

  @Test
  final <T> void testFooTIsAssignableToFoo() {
    final Type payloadType = new TypeLiteral<Foo<T>>() {}.getType();
    assertTrue(this.covariantTypeSemantics.isAssignable(Foo.class, payloadType));
  }

  @Test
  final <N extends Number> void testFooNExtendsNumberIsAssignableToFoo() {
    final Type payloadType = new TypeLiteral<Foo<N>>() {}.getType();
    assertTrue(this.covariantTypeSemantics.isAssignable(Foo.class, payloadType));
  }

  @Test
  final <T1 extends Number, T2 extends T1> void testTypeVariableWithTypeVariableBound() {
    final Type n1 = new TypeLiteral<List<Number>>() {}.getType();
    final Type payloadType = new TypeLiteral<List<T2>>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(n1, payloadType));

    final Type r1 = new TypeLiteral<List<Runnable>>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(r1, payloadType));

    final Type receiverType = new TypeLiteral<List<T1>>() {}.getType();
    assertFalse(this.covariantTypeSemantics.isAssignable(receiverType, payloadType));

    // Covariantly, receiverType can't be assigned to payloadType.
    assertFalse(this.covariantTypeSemantics.isAssignable(payloadType, receiverType));
  }

  @Test
  final void testArrayCovariance1() {
    // Prove the JDK lets us compile this.
    final Number[] numbers = new Integer[] { Integer.valueOf(0) };
    final Type type1 = new Number[0].getClass();
    final Type type2 = new Integer[0].getClass();
    assertTrue(this.covariantTypeSemantics.isAssignable(type1, type2));
  }


  /*
   * Inner and nested classes.
   */


  private static final class Foo<T> {
  }

  private static final class Bar implements Function<Contextual<?>, TypeSet> {

    @Override
    public final TypeSet apply(final Contextual<?> c) {
      return new TypeSet();
    }

  }

}
