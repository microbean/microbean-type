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
import java.lang.reflect.WildcardType;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.util.TypeLiteral;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class TestGetSupertypes {

  private TestGetSupertypes() {
    super();
  }

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
      // System.out.println("  " + Types.toString(supertype));
    }
    if (!found) {
      fail("Never found " + Types.toString(listString) + " in " + Types.toString(supertypes));
    }
    
  }
}
