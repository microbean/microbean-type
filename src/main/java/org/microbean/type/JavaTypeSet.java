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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class JavaTypeSet implements Iterable<Type> {

  private final Set<JavaType> set;

  private volatile Type mostSpecializedSupertype;
  
  private volatile Type mostSpecializedSuperinterface;

  public JavaTypeSet(final Type type) {
    super();
    this.set = Set.of(JavaType.of(type));
  }
  
  public JavaTypeSet(final Collection<? extends Type> types) {
    super();
    if (types == null || types.isEmpty()) {
      this.set = Set.of();
    } else {
      final Set<JavaType> set = new HashSet<>();
      for (final Type type : types) {
        set.add(JavaType.of(type));
      }
      this.set = Collections.unmodifiableSet(set);
    }
  }

  public final Type mostSpecializedSupertype() {
    Type mostSpecializedSupertype = this.mostSpecializedSupertype;
    if (mostSpecializedSupertype == NullType.INSTANCE) {
      return null;
    } else if (mostSpecializedSupertype == null) {
      
      this.mostSpecializedSupertype = mostSpecializedSupertype;
    }
    return mostSpecializedSupertype;
  }

  public final boolean contains(final Type type) {
    return this.set.contains(JavaType.of(type));
  }

  public final boolean isEmpty() {
    return this.set.isEmpty();
  }

  public final Iterator<Type> iterator() {
    if (this.set.isEmpty()) {
      return Collections.emptyIterator();
    } else {
      final Iterator<JavaType> i = this.set.iterator();
      return new Iterator<>() {
        @Override
        public final boolean hasNext() {
          return i.hasNext();
        }

        @Override
        public final Type next() {
          return i.next().type();
        }
      };
    }
  }

  private static final class NullType implements Type {

    private static final Type INSTANCE = new NullType();
    
    private NullType() {
      super();
    }
    
  }
  
}
