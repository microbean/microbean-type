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

import java.io.Serializable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.Collection;
import java.util.List;

import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import org.microbean.type.NewJavaType.Token;
import org.microbean.type.OldJavaType.CdiSemantics;
import org.microbean.type.OldJavaType.CovariantSemantics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Deprecated
final class TestOldCdiTypeSemantics {

  private static final CovariantSemantics covariantTypeSemantics = new CovariantSemantics(true);

  private static final CdiSemantics cdiSemantics = new CdiSemantics();

  private TestOldCdiTypeSemantics() {
    super();
  }

  @Test
  final void testLegalBeanTypeScenario() {
    final Type receiverType = new Token<Function<? super Contextual<?>, ? extends TypeSet>>() {}.type();
    final Type payloadType = new Token<Function<Contextual<Object>, TypeSet>>() {}.type();
    assertFalse(cdiSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final <T, U extends TypeSet> void testLegalBeanTypeScenario2() {
    final Type receiverType = new Token<Function<? super Contextual<?>, ? extends TypeSet>>() {}.type();
    final Type payloadType = new Token<Function<Contextual<T>, U>>() {}.type();
    assertFalse(cdiSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testParameterizedReceiverTypeRawPayloadType() {
    final Type receiverType = new Token<Predicate<Contextual<?>>>() {}.type();
    final Type payloadType = Predicate.class;
    // "A raw bean type is considered assignable to a parameterized
    // required type if the raw types are identical and all type
    // parameters of the required type are either unbounded type
    // variables or java.lang.Object."  Here the required type
    // (receiverType) has a type parameter (Contextual<?>) that is NOT
    // unbounded and is NOT java.lang.Object.
    assertFalse(cdiSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testRawReceiverTypeParameterizedPayloadType() {
    final Type receiverType = Predicate.class;
    final Type payloadType = new Token<Predicate<Contextual<?>>>() {}.type();
    // "A parameterized bean type is considered assignable to a raw
    // required type if the raw types are identical and all type
    // parameters of the bean type are either unbounded type variables
    // or java.lang.Object."  Here the bean type (payloadType) has a
    // type parameter (Contextual<?>) that is NOT unbounded and is NOT
    // java.lang.Object.
    assertFalse(cdiSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testLowerBoundedWildcardCase() {
    final Type receiverType = new Token<Predicate<? super Contextual<?>>>() {}.type();
    final Type payloadType = new Token<Predicate<Contextual<?>>>() {}.type();
    assertTrue(cdiSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testResolvedWildcardCase() {
    Type receiverType = new Token<Predicate<? super Contextual<?>>>() {}.type();
    final Type payloadType = new Token<Predicate<Contextual<Object>>>() {}.type();
    assertFalse(cdiSemantics.assignable(receiverType, payloadType));
    // This is the type you get when you call TypeResolver.resolve(receiverType).  Ugh.
    // See https://gitlab.com/microbean.systems/ristretto/-/issues/11.
    receiverType = new Token<Predicate<Object>>() {}.type();
    assertFalse(cdiSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final <T> void testFooTAssignableToFooStringWithBeanTypeSemantics() {
    final ParameterizedType receiverType = (ParameterizedType)new Token<Foo<String>>() {}.type();
    final ParameterizedType payloadType = (ParameterizedType)new Token<Foo<T>>() {}.type();
    assertSame(Object.class, ((TypeVariable<?>)payloadType.getActualTypeArguments()[0]).getBounds()[0]);

    // Java/covariant semantics do not let this assignment happen.
    assertFalse(covariantTypeSemantics.assignable(receiverType, payloadType));

    // Somewhat counterintuitively, CDI semantics permit it:
    //
    // "A parameterized bean type is considered assignable to a
    // parameterized required type if they have identical raw type and
    // for each parameter:
    // […]
    // "the required type parameter is an actual type [String], the
    // bean type parameter is a type variable [T] and the actual type
    // [String] is assignable to the upper bound, if any [Object], of
    // the type variable…"
    assertTrue(cdiSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testFooObjectAssignableToFoo() {
    final Type receiverType = Foo.class;
    final Type payloadType = new Token<Foo<Object>>() {}.type();
    assertTrue(covariantTypeSemantics.assignable(receiverType, payloadType));
    assertTrue(cdiSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final <T> void testFooTAssignableToFoo() {
    final Type receiverType = Foo.class;
    final Type payloadType = new Token<Foo<T>>() {}.type();
    assertTrue(covariantTypeSemantics.assignable(receiverType, payloadType));
    assertTrue(cdiSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final <N extends Number> void testFooNExtendsNumberAssignableToFoo() {
    final ParameterizedType payloadType = (ParameterizedType)new Token<Foo<N>>() {}.type();
    assertTrue(covariantTypeSemantics.assignable(Foo.class, payloadType));
    assertFalse(cdiSemantics.assignable(Foo.class, payloadType));
  }

  @Test
  final <T1 extends Number, T2 extends T1> void testTypeVariableWithTypeVariableBound() {
    final Type n1 = new Token<List<Number>>() {}.type();
    final Type payloadType = new Token<List<T2>>() {}.type();
    assertFalse(covariantTypeSemantics.assignable(n1, payloadType));
    assertTrue(cdiSemantics.assignable(n1, payloadType));

    final Type r1 = new Token<List<Runnable>>() {}.type();
    assertFalse(covariantTypeSemantics.assignable(r1, payloadType));
    assertFalse(cdiSemantics.assignable(r1, payloadType));

    final Type receiverType = new Token<List<T1>>() {}.type();
    assertFalse(covariantTypeSemantics.assignable(receiverType, payloadType));
    assertTrue(cdiSemantics.assignable(receiverType, payloadType));

    // Covariantly, receiverType can't be assigned to payloadType...
    assertFalse(covariantTypeSemantics.assignable(payloadType, receiverType));

    // ...but with bean type semantics, receiverType and payloadType
    // are interchangeable.
    assertTrue(cdiSemantics.assignable(payloadType, receiverType));
  }

  @Test
  final <T1 extends Number, T2 extends T1> void testWildcardWithTypeVariableBound() {
    final Type n1 = new Token<List<Number>>() {}.type();
    final Type qet2 = new Token<List<? extends T2>>() {}.type();
    final Type qst2 = new Token<List<? super T2>>() {}.type();
    final Type i1 = new Token<List<Integer>>() {}.type();
    final Type o1 = new Token<List<Object>>() {}.type();
    assertTrue(cdiSemantics.assignable(qet2, n1));

    assertTrue(cdiSemantics.assignable(qet2, i1));

    assertFalse(cdiSemantics.assignable(qet2, o1));

    assertTrue(cdiSemantics.assignable(qst2, n1));

    assertFalse(cdiSemantics.assignable(qst2, i1));

    assertTrue(cdiSemantics.assignable(qst2, o1));
  }

  @Test
  public
    <T1 extends List<?> & Appendable,
     T2 extends java.io.Writer & java.io.Serializable & Collection<?>,
     T3 extends T2,
     T4 extends Appendable & Iterable<?>,
     T5 extends T4>
    void testTypeVariableWithMultipleBounds() {
    assertTrue(cdiSemantics.assignable(new Token<List<T2>>() {}.type(),
                                       new Token<List<T4>>() {}.type()));
    assertTrue(cdiSemantics.assignable(new Token<List<T3>>() {}.type(),
                                       new Token<List<T5>>() {}.type()));
    assertTrue(cdiSemantics.assignable(new Token<List<? extends T1>>() {}.type(),
                                       new Token<List<T4>>() {}.type()));
    assertTrue(cdiSemantics.assignable(new Token<List<? extends T2>>() {}.type(),
                                       new Token<List<T4>>() {}.type()));
    assertTrue(cdiSemantics.assignable(new Token<List<? extends T3>>() {}.type(),
                                       new Token<List<T5>>() {}.type()));
    assertFalse(cdiSemantics.assignable(new Token<List<T4>>() {}.type(),
                                        new Token<List<T1>>() {}.type()));
    assertFalse(cdiSemantics.assignable(new Token<List<T2>>() {}.type(),
                                        new Token<List<T1>>() {}.type()));
    assertFalse(cdiSemantics.assignable(new Token<List<T4>>() {}.type(),
                                        new Token<List<T2>>() {}.type()));
    assertFalse(cdiSemantics.assignable(new Token<List<T1>>() {}.type(),
                                        new Token<List<T2>>() {}.type()));
    assertFalse(cdiSemantics.assignable(new Token<List<T2>>() {}.type(),
                                        new Token<List<T1>>() {}.type()));
    assertFalse(cdiSemantics.assignable(new Token<List<? extends T1>>() {}.type(),
                                        new Token<List<T2>>() {}.type()));
    assertFalse(cdiSemantics.assignable(new Token<List<? extends T2>>() {}.type(),
                                        new Token<List<T1>>() {}.type()));
  }

  @Test
  public
    <T1 extends List<?> & Appendable,
     T4 extends Appendable & Iterable<?>>
    void testTypeVariableWithInterfaceBoundsIsAssignableToTypeVariableWithInterfaceBounds() {
    assertTrue(cdiSemantics.assignable(new Token<List<T1>>() {}.type(),
                                       new Token<List<T4>>() {}.type()));
  }

  @Test
  final
    <T1,
    T2 extends T1,
    T3 extends Collection<T1>,
    T4 extends Collection<T2>,
    T5 extends Collection<Number>,
    T6 extends Collection<Integer>,
    T7 extends Collection<?>>
  void testTypeVariableWithParameterizedTypesAsBounds() {
    assertTrue(cdiSemantics.assignable(new Token<List<T5>>() {}.type(),
                                       new Token<List<T5>>() {}.type()));
    assertFalse(cdiSemantics.assignable(new Token<List<T6>>() {}.type(),
                                        new Token<List<T5>>() {}.type()));
    assertFalse(cdiSemantics.assignable(new Token<List<T5>>() {}.type(),
                                        new Token<List<T6>>() {}.type()));
    assertFalse(cdiSemantics.assignable(new Token<List<T7>>() {}.type(),
                                        new Token<List<T5>>() {}.type()));
    assertTrue(cdiSemantics.assignable(new Token<List<T3>>() {}.type(),
                                       new Token<List<T3>>() {}.type()));
    assertFalse(cdiSemantics.assignable(new Token<List<T4>>() {}.type(),
                                        new Token<List<T3>>() {}.type()));
    assertFalse(cdiSemantics.assignable(new Token<List<T3>>() {}.type(),
                                        new Token<List<T4>>() {}.type()));
    assertFalse(cdiSemantics.assignable(new Token<List<T7>>() {}.type(),
                                        new Token<List<T3>>() {}.type()));
  }


  /*
   * Inner and nested classes.
   */

  private static final class TypeSet {

  }

  private static final class Contextual<T> {
  }

  private static final class Foo<T> {
  }

  private static final class Bar implements Function<Contextual<?>, TypeSet> {

    @Override
    public final TypeSet apply(final Contextual<?> c) {
      return new TypeSet();
    }

  }

}
