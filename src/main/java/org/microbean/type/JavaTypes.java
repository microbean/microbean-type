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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public final class JavaTypes {

  private static final Type[] EMPTY_TYPE_ARRAY = new Type[0];
  
  public static final Type[] emptyTypeArray() {
    return EMPTY_TYPE_ARRAY;
  }
  
  public static final Collection<Type> directSupertypes(final Type type) {
    if (Objects.requireNonNull(type) instanceof Class<?> c) {
      return directSupertypes(c);
    } else if (type instanceof ParameterizedType p) {
      return directSupertypes(p);
    } else if (type instanceof GenericArrayType g) {
      return directSupertypes(g);
    } else if (type instanceof TypeVariable<?> tv) {
      return directSupertypes(tv);
    } else {
      throw new IllegalArgumentException("type: " + toString(type));
    }
  }

  private static final Collection<Type> directSupertypes(final Class<?> c) {
    if (c == Object.class || c.isInterface() || c.isPrimitive()) {
      return List.of();
    } else {
      final Collection<Type> directSupertypes = new ArrayList<>(11);
      final Class<?> componentType = c.getComponentType();
      if (componentType == null) {
        final Type genericSuperclass = c.getGenericSuperclass();
        assert genericSuperclass != null;
        assert (genericSuperclass instanceof Class) || (genericSuperclass instanceof ParameterizedType) : "Unexpected genericSuperclass: " + toString(genericSuperclass);
        directSupertypes.add(genericSuperclass);
        if (genericSuperclass instanceof ParameterizedType p) {
          directSupertypes.add(erase(p));
        }
        final Type[] directSuperinterfaces = c.getGenericInterfaces();
        if (directSuperinterfaces.length > 0) {
          for (final Type directSuperinterface : directSuperinterfaces) {
            assert (directSuperinterface instanceof Class) || (directSuperinterface instanceof ParameterizedType) : "Unexpected genericInterface: " + toString(directSuperinterface);
            directSupertypes.add(directSuperinterface);
            if (directSuperinterface instanceof ParameterizedType p) {
              directSupertypes.add(erase(p));
            }
          }
        } else {
          directSupertypes.add(Object.class);
        }
      } else if (componentType == Object.class || componentType.isPrimitive()) {
        directSupertypes.add(Object.class);
        directSupertypes.add(Cloneable.class);
        directSupertypes.add(java.io.Serializable.class);
      } else {
        final Class<?> superclass = componentType.getSuperclass();
        assert superclass != null;
        directSupertypes.add(superclass.arrayType());
        final Type[] directInterfaces = componentType.getGenericInterfaces();
        for (final Type directSuperinterface : directInterfaces) {
          assert (directSuperinterface instanceof Class) || (directSuperinterface instanceof ParameterizedType) : "Unexpected genericInterface: " + toString(directSuperinterface);
          final Type arraySuperinterface = array(directSuperinterface);
          directSupertypes.add(arraySuperinterface);
          if (arraySuperinterface instanceof GenericArrayType g) {
            directSupertypes.add(erase(g));
          }
        }
      }
      return Collections.unmodifiableCollection(directSupertypes);
    }
  }

  private static final Collection<Type> directSupertypes(final ParameterizedType p) {
    return directSupertypes(p.getRawType()); // doesn't include wildcard-argumented parameterized types
    /*
    final Collection<Type> directSupertypes = new ArrayList<>(directSupertypes(p.getRawType()));
    final Type[] arguments = p.getActualTypeArguments();
    final Type[] containingTypeArguments = new Type[arguments.length];
    OUTER_LOOP:
    for (int i = 0; i < 3; i++) {
      boolean atLeastOneContainingArgument = false;
      for (int j = 0; j < arguments.length; j++) {
        final Type argument = arguments[j];
        if (argument instanceof WildcardType w) {
          final Type[] lowerBounds = w.getLowerBounds();
          if (lowerBounds.length <= 0) {
            final Type[] upperBounds = w.getUpperBounds();
            if (upperBounds.length <= 0 || upperBounds[0] == Object.class) {
              // Wildcard is unbounded; leave it as is
              containingTypeArguments[j] = argument;
            } else {
              // Wildcard is upper-bounded (extends)
              containingTypeArguments[j] = UnboundedWildcardType.INSTANCE;
              if (!atLeastOneContainingArgument) {
                atLeastOneContainingArgument = true;
              }
            }
          } else {
            // Wildcard is lower-bounded (super)
            containingTypeArguments[j] = new LowerBoundedWildcardType(Object.class);
            if (!atLeastOneContainingArgument) {
              atLeastOneContainingArgument = true;
            }
          }
        } else {
          if (!atLeastOneContainingArgument) {
            atLeastOneContainingArgument = true;
          }
          switch (i) {
          case 0:
            containingTypeArguments[j] = UnboundedWildcardType.INSTANCE;
            break;
          case 1:
            containingTypeArguments[j] = new UpperBoundedWildcardType(argument);
            break;
          case 2:
            containingTypeArguments[j] = new LowerBoundedWildcardType(argument);
            break;
          default:
            throw new AssertionError("Bad i: " + i);
          }
        }
      }
      if (!atLeastOneContainingArgument) {
        break OUTER_LOOP;
      }
      directSupertypes.add(new DefaultParameterizedType(p.getOwnerType(), p.getRawType(), containingTypeArguments)); // containingTypeArguments will be cloned
    }
    return Collections.unmodifiableCollection(directSupertypes);
    */
  }

  private static final Collection<Type> directSupertypes(final TypeVariable<?> tv) {
    // "The direct supertypes of a type variable are the types listed in its bound."
    return List.of(tv.getBounds());
  }

  private static final Collection<Type> directSupertypes(final GenericArrayType g) {
    final Collection<Type> genericComponentTypeDirectSupertypes = directSupertypes(g.getGenericComponentType());
    final Collection<Type> returnValue = new ArrayList<>(genericComponentTypeDirectSupertypes.size());
    for (final Type ds : genericComponentTypeDirectSupertypes) {
      returnValue.add(array(ds));
    }
    return Collections.unmodifiableCollection(returnValue);
  }

  public static final Collection<Type> supertypes(final Type type) {
    return supertypes(type, new HashSet<>());
  }

  private static final Collection<Type> supertypes(final Type type, final Set<String> seen) {
    if (seen.add(toString(type))) {
      final Collection<Type> supertypes = new ArrayList<>();
      supertypes.add(type); // reflexive
      for (final Type ds : directSupertypes(type)) {
        supertypes.addAll(supertypes(ds, seen)); // XXX recursive
      }
      return Collections.unmodifiableCollection(supertypes);
    } else {
      return List.of();
    }
  }

  public static final boolean supertype(final Type sup, final Type sub) {
    // Is sup a supertype of sub?
    if (equals(Objects.requireNonNull(sup, "sup"), Objects.requireNonNull(sub, "sub"))) {
      return true;
    } else if (sup instanceof Class<?> supC && sub instanceof Class<?> subC) {
      // Easy optimization
      return supC.isAssignableFrom(subC);
    } else {
      for (final Type supertype : supertypes(sub)) {
        if (equals(supertype, sup)) {
          return true;
        }
      }
    }
    return false;
  }
  
  private static final Type array(final Type type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-10.html#jls-10.1
    // 10.1 Array Types
    // …
    // The element type of an array may be any type, whether
    // primitive or reference.
    // …
    // [That rules out wildcards, but nothing else.]
    if (Objects.requireNonNull(type, "type") instanceof Class<?> c) {
      return array(c);
    } else if (type instanceof GenericArrayType) {
      return type;
    } else if (!(type instanceof WildcardType)) {
      return new DefaultGenericArrayType(type);
    } else {
      throw new IllegalArgumentException("type: " + toString(type));
    }
  }

  private static final Class<?> array(final Class<?> type) {
    // return type.isArray() ? type : Array.newInstance(type, 0).getClass();
    return type.isArray() ? type : type.arrayType();
  }

  public static final boolean isReferenceType(final Type type) {
    // Wildcards are ruled out by the BNF in
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.5.1
    return !(type == null || type instanceof WildcardType || (type instanceof Class<?> c && c.isPrimitive()));
  }

  /**
   * Returns the type erasure for the supplied {@link Type} according
   * to <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.6"
   * target="_parent">the rules of the Java Language
   * Specification, section 4.6</a>.
   *
   * <ul>
   *
   * <li>If {@code null} is supplied, {@code null} is returned.</li>
   *
   * <li>If a {@link Class} is supplied, the {@link Class} is returned.</li>
   *
   * <li>If a {@link ParameterizedType} is supplied, the result of
   * invoking {@link #erase(Type)} on its {@linkplain
   * ParameterizedType#getRawType() raw type} is returned.</li>
   *
   * <li>If a {@link GenericArrayType} is supplied, the result of
   * invoking {@link Object#getClass()} on an invocation of {@link
   * Array#newInstance(Class, int)} with the return value of an
   * invocation of {@link #erase(Type)} on its {@linkplain
   * GenericArrayType#getGenericComponentType() generic component
   * type} and {@code 0} as its arguments is returned.</li>
   *
   * <li>If a {@link TypeVariable} is supplied, the result of invoking
   * {@link #erase(Type)} <strong>on its {@linkplain
   * TypeVariable#getBounds() first (leftmost) bound}</strong> is
   * returned (if it has one) or {@link Object Object.class} if it
   * does not.  <strong>Any other bounds are ignored.</strong></li>
   *
   * <li>If a {@link WildcardType} is supplied, the result of invoking
   * {@link #erase(Type)} <strong>on its {@linkplain
   * WildcardType#getUpperBounds() first upper bound}</strong> is
   * returned.  <strong>Any other bounds are ignored.</strong></li>
   *
   * </ul>
   *
   * @param type the {@link Type} for which the corresponding type
   * erasure is to be returned; may be {@code null} in which case
   * {@code null} will be returned
   *
   * @return a {@link Class}, or {@code null} if a suitable type
   * erasure could not be determined
   *
   * @nullability This method may return {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by mutltiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final Class<?> erase(final Type type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.6
    // 4.6. Type Erasure
    //
    // Type erasure is a mapping from types (possibly including
    // parameterized types and type variables) to types (that are
    // never parameterized types or type variables). We write |T| for
    // the erasure of type T. The erasure mapping is defined as
    // follows:
    //
    // The erasure of a parameterized type (§4.5) G<T1,...,Tn> is |G|.
    //
    // The erasure of a nested type T.C is |T|.C.
    //
    // The erasure of an array type T[] is |T|[].
    //
    // The erasure of a type variable (§4.4) is the erasure of its
    // leftmost bound.
    //
    // The erasure of every other type is the type itself.
    final Class<?> returnValue;
    if (type == null) {
      return null;
    } else if (type instanceof Class<?> c) {
      return erase(c);
    } else if (type instanceof ParameterizedType p) {
      return erase(p);
    } else if (type instanceof GenericArrayType g) {
      return erase(g);
    } else if (type instanceof TypeVariable<?> tv) {
      return erase(tv);
    } else if (type instanceof WildcardType w) {
      return erase(w);
    } else {
      return null;
    }
  }

  private static final Class<?> erase(final Class<?> type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.6
    // …
    // The erasure of a nested type T.C is
    // |T|.C. [Class.getDeclaringClass() returns an already erased
    // type.]
    //
    // The erasure of an array type T[] is |T|[]. [A Class that is an
    // array has a Class as its component type, and that is already
    // erased.]
    // …
    // The erasure of every other type is the type itself. [So in all
    // cases we can just return the supplied Class<?>.]
    return type;
  }

  private static final Class<?> erase(final ParameterizedType type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.6
    // …
    // The erasure of a parameterized type (§4.5) G<T1,...,Tn> is |G|
    // [|G| means the erasure of G, i.e. the erasure of
    // type.getRawType()].
    return erase(type.getRawType());
  }

  private static final Class<?> erase(final GenericArrayType type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.6
    //
    // The erasure of an array type T[] is |T|[]. [|T| means the
    // erasure of T. We erase the genericComponentType() and use
    // Class#arrayType() to find the "normal" array class.]
    final Class<?> candidate = erase(type.getGenericComponentType());
    if (candidate == null) {
      return null;
    } else {
      assert !candidate.isArray(); // it's the component type
      final Class<?> returnValue = candidate.arrayType();
      assert returnValue.isArray();
      return returnValue;
    }
  }

  private static final Class<?> erase(final TypeVariable<?> type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.6
    //
    // The erasure of a type variable (§4.4) is the erasure of its
    // leftmost bound. [In the case of a TypeVariable<?> that returns
    // multiple bounds, we know they will start with a class, not an
    // interface and not a type variable.]
    final Type[] bounds = type.getBounds();
    return bounds != null && bounds.length > 0 ? erase(bounds[0]) : Object.class;
  }

  private static final Class<?> erase(final WildcardType type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.6
    //
    // The erasure of a type variable (§4.4) is the erasure of its
    // leftmost bound.  [WildcardTypes aren't really in the JLS per se
    // but they behave like type variables. Only upper bounds will
    // matter here.]
    final Type[] bounds = type.getUpperBounds();
    return bounds != null && bounds.length > 0 ? erase(bounds[0]) : Object.class;
  }
  
  /**
   * Tests two {@link Type}s for equality in a manner that is
   * independent of their implementations.
   *
   * @param type0 the first {@link Type}; may be {@code null}
   *
   * @param type1 the second {@link Type}; may be {@code null}
   *
   * @return {@code true} if the two {@link Type}s are equal; {@code
   * false} otherwise
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final boolean equals(final Type type0, final Type type1) {
    if (type0 == null) {
      return type1 == null;
    } else if (type1 == null) {
      return false;
    } else if (type0 == type1) {
      return true;
    } else if (type0 instanceof Class<?> c0) {
      return type1 instanceof Class<?> c1 && equals(c0, c1);
    } else if (type0 instanceof ParameterizedType p0) {
      return type1 instanceof ParameterizedType p1 && equals(p0, p1);
    } else if (type0 instanceof GenericArrayType g0) {
      return type1 instanceof GenericArrayType g1 && equals(g0, g1);
    } else if (type0 instanceof TypeVariable<?> tv0) {
      return type1 instanceof TypeVariable<?> tv1 && equals(tv0, tv1);
    } else if (type0 instanceof WildcardType w0) {
      return type1 instanceof WildcardType w1 && equals(w0, w1);
    } else {
      return Objects.equals(type0, type1);
    }
  }

  private static final boolean equals(final Type[] ts0, final Type[] ts1) {
    if (ts0 == null) {
      return ts1 == null;
    } else if (ts1 == null) {
      return false;
    } else if (ts0 == ts1) {
      return true;
    } else if (ts0.length == ts1.length) {
      for (final Type t0 : ts0) {
        for (final Type t1 : ts1) {
          if (!equals(t0, t1)) {
            return false;
          }
        }
      }
      return true;
    } else {
      return false;
    }
  }

  private static final boolean equals(final Class<?> c0, final Class<?> c1) {
    return Objects.equals(c0, c1);
  }

  private static final boolean equals(final ParameterizedType pt0, final ParameterizedType pt1) {
    if (pt0 == null) {
      return pt1 == null;
    } else if (pt1 == null) {
      return false;
    } else if (pt0 == pt1) {
      return true;
    } else {
      return
        equals(pt0.getOwnerType(), pt1.getOwnerType()) &&
        equals(pt0.getRawType(), pt1.getRawType()) &&
        equals(pt0.getActualTypeArguments(), pt1.getActualTypeArguments());
    }
  }

  private static final boolean equals(final GenericArrayType ga0, final GenericArrayType ga1) {
    if (ga0 == null) {
      return ga1 == null;
    } else if (ga1 == null) {
      return false;
    } else if (ga0 == ga1) {
      return true;
    } else {
      return equals(ga0.getGenericComponentType(), ga1.getGenericComponentType());
    }
  }

  private static final boolean equals(final TypeVariable<?> tv0, final TypeVariable<?> tv1) {
    if (tv0 == null) {
      return tv1 == null;
    } else if (tv1 == null) {
      return false;
    } else if (tv0 == tv1) {
      return true;
    } else {
      return
        equals(tv0.getGenericDeclaration(), tv1.getGenericDeclaration()) &&
        Objects.equals(tv0.getName(), tv1.getName());
    }
  }

  private static final boolean equals(final GenericDeclaration gd0, final GenericDeclaration gd1) {
    if (gd0 == null) {
      return gd1 == null;
    } else if (gd1 == null) {
      return false;
    } else if (gd0 == gd1) {
      return true;
    } else if (gd0 instanceof Class<?> c0) {
      return gd1 instanceof Class<?> c1 && equals(c0, c1);
    } else if (gd0 instanceof Executable e0) {
      return gd1 instanceof Executable e1 && equals(e0, e1);
    } else {
      return Objects.equals(gd0, gd1);
    }
  }

  private static final boolean equals(final Executable e0, final Executable e1) {
    if (e0 == null) {
      return e1 == null;
    } else if (e1 == null) {
      return false;
    } else if (e0 == e1) {
      return true;
    } else if (e0 instanceof Constructor<?> c0) {
      return e1 instanceof Constructor<?> c1 && equals(c0, c1);
    } else if (e0 instanceof Method m0) {
      return e1 instanceof Method m1 && equals(m0, m1);
    } else {
      return Objects.equals(e0, e1);
    }
  }

  private static final boolean equals(final Constructor<?> c0, final Constructor<?> c1) {
    return Objects.equals(c0, c1);
  }

  private static final boolean equals(final Method m0, final Method m1) {
    return Objects.equals(m0, m1);
  }

  private static final boolean equals(final WildcardType w0, final WildcardType w1) {
    if (w0 == null) {
      return w1 == null;
    } else if (w1 == null) {
      return false;
    } else if (w0 == w1) {
      return true;
    } else {
      return
        equals(w0.getLowerBounds(), w1.getLowerBounds()) &&
        equals(w0.getUpperBounds(), w1.getUpperBounds());
    }
  }

  /**
   * Computes and returns a hashcode for the supplied {@link Type} independent of
   * its implementation.
   *
   * @param type the {@link Type} in question; may be {@code null} in
   * which case {@code 0} will be returned
   *
   * @return a hashcode for the supplied {@link Type}
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final int hashCode(final Type type) {
    if (type == null) {
      return 0;
    } else if (type instanceof Class<?> c) {
      return hashCode(c);
    } else if (type instanceof ParameterizedType p) {
      return hashCode(p);
    } else if (type instanceof GenericArrayType g) {
      return hashCode(g);
    } else if (type instanceof TypeVariable<?> tv) {
      return hashCode(tv);
    } else if (type instanceof WildcardType w) {
      return hashCode(w);
    } else {
      return type.hashCode();
    }
  }

  private static final int hashCode(final Class<?> type) {
    return type == null ? 0 : type.hashCode();
  }

  private static final int hashCode(final ParameterizedType type) {
    if (type == null) {
      return 0;
    } else {
      return Arrays.hashCode(type.getActualTypeArguments()) ^ hashCode(type.getOwnerType()) ^ hashCode(type.getRawType());
    }
  }

  private static final int hashCode(final GenericArrayType type) {
    return type == null ? 0 : Objects.hashCode(type.getGenericComponentType());
  }

  private static final int hashCode(final TypeVariable<?> type) {
    return type == null ? 0 : type.hashCode();
  }

  private static final int hashCode(final WildcardType type) {
    return type == null ? 0 : Arrays.hashCode(type.getUpperBounds()) ^ Arrays.hashCode(type.getLowerBounds());
  }

  /**
   * Returns a {@link String} representation of the supplied {@link
   * Type} that is independent of its implementation.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @param type the {@link Type} in question; may be {@code null} in which
   * case "{@code null}" will be returned
   *
   * @return a non-{@code null} {@link String} representation of the
   * supplied {@link Type}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final String toString(final Type type) {
    if (type == null) {
      return "null";
    } else if (type instanceof Class<?> c) {
      return toString(c);
    } else if (type instanceof ParameterizedType p) {
      return toString(p);
    } else if (type instanceof GenericArrayType g) {
      return toString(g);
    } else if (type instanceof TypeVariable<?> tv) {
      return toString(tv);
    } else if (type instanceof WildcardType w) {
      return toString(w);
    } else {
      return type.getTypeName();
    }
  }

  private static final String toString(final Class<?> c) {
    return c == null ? "null" : c.getTypeName();
  }

  private static final String toString(final ParameterizedType ptype) {
    if (ptype == null) {
      return "null";
    } else {
      final StringBuilder sb = new StringBuilder();
      final Type ownerType = ptype.getOwnerType();
      if (ownerType == null) {
        sb.append(toString(ptype.getRawType()));
      } else {
        sb.append(toString(ownerType)).append("$");
        final Type rawType = ptype.getRawType();
        if (ownerType instanceof ParameterizedType p) {
          sb.append(toString(rawType).replace(toString(p.getRawType()) + "$", ""));
        } else if (rawType instanceof Class<?> c) {
          sb.append(c.getSimpleName());
        } else {
          sb.append(toString(rawType));
        }
      }
      final Type[] actualTypeArguments = ptype.getActualTypeArguments();
      if (actualTypeArguments != null && actualTypeArguments.length > 0) {
        final StringJoiner stringJoiner = new StringJoiner(", ", "<", ">");
        stringJoiner.setEmptyValue("");
        for (final Type actualTypeArgument : actualTypeArguments) {
          stringJoiner.add(toString(actualTypeArgument));
        }
        sb.append(stringJoiner.toString());
      }
      return sb.toString();
    }
  }

  private static final String toString(final GenericArrayType gatype) {
    if (gatype == null) {
      return "null";
    } else {
      return toString(gatype.getGenericComponentType()) + "[]";
    }
  }

  private static final String toString(final TypeVariable<?> tv) {
    return tv == null ? "null" : tv.getName();
  }

  private static final String toString(final WildcardType wc) {
    if (wc == null) {
      return "null";
    } else {
      final StringBuilder sb = new StringBuilder("?");
      Type[] bounds = wc.getLowerBounds();
      if (bounds == null || bounds.length <= 0) {
        // Upper bounds only.
        bounds = wc.getUpperBounds();
        if (bounds == null || bounds.length <= 0 || Object.class.equals(bounds[0])) {
          bounds = null;
        } else {
          sb.append(" extends ");
        }
      } else {
        // Lower bounds only.
        sb.append(" super ");
      }
      if (bounds != null) {
        assert bounds.length > 0;
        final StringJoiner sj = new StringJoiner(" & ");
        for (final Type bound: bounds) {
          sj.add(String.valueOf(toString(bound)));
        }
        sb.append(sj.toString());
      }
      return sb.toString();
    }
  }

}
