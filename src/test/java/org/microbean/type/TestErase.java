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

import java.util.List;
import java.util.RandomAccess;

import jakarta.enterprise.util.TypeLiteral;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

final class TestErase {

  private TestErase() {
    super();
  }

  @Test
  final <T extends RandomAccess & CharSequence> void testTypeVariable() {
    // Note the type variable in the method signature.  "The erasure
    // of a type variable is the erasure of its leftmost bound."
    assertSame(RandomAccess.class,
               Types.erase(((ParameterizedType)new TypeLiteral<List<T>>() {}.getType()).getActualTypeArguments()[0]));
  }
  
}
