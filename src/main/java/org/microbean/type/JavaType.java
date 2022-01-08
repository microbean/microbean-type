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

import java.util.Objects;

public final class JavaType {

  private final Type type;

  private JavaType(final Type type) {
    super();
    this.type = Objects.requireNonNull(type, "type");
  }

  public final Type type() {
    return this.type;
  }

  @Override
  public final int hashCode() {
    return JavaTypes.hashCode(this.type);
  }

  @Override
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && this.getClass() == other.getClass()) {
      return JavaTypes.equals(this.type, ((JavaType)other).type);
    } else {
      return false;
    }
  }

  public static final JavaType of(final Type type) {
    return new JavaType(type);
  }
  
}
