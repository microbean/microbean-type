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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.GenericDeclaration;

import java.util.List;
import java.util.Objects;

import jakarta.enterprise.util.TypeLiteral; // for convenience

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class TestSerialization {

  private TestSerialization() {
    super();
  }

  @Test
  final void testSerializeSimpleParameterizedType() throws ClassNotFoundException, IOException {
    final ParameterizedType ptype = (ParameterizedType)new TypeLiteral<List<String>>() {}.getType();
    assertFalse(ptype instanceof Serializable);
    assertNull(ptype.getOwnerType());
    assertEquals(List.class, ptype.getRawType());
    final Type[] ptypeArguments = ptype.getActualTypeArguments();
    assertNotNull(ptypeArguments);
    assertEquals(1, ptypeArguments.length);
    assertEquals(String.class, ptypeArguments[0]);
    final byte[] bytes = toBytes(Types.toSerializableType(ptype));
    final DefaultParameterizedType deserializedType = fromBytes(bytes);
    assertNotNull(deserializedType);
    assertNull(deserializedType.getOwnerType());
    assertEquals(List.class, deserializedType.getRawType());
    final Type[] deserializedTypeArguments = deserializedType.getActualTypeArguments();
    assertNotNull(deserializedTypeArguments);
    assertEquals(1, deserializedTypeArguments.length);
    assertEquals(String.class, deserializedTypeArguments[0]);
  }

  @Test
  final void testSerializeSecondOrderParameterizedType() throws ClassNotFoundException, IOException {
    final ParameterizedType ptype = (ParameterizedType)new TypeLiteral<List<List<String>>>() {}.getType();
    assertFalse(ptype instanceof Serializable);
    assertNull(ptype.getOwnerType());
    assertEquals(List.class, ptype.getRawType());
    Type[] ptypeArguments = ptype.getActualTypeArguments();
    assertNotNull(ptypeArguments);
    assertEquals(1, ptypeArguments.length);
    Type firstArgument = ptypeArguments[0];
    assertTrue(firstArgument instanceof ParameterizedType);
    ParameterizedType firstPtype = (ParameterizedType)firstArgument;
    assertEquals(List.class, firstPtype.getRawType());
    ptypeArguments = firstPtype.getActualTypeArguments();
    assertNotNull(ptypeArguments);
    assertEquals(1, ptypeArguments.length);
    assertEquals(String.class, ptypeArguments[0]);
    final byte[] bytes = toBytes(Types.toSerializableType(ptype));
    final DefaultParameterizedType deserializedType = fromBytes(bytes);
    assertNotNull(deserializedType);
    assertNull(deserializedType.getOwnerType());
    assertEquals(List.class, deserializedType.getRawType());
    ptypeArguments = deserializedType.getActualTypeArguments();
    assertNotNull(ptypeArguments);
    assertEquals(1, ptypeArguments.length);
    firstArgument = ptypeArguments[0];
    assertTrue(firstArgument instanceof ParameterizedType);
    firstPtype = (ParameterizedType)firstArgument;
    assertEquals(List.class, firstPtype.getRawType());
    ptypeArguments = firstPtype.getActualTypeArguments();
    assertNotNull(ptypeArguments);
    assertEquals(1, ptypeArguments.length);
    assertEquals(String.class, ptypeArguments[0]);
  }

  @Test
  final <T extends Integer> void testSerializeTypeVariable() throws ClassNotFoundException, IOException {
    final ParameterizedType ptype = (ParameterizedType)new TypeLiteral<Uninitializable<T>>() {}.getType();
    assertFalse(ptype instanceof Serializable);
    assertSame(this.getClass(), ptype.getOwnerType());
    assertEquals(Uninitializable.class, ptype.getRawType());
    final Type[] ptypeArguments = ptype.getActualTypeArguments();
    assertNotNull(ptypeArguments);
    assertEquals(1, ptypeArguments.length);
    TypeVariable<?> tv = (TypeVariable<?>)ptypeArguments[0];
    GenericDeclaration gd = tv.getGenericDeclaration();
    assertTrue(gd instanceof java.lang.reflect.Method);
    assertEquals("T", tv.getName());
    Type[] bounds = tv.getBounds();
    assertNotSame(bounds, tv.getBounds());
    assertNotNull(bounds);
    assertEquals(1, bounds.length);
    assertEquals(Integer.class, bounds[0]);
    final byte[] bytes = toBytes(Types.toSerializableType(ptype));
    final DefaultParameterizedType deserializedType = fromBytes(bytes);
    assertNotNull(deserializedType);
    assertSame(this.getClass(), deserializedType.getOwnerType());
    assertEquals(Uninitializable.class, deserializedType.getRawType());
    final Type[] deserializedTypeArguments = deserializedType.getActualTypeArguments();
    assertNotNull(deserializedTypeArguments);
    assertEquals(1, deserializedTypeArguments.length);
    final Type t = deserializedTypeArguments[0];
    assertTrue(t instanceof DefaultTypeVariable);
    tv = (TypeVariable<?>)t;
    assertEquals("T", tv.getName());
    bounds = tv.getBounds();
    assertNotNull(bounds);
    assertEquals(1, bounds.length);
    assertEquals(Integer.class, bounds[0]);
    gd = tv.getGenericDeclaration();
    assertNotNull(gd);
  }

  private static final byte[] toBytes(final Serializable object) throws IOException {
    assertNotNull(object);
    try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
         final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(baos);
         final ObjectOutputStream out = new ObjectOutputStream(bufferedOutputStream)) {
      out.writeObject(object);
      out.flush();
      return baos.toByteArray();
    }
  }

  @SuppressWarnings("unchecked")
  private static final <T extends Serializable> T fromBytes(final byte[] bytes) throws ClassNotFoundException, IOException {
    try (final ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(bytes)))) {
      return (T)stream.readObject();
    }
  }

  private static final class Uninitializable<T> {

    static {
      // This proves during deserialization that even static
      // initializers don't run.  See testSerializeTypeVariable()
      // above.  So deserialization of types as we do in this library
      // cannot run unexpected code.
      fail();
    }

  }

}
