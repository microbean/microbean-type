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

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import java.util.function.Function;

public class BeanType extends JavaType {

  private final Function<Type, Collection<Type>> directSupertypesFunction;
  
  public BeanType(final Type type, final Function<Type, Collection<Type>> directSupertypesFunction) {
    super(type);
    this.directSupertypesFunction = Objects.requireNonNull(directSupertypesFunction, "directSupertypesFunction");
  }

  @Override
  public Collection<JavaType> directSupertypes() {
    final Collection<Type> directSupertypes = this.directSupertypesFunction.apply(this.object());
    if (!directSupertypes.isEmpty()) {
      final Collection<JavaType> c = new ArrayList<>(directSupertypes.size());
      for (final Type type : directSupertypes) {
        c.add(of(type));
      }
      return Collections.unmodifiableCollection(c);
    }
    return List.of();
  }
  
}
