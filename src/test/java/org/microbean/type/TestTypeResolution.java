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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class TestTypeResolution {

  private TestTypeResolution() {
    super();
  }

  @Test
  final void testToTypes() {
    final TypeSet typeSet = Types.toTypes(Types.normalize(ConcreteFactory.class));
    System.out.println("*** typeSet:");
    typeSet.forEach(t -> System.out.println("    " + t));
  }

  // These classes model a real-world use case that caused this bug to
  // be discovered.

  // Contextual<T>
  private static interface Maker<T> {

    public T make();

  }

  // AlternativeContextual<T> extends Contextual<T> (and PrioritizedAlternative)
  private static interface AltMaker<T> extends Maker<T> {

  }

  /* *** */

  // ContextualInstanceFactory<T>
  private static interface Factory<T> {

    public T create();
    
  }

  // AbstractContextualInstanceFactory<T> implements Factory<T>
  private static abstract class AbstractFactory<T> implements Factory<T> {

  }

  /* *** */

  // AbstractContextualInstanceFactoryContextual<T> extends AbstractContextualInstanceFactory<T> implements AlternativeContextual<T>
  private static abstract class AbstractFactoryAndAltMaker<T> extends AbstractFactory<T> implements AltMaker<T> {

  }

  /* *** */

  // Provider<T>
  private static interface Getter<T> {

  }
    
  // Instance<T>
  private static interface Iterator<T> extends Getter<T> {

  }

  // ContextualReferenceSupplierBackedInstance<T>
  private static final class ConcreteIterator<T> implements Iterator<T> {

  }

  /* *** */

  // InstanceAlternativeContextual<T> extends AbstractContextualInstanceFactoryContextual<ContextualReferenceSupplierBackedInstance<T>>
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
