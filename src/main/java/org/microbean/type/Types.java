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

import java.io.Serializable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.microbean.development.annotation.Experimental;
import org.microbean.development.annotation.Incomplete;

/**
 * A hub for Java {@link Type}-related operations.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #toTypes(Type)
 *
 * @threadsafety Instances of this class are safe for concurrent use
 * by multiple threads.
 */
public final class Types {


  /*
   * Static fields.
   */


  private static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

  static final Comparator<Type> typeComparator = new TypeComparator();

  private static final Map<Class<?>, Class<?>> wrapperTypes;

  static {
    final Map<Class<?>, Class<?>> map = new IdentityHashMap<>(9);
    map.put(boolean.class, Boolean.class);
    map.put(byte.class, Byte.class);
    map.put(char.class, Character.class);
    map.put(double.class, Double.class);
    map.put(float.class, Float.class);
    map.put(int.class, Integer.class);
    map.put(long.class, Long.class);
    map.put(short.class, Short.class);
    map.put(void.class, Void.class);
    wrapperTypes = Collections.unmodifiableMap(map);
  }


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link Types}.
   */
  private Types() {
    super();
  }


  /*
   * Methods.
   */


  /**
   * For the vast majority of cases, returns the supplied {@link Type}
   * unchanged, but in the case where a {@linkplain #isGeneric(Class)
   * generic} {@link Class} is supplied, returns an equivalent {@link
   * ParameterizedType} whose {@linkplain
   * ParameterizedType#getActualTypeArguments() actual type arguments}
   * are not resolved.
   *
   * @param type the {@link Type} to normalize; may be {@code null} in
   * which case {@code null} will be returned; will often be returned
   * unchanged
   *
   * @return the normalized {@link Type}; {@code null} if {@code type}
   * was {@code null}
   *
   * @nullability This method may return {@code null} only if the
   * supplied {@link Type} is {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final Type normalize(final Type type) {
    if (type instanceof Class) {
      final Class<?> cls = (Class<?>)type;
      if (isGeneric(cls)) {
        return new DefaultParameterizedType(cls.getDeclaringClass(), cls, cls.getTypeParameters());
      } else {
        final Type componentType = cls.getComponentType();
        if (componentType == null) {
          return cls;
        } else {
          final Type normalizedComponentType = normalize(componentType); // NOTE: recursive
          return componentType == normalizedComponentType ? cls : array(normalizedComponentType);
        }
      }
    } else {
      return type;
    }
  }

  public static final boolean isArrayType(final Type type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-10.html#jls-10.1
    // 10.1 Array Types
    // …
    // The element type of an array may be any type, whether primitive
    // or reference.
    // …
    // [That rules out wildcards, but nothing else.]
    return type instanceof Class ? ((Class<?>)type).isArray() : type instanceof GenericArrayType;
  }

  public static final Type getComponentType(Type type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-10.html#jls-10
    // Chapter 10. Arrays
    // …
    // An array object contains a number of variables. The number of
    // variables may be zero, in which case the array is said to be
    // empty. The variables contained in an array have no names;
    // instead they are referenced by array access expressions that
    // use non-negative integer index values. These variables are
    // called the components of the array. If an array has 𝘯
    // components, we say 𝘯 is the length of the array; the components
    // of the array are referenced using integer indices from 0 to 𝘯 -
    // 1, inclusive.
    //
    // All the components of an array have the same type, called the
    // component type of the array. If the component type of an array
    // is T, then the type of the array itself is written T[].
    // …
    if (type instanceof Class) {
      return ((Class<?>)type).getComponentType();
    } else if (type instanceof GenericArrayType) {
      return ((GenericArrayType)type).getGenericComponentType();
    } else {
      return null;
    }
  }

  public static final Type getElementType(final Type type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-10.html#jls-10
    // Chapter 10. Arrays
    // …
    // The component type of an array may itself be an array type. The
    // components of such an array may contain references to
    // subarrays. If, starting from any array type, one considers its
    // component type, and then (if that is also an array type) the
    // component type of that type, and so on, eventually one must
    // reach a component type that is not an array type; this is
    // called the element type of the original array, and the
    // components at this level of the data structure are called the
    // elements of the original array.
    // …
    Type elementType = null;
    Type componentType = getComponentType(type);
    while (componentType != null) {
      elementType = componentType;
      componentType = getComponentType(componentType);
    }
    return elementType;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Type} is
   * a <em>raw type</em> according to <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.8"
   * target="_parent">the rules of the Java Language
   * Specification, section 4.8</a>.
   *
   * <p>This method returns {@code true} if and only if:</p>
   *
   * <ul>
   *
   * <li>The supplied {@link Type} is an instance of {@link Class},
   * <strong>and any</strong> of the following conditions is true:
   *
   * <ul>
   *
   * <li>The return value of invoking this method on the {@linkplain
   * Class class}'s {@linkplain Class#getComponentType() component
   * type} is {@code true}</li>
   *
   * <li>The {@linkplain Class class} {@linkplain
   * Class#getTypeParameters() has type parameters}</li>
   *
   * <li>The {@linkplain Class class} is a non-static member class of
   * a {@linkplain Class#getDeclaringClass() declaring class} that
   * {@linkplain #isGeneric(Type) is generic}</li>
   *
   * </ul>
   *
   * </li>
   *
   * </ul>
   *
   * @param type the {@link Type} in question; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if and only if the supplied {@link Type} is
   * a raw {@link Class}
   *
   * @see <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.8"
   * target="_parent">The Java Language Specification, section 4.8</a>
   */
  public static final boolean isRawType(final Type type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.8
    // 4.8. Raw Types
    //
    // To facilitate interfacing with non-generic legacy code, it is
    // possible to use as a type the erasure (§4.6) of a parameterized
    // type (§4.5) or the erasure of an array type (§10.1) whose
    // element type is a parameterized type. Such a type is called a
    // raw type. [The erasure "of an array type (§10.1) whose element
    // type is a parameterized type" is exactly the return value of
    // erase(someGenericArrayType).]
    //
    // More precisely, a raw type is defined to be one of:
    //
    // The reference type that is formed by taking the name of a
    // generic type declaration without an accompanying type argument
    // list. [More simply: a non-null Class where
    // getTypeParameters().length ≤ 0.  This is exactly the
    // isGeneric(type) test.]
    //
    // An array type whose element type is a raw
    // type. [isRawType(getElementType(type))]
    //
    // A non-static member type of a raw type R that is not inherited
    // from a superclass or superinterface of R. [A Class whose
    // getDeclaringClass() returns a "raw type R" or a
    // ParameterizedType whose getOwnerType() returns a "raw type R".]
    //
    // A non-generic class or interface type is not a raw type.  [We
    // covered this already.]
    if (type == null) {
      return false;
    } else if (type instanceof Class) {
      return isRawType((Class<?>)type);
    } else if (type instanceof ParameterizedType) {
      return isRawType((ParameterizedType)type);
    } else if (type instanceof GenericArrayType) {
      return isRawType((GenericArrayType)type);
    } else {
      return false;
    }
  }

  private static final boolean isRawType(final Class<?> c) {
    // We use getDeclaringClass() here as the third test rather than
    // getEnclosingClass() because getDeclaringClass() skips anonymous
    // class definitions, which are not capable of being generic, and
    // therefore cannot be raw types, and therefore cannot house a
    // "non-static member type of a raw type R".
    return c != null && (isGeneric(c) || isRawType(getElementType(c)) || isGeneric(c.getDeclaringClass()));
  }

  private static final boolean isRawType(final ParameterizedType type) {
    if (type == null) {
      return false;
    } else {
      final Type ownerType = type.getOwnerType();
      if (ownerType == null || ownerType instanceof ParameterizedType) {
        // ownerType is not a "raw type R" so type is not a "non-static
        // member type" of it
        return false;
      } else if (ownerType instanceof Class) {
        assert !((Class<?>)ownerType).isArray();
        // Don't call isRawType(); that would recurse and we only want
        // to check one level up.
        return isGeneric(ownerType);
      } else {
        throw new AssertionError("Unexpected ownerType: " + ownerType);
      }
    }
  }

  private static final boolean isRawType(final GenericArrayType type) {
    return isRawType(getElementType(type));
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Type} is
   * <em>generic</em> according to <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-8.html#jls-8.1.2"
   * target="_parent">the rules of the Java Language
   * Specification, section 8.1.2</a>.
   *
   * <p>Only {@link Class}es can be generic. This method will return
   * {@code false} when passed any other kind of {@link Type}.</p>
   *
   * @param type the {@link Type} in question; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if the supplied {@link Type} is an instance
   * of {@link Class} and returns a non-{@code null} array with more
   * than zero elements from an invocation of its {@link
   * Class#getTypeParameters()} method; {@code false} otherwise
   *
   * @see Class#getTypeParameters()
   *
   * @see <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-8.html#jls-8.1.2"
   * target="_parent">The Java Language Specification section
   * 8.1.2</a>
   */
  public static final boolean isGeneric(final Type type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-8.html#jls-8.1.2
    // 8.1.2. Generic Classes and Type Parameters
    //
    // A class is generic if it declares one or more type variables (§4.4).
    return type instanceof Class && ((Class<?>)type).getTypeParameters().length > 0;
  }

  private static final boolean isGeneric(final Class<?> cls) {
    return cls != null && cls.getTypeParameters().length > 0;
  }

  public static final boolean isGenericInterface(final Type type) {
    if (type instanceof Class) {
      final Class<?> cls = (Class<?>)type;
      return cls.isInterface() && cls.getTypeParameters().length > 0;
    } else {
      return false;
    }
  }

  private static final boolean isGenericInterface(final Class<?> cls) {
    return cls != null && cls.isInterface() && cls.getTypeParameters().length > 0;
  }

  public static final boolean isInstanceOfClass(final Type type) {
    return type instanceof Class;
  }

  private static final boolean isInstanceOfClass(final Class<?> cls) {
    return cls != null;
  }

  public static final boolean isNonInterfaceClassType(final Type type) {
    return type instanceof Class && !((Class<?>)type).isInterface();
  }

  private static final boolean isNonInterfaceClassType(final Class<?> cls) {
    return cls != null && !cls.isInterface();
  }

  public static final boolean isInterface(final Type type) {
    return type instanceof Class && ((Class<?>)type).isInterface();
  }

  private static final boolean isInterface(final Class<?> cls) {
    return cls != null && cls.isInterface();
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Type}
   * represents a <em>reference type</em> according to <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.3"
   * target="_parent">the rules of the Java Language
   * Specification, section 4.3</a>.
   *
   * <p>This method is implemented as if by {@code !isPrimitive(type)
   * && !isWildcard(type)}.</p>
   *
   * @param type the {@link Type} in question; may be {@code null} in which case
   * {@code false} will be returned
   *
   * @return {@code true} if and only if the supplied {@link Type}
   * represents a <em>reference type</em> according to <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.3"
   * target="_parent">the rules of the Java Language
   * Specification, section 4.3</a>
   *
   * @see <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.3"
   * target="_parent">The Java Language Specification section 4.3</a>
   *
   * @see #isPrimitive(Type)
   *
   * @see #isWildcard(Type)
   */
  public static final boolean isReferenceType(final Type type) {
    // Wildcards are ruled out by the BNF in
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.5.1
    return !isPrimitive(type) && !isWildcard(type);
  }

  public static final boolean isWildcard(final Type type) {
    return type instanceof WildcardType;
  }

  private static final boolean provablyDistinct(final Type type0, final Type type1) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.5.1
    // 4.5.1. Type Arguments of Parameterized Types
    //
    // Two type arguments are provably distinct if one of the
    // following is true:
    //
    // Neither argument is a type variable or wildcard, and the two
    // arguments are not the same type.
    //
    // One type argument is a type variable or wildcard, with an upper
    // bound (from capture conversion (§5.1.10), if necessary) of S;
    // and the other type argument T is not a type variable or
    // wildcard; and neither |S| <: |T| nor |T| <: |S| (§4.8,
    // §4.10). [|T| means the erasure of T.]
    //
    // Each type argument is a type variable or wildcard, with upper
    // bounds (from capture conversion, if necessary) of S and T; and
    // neither |S| <: |T| nor |T| <: |S|.

    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10
    // 4.10. Subtyping
    //
    // The subtype and supertype relations are binary relations on types.
    //
    // The supertypes of a type are obtained by reflexive and
    // transitive closure over the direct supertype relation, written
    // S >₁ T, which is defined by rules given later in this
    // section. We write S :> T to indicate that the supertype
    // relation holds between S and T.
    //
    // S is a proper supertype of T, written S > T, if S :> T and S ≠ T.
    //
    // The subtypes of a type T are all types U such that T is a
    // supertype of U, and the null type. We write T <: S to indicate
    // that that the subtype relation holds between types T and S.
    //
    // T is a proper subtype of S, written T < S, if T <: S and S ≠ T.
    //
    // T is a direct subtype of S, written T <₁ S, if S >₁ T.
    //
    // Subtyping does not extend through parameterized types: T <: S
    // does not imply that C<T> <: C<S>.
    if (type0 instanceof FreshTypeVariable ||
        type0 instanceof IntersectionType ||
        type0 instanceof TypeVariable ||
        type0 instanceof WildcardType ||
        type1 instanceof FreshTypeVariable ||
        type1 instanceof IntersectionType ||
        type1 instanceof TypeVariable ||
        type1 instanceof WildcardType) {
      final Class<?> c0 = erase(type0);
      final Class<?> c1 = erase(type1);
      return !isSubtype(c0, c1) && !isSubtype(c1, c0);
    } else {
      // …
      // Neither argument is a type variable or wildcard, and the two
      // arguments are not the same type.
      return !equals(type0, type1);
    }
  }

  static final boolean isSupertype(final Type s, final Type t) {
    if (s == null) {
      // The null type is the supertype of no other type.
      return false;
    } else if (t == null || equals(s, t)) {
      // The null type is a subtype of all other types.
      // Supertype is a reflexive relation.
      return true;
    } else if (s instanceof Class && t instanceof Class) {
      // Let the JDK's native bits handle this for us.
      return ((Class<?>)s).isAssignableFrom((Class<?>)t);
    } else {
      final Type[] supertypes = getSupertypes(t); // getSupertypes() is already transitive
      for (final Type supertype : supertypes) {
        if (equals(s, supertype)) {
          return true;
        }
      }
      return false;
    }
  }

  private static final boolean isProperSupertype(final Type s, final Type t) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10
    // 4.10. Subtyping
    // …
    // We write S :> T to indicate that the supertype
    // relation holds between S and T.
    //
    // S is a proper supertype of T, written S > T, if S :> T and S ≠ T.
    // …
    return !equals(s, t) && isSupertype(s, t);
  }

  /**
   * Returns {@code true} if {@code s} is a <em>direct supertype</em>
   * of {@code t}, according to <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10"
   * target="_parent">the rules of the Java Language Specification,
   * section 4.10</a>.
   *
   * @param s the purported direct supertype
   *
   * @param t the purported subtype
   *
   * @return {@code true} if {@code s} is a <em>direct supertype</em>
   * of {@code t}, according to <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10"
   * target="_parent">the rules of the Java Language Specification,
   * section 4.10</a>; {@code false} otherwise
   *
   * @see <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10">The
   * Java Language Specification, section 4.10</a>
   *
   * @see #getDirectSupertypes(Type)
   */
  private static final boolean isDirectSupertype(final Type s, final Type t) {
    for (final Type directSupertype : getDirectSupertypes(t)) {
      if (equals(directSupertype, s)) {
        return true;
      }
    }
    return false;
  }

  private static final boolean isSubtype(final Type u, final Type t) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10
    // 4.10. Subtyping
    // …
    // The subtypes of a type T are all types U such that T is a
    // supertype of U, and the null type.
    return u == null || isSupertype(t, u);
  }

 private static final boolean isProperSubtype(final Type t, final Type s) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10
    // 4.10. Subtyping
    // …
    // The subtypes of a type T are all types U such that T is a
    // supertype of U, and the null type. We write T <: S to indicate
    // that that the subtype relation holds between types T and S.
    //
    // T is a proper subtype of S, written T < S, if T <: S and S ≠ T.
    // …
    // Subtyping does not extend through parameterized types: T <: S
    // does not imply that C<T> <: C<S>.
    return isSubtype(t, s) && !equals(t, s);
  }

  private static final boolean isDirectSubtype(final Type t, final Type s) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10
    // 4.10. Subtyping
    // …
    // The supertypes of a type are obtained by reflexive and
    // transitive closure over the direct supertype relation, written
    // S >₁ T, which is defined by rules given later in this
    // section.
    // …
    // T is a direct subtype of S, written T <₁ S, if S >₁ T.
    return isDirectSupertype(s, t);
  }


  /*
   * Bookmark: resolve(Type)
   */


  /**
   * Resolves the supplied {@link Type} and returns the result, or
   * the {@link Type} itself if the {@link Type} could not be resolved.
   *
   * <p>Type resolution is delegated to the supplied {@link Function}
   * which is most commonly a method reference to the {@link
   * Map#get(Object)} method of a {@link Map Map&lt;Type, Type&gt;}.
   * Generally speaking, however, the type resolver may take any
   * side-effect-free action it wishes.</p>
   *
   * @param type the {@link Type} to resolve; may be {@code null}
   *
   * @param typeResolver the {@link Function} whose {@link
   * Function#apply(Object)} method will be called on, among other
   * things, {@link ParameterizedType} {@linkplain
   * ParameterizedType#getActualTypeArguments() type arguments}; must
   * not be {@code null}; may return {@code null}; will never be
   * supplied with a {@code null} {@link Type}
   *
   * @return the resolved {@link Type}, or {@code null}
   *
   * @exception NullPointerException if {@code typeResolver} is {@code
   * null}
   *
   * @nullability This method will return {@code null} only when the
   * supplied {@link Type} is {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final Type resolve(final Type type,
                                   final Function<? super Type, ? extends Type> typeResolver) {
    if (type == null) {
      return null;
    } else if (type instanceof Class) {
      return resolve((Class<?>)type, typeResolver);
    } else if (type instanceof ParameterizedType) {
      return resolve((ParameterizedType)type, typeResolver);
    } else if (type instanceof GenericArrayType) {
      return resolve((GenericArrayType)type, typeResolver);
    } else if (type instanceof FreshTypeVariable) {
      return resolve((FreshTypeVariable)type, typeResolver);
    } else if (type instanceof IntersectionType) {
      return resolve((IntersectionType)type, typeResolver);
    } else if (type instanceof TypeVariable) {
      return resolve((TypeVariable<?>)type, typeResolver);
    } else if (type instanceof WildcardType) {
      return resolve((WildcardType)type, typeResolver);
    } else {
      // (Unknown type, so we don't know how to resolve it.)
      return type;
    }
  }

  private static final Type resolve(final Class<?> type,
                                    final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    return candidate == null ? type : candidate;
  }

  private static final Type resolve(final ParameterizedType type,
                                    final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    if (candidate == null) {
      final Type[] actualTypeArguments = type.getActualTypeArguments();
      final int length = actualTypeArguments.length;
      final Type[] resolvedActualTypeArguments = new Type[length];
      boolean createNewType = false;
      for (int i = 0; i < length; i++) {
        final Type actualTypeArgument = actualTypeArguments[i];
        final Type resolvedActualTypeArgument = resolve(actualTypeArgument, typeResolver); // NOTE: recursive
        resolvedActualTypeArguments[i] = resolvedActualTypeArgument;
        if (!createNewType && actualTypeArgument != resolvedActualTypeArgument) {
          // If they're not the same object reference, then resolution
          // did something, so we have to return a new type, not the
          // one we were handed.
          createNewType = true;
        }
      }
      return
        createNewType ? new DefaultParameterizedType(type.getOwnerType(), type.getRawType(), resolvedActualTypeArguments) : type;
    } else {
      return candidate;
    }

  }

  private static final Type resolve(final GenericArrayType type,
                                    final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    if (candidate == null) {
      final Type genericComponentType = type.getGenericComponentType();
      final Type resolvedComponentType = resolve(genericComponentType, typeResolver); // NOTE: recursive
      if (resolvedComponentType == null || resolvedComponentType == genericComponentType) {
        // Identity means that basically resolution was a no-op, so the
        // genericComponentType was whatever it was, and so we are going
        // to be a no-op as well.
        return type;
      } else if (resolvedComponentType instanceof Class) {
        // This might happen when genericComponentType was a
        // TypeVariable.  In this case, it might get resolved to a
        // simple scalar (e.g. Integer.class).  Now we have,
        // effectively, a GenericArrayType whose genericComponentType is
        // just a plain class.  That's actually just an ordinary Java
        // array, so "resolve" this type by returning its array
        // equivalent.
        return array(resolvedComponentType);
      } else {
        // If we get here, we know that resolution actually did
        // something, so return a new GenericArrayType implementation
        // whose component type is the resolved version of the
        // original's.
        return new DefaultGenericArrayType(resolvedComponentType);
      }
    } else {
      return candidate;
    }
  }

  private static final Type resolve(final FreshTypeVariable type,
                                    final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    return candidate == null ? type : candidate;
  }

  private static final Type resolve(final IntersectionType type,
                                    final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    return candidate == null ? type : candidate;
  }


  private static final Type resolve(final TypeVariable<?> type,
                                    final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    return candidate == null ? type : candidate;
  }

  private static final Type resolve(final WildcardType type,
                                    final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    return candidate == null ? type : candidate;
  }


  /*
   * Bookmark: toTypes(Type)
   */


  /**
   * Returns a non-{@code null}, {@linkplain
   * Collections#unmodifiableSet(Set) unmodifiable <code>Set</code>}
   * of {@link Type}s, each element of which is a {@link Type} which
   * any instance bearing the supplied {@link Type} will implement.
   *
   * <p>The {@link Set} that is returned is guaranteed to not contain
   * {@link WildcardType} or {@link TypeVariable} instances.</p>
   *
   * @param type the {@link Type} whose type closure should be
   * computed; may be {@code null}
   *
   * @return a non-{@code null} {@link TypeSet}; the supplied {@link
   * Type} will be one of its elements unless it is a {@link
   * TypeVariable} or a {@link WildcardType}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent.
   */
  public static final TypeSet toTypes(final Type type) {
    return toTypes(type, null);
  }

  /**
   * Returns a non-{@code null}, {@linkplain
   * Collections#unmodifiableSet(Set) unmodifiable <code>Set</code>}
   * of {@link Type}s, each element of which is a {@link Type} which
   * any instance bearing the supplied {@link Type} will implement.
   *
   * <p>The {@link Set} that is returned is guaranteed to not contain
   * {@link WildcardType} or {@link TypeVariable} instances.</p>
   *
   * <p>If the supplied {@link Predicate} returns {@code true} from an
   * invocation of its {@link Predicate#test(Object)} method, then the
   * {@link Type} that was passed to it will not be contained by the
   * returned {@link TypeSet}.</p>
   *
   * @param type the {@link Type} whose type closure should be
   * computed; may be {@code null}
   *
   * @param removalPredicate a {@link Predicate} used to selectively
   * remove some <strong>resolved</strong> {@link Type}s from the computed
   *  {@link TypeSet}; may be {@code null}
   *
   * @return a non-{@code null} {@link TypeSet}, filtered by the
   * supplied {@link Predicate}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent.
   */
  public static final TypeSet toTypes(final Type type, final Predicate<? super Type> removalPredicate) {
    // Keys will be Class or TypeVariable<?> instances and nothing
    // else.
    final Map<Type, Type> resolvedTypes = new HashMap<>();
    toTypes(type, isRawType(type), resolvedTypes);
    if (removalPredicate == null) {
      resolvedTypes.keySet().removeIf(Predicate.not(Types::isInstanceOfClass));
    } else {
      resolvedTypes.keySet().removeIf(k -> !isInstanceOfClass(k) || removalPredicate.test(resolvedTypes.get(k)));
    }
    return resolvedTypes.isEmpty() ? TypeSet.EMPTY_TYPESET : new TypeSet(resolvedTypes.values());
  }

  private static final void toTypes(final Type type,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    if (type == null) {
      // Do nothing on purpose
    } else if (type instanceof Class) {
      toTypes((Class<?>)type, noParameterizedTypes, resolvedTypes);
    } else if (type instanceof ParameterizedType) {
      toTypes((ParameterizedType)type, noParameterizedTypes, resolvedTypes);
    } else if (type instanceof GenericArrayType) {
      toTypes((GenericArrayType)type, noParameterizedTypes, resolvedTypes);
    } else if (type instanceof FreshTypeVariable) {
      toTypes((FreshTypeVariable)type, noParameterizedTypes, resolvedTypes);
    } else if (type instanceof IntersectionType) {
      toTypes((IntersectionType)type, noParameterizedTypes, resolvedTypes);
    } else if (type instanceof TypeVariable) {
      toTypes((TypeVariable<?>)type, noParameterizedTypes, resolvedTypes);
    } else if (type instanceof WildcardType) {
      toTypes((WildcardType)type, noParameterizedTypes, resolvedTypes);
    } else {
      throw new IllegalArgumentException("Unexpected type: " + type);
    }
  }

  private static final void toTypes(final Class<?> cls,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    resolvedTypes.put(cls, resolve(cls, resolvedTypes::get));
    final Type superclass = noParameterizedTypes ? cls.getSuperclass() : cls.getGenericSuperclass();
    if (superclass != null) {
      toTypes(superclass, noParameterizedTypes || isRawType(superclass), resolvedTypes);
    }
    final Type[] interfaces = noParameterizedTypes ? cls.getInterfaces() : cls.getGenericInterfaces();
    for (final Type iface : interfaces) {
      toTypes(iface, noParameterizedTypes || isRawType(iface), resolvedTypes);
    }
  }

  private static final void toTypes(final ParameterizedType parameterizedType,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    final Class<?> erasure = erase(parameterizedType);
    if (erasure != null) {
      if (!noParameterizedTypes) {
        final TypeVariable<?>[] typeVariables = erasure.getTypeParameters();
        final Type[] typeArguments = parameterizedType.getActualTypeArguments();
        assert typeVariables.length == typeArguments.length;
        for (int i = 0; i < typeVariables.length; i++) {
          resolvedTypes.put(typeVariables[i], resolve(typeArguments[i], resolvedTypes::get));
        }
      }
      resolvedTypes.put(erasure, resolve(parameterizedType, resolvedTypes::get));
      toTypes(erasure, noParameterizedTypes, resolvedTypes);
    }
  }

  private static final void toTypes(final GenericArrayType genericArrayType,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    final Class<?> erasure = erase(genericArrayType);
    if (erasure != null) {
      assert erasure.isArray() : "Not an array type; check erase(GenericArrayType): " + erasure;
      resolvedTypes.put(erasure, genericArrayType);
      toTypes(erasure, noParameterizedTypes, resolvedTypes);
    }
  }

  private static final void toTypes(final FreshTypeVariable freshTypeVariable,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    // Do nothing on purpose.
  }


  private static final void toTypes(final IntersectionType intersectionType,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    // Do nothing on purpose.
  }


  private static final void toTypes(final TypeVariable<?> typeVariable,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    // Do nothing on purpose.
  }

  private static final void toTypes(final WildcardType wildcardType,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    // Do nothing on purpose.
  }

  /**
   * Returns the result of <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-5.html#jls-5.1.7">boxing
   * conversion</a> on the supplied {@link Type}.
   *
   * <p>In most cases this means the supplied {@link Type} is returned
   * unchanged.</p>
   *
   * <p>This method calls {@link #isPrimitive(Type)} as part of its
   * execution.</p>
   *
   * @param <T> the kind of {@link Type} in question
   *
   * @param type the {@link Type} to test
   *
   * @return the result of <a
   * href="https://docs.oracle.com/javase/specs/jls/se13/html/jls-5.html#jls-5.1.7">boxing
   * conversion</a> on the supplied {@link Type}
   *
   * @nullability This method will return {@code null} if {@code type}
   * is {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @see #isPrimitive(Type)
   *
   * @see <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-5.html#jls-5.1.7">The
   * Java Language Specification section 5.1.7</a>
   */
  @SuppressWarnings("unchecked")
  public static final <T extends Type> T box(final T type) {
    return
      type instanceof Class && ((Class<?>)type).isPrimitive() ?
      (T)wrapperTypes.getOrDefault(type, (Class<?>)type) :
      type;
  }

  /**
   * Returns {@code true} if and only if {@code type} is a {@link
   * Class} and {@linkplain Class#isPrimitive() is primitive}.
   *
   * <h2>Design Notes</h2>
   *
   * <p>This prosaic method exists because various {@link Predicate}s
   * need to exist that test this very thing, and making it {@code
   * public} does no harm.</p>
   *
   * @param type the {@link Type} to test; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if and only if {@code type} is a {@link
   * Class} and {@linkplain Class#isPrimitive() is primitive}
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final boolean isPrimitive(final Type type) {
    return type instanceof Class && ((Class<?>)type).isPrimitive();
  }

  /**
   * Returns a new array of {@link Type}s containing the type erasures
   * for the supplied {@link Type}s according to <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.6"
   * target="_parent">the rules of the Java Language Specification,
   * section 4.6</a>.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @param types the {@link Type}s to erase; may be {@code null}
   *
   * @return a new array of the same length as the supplied {@code
   * types} array that contains type erasures
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #erase(Type)
   */
  public static final Type[] erase(final Type[] types) {
    if (types == null || types.length <= 0) {
      return emptyTypeArray();
    } else {
      final Type[] erasedTypes = new Type[types.length];
      for (int i = 0; i < types.length; i++) {
        erasedTypes[i] = erase(types[i]);
      }
      return erasedTypes;
    }
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
    } else if (type instanceof Class) {
      return erase((Class<?>)type);
    } else if (type instanceof ParameterizedType) {
      return erase((ParameterizedType)type);
    } else if (type instanceof GenericArrayType) {
      return erase((GenericArrayType)type);
    } else if (type instanceof FreshTypeVariable) {
      return erase((FreshTypeVariable)type);
    } else if (type instanceof IntersectionType) {
      return erase((IntersectionType)type);
    } else if (type instanceof TypeVariable) {
      return erase((TypeVariable<?>)type);
    } else if (type instanceof WildcardType) {
      return erase((WildcardType)type);
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
    // reflection to find the "normal" array class.]
    final Class<?> candidate = erase(type.getGenericComponentType());
    if (candidate == null) {
      return null;
    } else {
      assert !candidate.isArray(); // it's the component type
      return array(candidate);
    }
  }

  private static final Class<?> erase(final FreshTypeVariable type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.6
    //
    // The erasure of a type variable (§4.4) is the erasure of its
    // leftmost bound. [In the case of a FreshTypeVariable that
    // returns multiple bounds, we know they will start with a class,
    // not an interface and not a type variable.]
    return erase(type.getUpperBound());
  }

  private static final Class<?> erase(final IntersectionType type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.6
    //
    // The erasure of a type variable (§4.4) is the erasure of its
    // leftmost bound. [In the case of a FreshTypeVariable that
    // returns multiple bounds, we know they will start with a class,
    // not an interface and not a type variable.]
    final Type[] bounds = type.getBounds();
    return bounds != null && bounds.length > 0 ? erase(bounds[0]) : Object.class;
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

  public static final boolean isUnboundedWildcard(final Type type) {
    return isUpperBoundedWildcard(type) && ((WildcardType)type).getUpperBounds()[0] == Object.class;
  }

  private static final boolean isUnboundedWildcard(final WildcardType type) {
    return isUpperBoundedWildcard(type) && type.getUpperBounds()[0] == Object.class;
  }

  private static final boolean isUnboundedWildcard(final UnboundedWildcardType type) {
    return type != null;
  }

  public static final boolean isUpperBoundedWildcard(final Type type) {
    return type instanceof WildcardType && ((WildcardType)type).getLowerBounds().length <= 0;
  }

  private static final boolean isUpperBoundedWildcard(final WildcardType type) {
    return type != null && type.getLowerBounds().length <= 0;
  }

  private static final boolean isUpperBoundedWildcard(final UpperBoundedWildcardType type) {
    return type != null;
  }

  public static final boolean isLowerBoundedWildcard(final Type type) {
    return type instanceof WildcardType && ((WildcardType)type).getLowerBounds().length > 0;
  }

  private static final boolean isLowerBoundedWildcard(final WildcardType type) {
    return type != null && type.getLowerBounds().length > 0;
  }

  private static final boolean isLowerBoundedWildcard(final LowerBoundedWildcardType type) {
    return type != null;
  }

  static final Type getSoleUpperBound(final WildcardType type) {
    return type.getUpperBounds()[0];
  }

  static final Type getSoleLowerBound(final WildcardType type) {
    final Type[] lowerBounds = type.getLowerBounds();
    return lowerBounds.length <= 0 ? null : lowerBounds[0];
  }

  private static final boolean contains(final Type t1, final Type t2) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.5.1
    // 4.5.1. Type Arguments of Parameterized Types
    // …
    // A type argument T₁ is said to contain
    // another type argument T₂, written T₂ <= T₁, if the set of types
    // denoted by T₂ is provably a subset of the set of types denoted
    // by T₁ under the reflexive and transitive closure of the
    // following rules (where <: denotes subtyping (§4.10)):
    //
    // ? extends T <= ? extends S if T <: S [if T is a subtype of S, then ? extends S contains ? extends T]
    // ? extends T <= ? [? contains ? extends T]
    // ? super T <= ? super S if S <: T [if S is a subtype of T, then ? super S contains ? super T]
    // ? super T <= ? [? contains ? super T]
    // ? super T <= ? extends Object [? extends Object contains ? super T]
    // T <= T [T contains T; a reference type can only contain itself because it is only one type argument]
    // T <= ? extends T [? extends T contains T]
    // T <= ? super T) [? super T contains T]
    if (t1 == null || t2 == null) {
      return false;
    } else if (equals(t1, t2)) {
      // T <= T [T contains T]
      return true;
    } else if (t1 instanceof WildcardType) {
      if (t2 instanceof WildcardType) {
        return contains((WildcardType)t1, (WildcardType)t2);
      } else if (isReferenceType(t2)) {
        return contains((WildcardType)t1, t2);
      } else {
        throw new IllegalArgumentException("t2 is neither a WildcardType nor a reference type: " + t2);
      }
    } else if (isReferenceType(t1)) {
      if (t2 instanceof WildcardType || isReferenceType(t2)) {
        return false; // …because we already tested for equality and a reference type can only contain itself
      } else {
        throw new IllegalArgumentException("t2 is neither a WildcardType nor a reference type: " + t2);
      }
    } else {
      throw new IllegalArgumentException("t1 is neither a WildcardType nor a reference type: " + t1);
    }
  }

  private static final boolean contains(final WildcardType t1, final Type t2) {
    assert t1 != null;
    assert t2 != null;
    assert isReferenceType(t2);
    assert !equals(t1, t2);
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.5.1
    // 4.5.1. Type Arguments of Parameterized Types
    // …
    // A type argument T₁ is said to contain
    // another type argument T₂, written T₂ <= T₁, if the set of types
    // denoted by T₂ is provably a subset of the set of types denoted
    // by T₁ under the reflexive and transitive closure of the
    // following rules (where <: denotes subtyping (§4.10)):
    //
    // …
    // T <= ? extends T [? extends T contains T]
    // T <= ? super T) [? super T contains T]
    // …
    final Type[] t1UpperBounds = t1.getUpperBounds();
    final Type t1UpperBound = t1UpperBounds == null || t1UpperBounds.length <= 0 ? Object.class : t1UpperBounds[0];
    assert t1UpperBounds.length == 0 || t1UpperBounds.length == 1 : "Unexpected upper bounds: " + toString(t1UpperBounds);
    if (equals(t1UpperBound, t2)) {
      return true;
    } else {
      final Type[] t1LowerBounds = t1.getLowerBounds();
      assert t1LowerBounds.length == 0 || t1LowerBounds.length == 1 : "Unexpected lower bounds: " + toString(t1LowerBounds);
      final Type t1LowerBound = t1LowerBounds == null || t1LowerBounds.length <= 0 ? null : t1LowerBounds[0];
      return equals(t1LowerBound, t2);
    }
  }

  private static final boolean contains(final WildcardType t1s, final WildcardType t2t) {
    assert t1s != null;
    assert t2t != null;
    assert !equals(t1s, t2t);
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.5.1
    // 4.5.1. Type Arguments of Parameterized Types
    // …
    // A type argument T₁ is said to contain
    // another type argument T₂, written T₂ <= T₁, if the set of types
    // denoted by T₂ is provably a subset of the set of types denoted
    // by T₁ under the reflexive and transitive closure of the
    // following rules (where <: denotes subtyping (§4.10)):
    // …
    final Type[] t1sUpperBounds = t1s.getUpperBounds();
    assert t1sUpperBounds.length == 0 || t1sUpperBounds.length == 1 : "Unexpected upper bounds: " + toString(t1sUpperBounds);
    final Type t1sUpperBound = t1sUpperBounds == null || t1sUpperBounds.length <= 0 ? Object.class : t1sUpperBounds[0];
    final Type[] t2tUpperBounds = t2t.getUpperBounds();
    assert t2tUpperBounds.length == 0 || t2tUpperBounds.length == 1 : "Unexpected upper bounds: " + toString(t2tUpperBounds);
    final Type t2tUpperBound = t2tUpperBounds == null || t2tUpperBounds.length <= 0 ? Object.class : t2tUpperBounds[0];
    if (t1sUpperBound == Object.class ||
        isSubtype(t2tUpperBound, t1sUpperBound)) {
      // …
      // ? extends T <= ? extends S if T <: S [if T is a subtype of S, then ? extends S contains ? extends T]
      // ? extends T <= ? [? contains ? extends T because Object.class, ?'s default upper bound, contains every reference type]
      // …
      // ? super T <= ? [? contains ? super T because Object.class, ?'s default upper bound, contains every reference type]
      // …
      // ? super T <= ? extends Object [? extends Object contains ? super T because ? extends Object is the same as ?]
      // …
      return true;
    } else {
      final Type[] t1sLowerBounds = t1s.getLowerBounds();
      assert t1sLowerBounds.length == 0 || t1sLowerBounds.length == 1 : "Unexpected lower bounds: " + toString(t1sLowerBounds);
      final Type t1sLowerBound = t1sLowerBounds == null || t1sLowerBounds.length <= 0 ? null : t1sLowerBounds[0];
      final Type[] t2tLowerBounds = t2t.getLowerBounds();
      assert t2tLowerBounds.length == 0 || t2tLowerBounds.length == 1 : "Unexpected lower bounds: " + toString(t2tLowerBounds);
      final Type t2tLowerBound = t2tLowerBounds == null || t2tLowerBounds.length <= 0 ? null : t2tLowerBounds[0];
      // …
      // ? super T <= ? super S if S <: T [if S is a subtype of T, then ? super S contains ? super T]
      // …
      return isSubtype(t1sLowerBound, t2tLowerBound);
    }
  }

  static final Type[] getContainingTypeArguments(final Type type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.5.1
    // 4.5.1. Type Arguments of Parameterized Types
    // …
    // A type argument T₁ is said to contain
    // another type argument T₂, written T₂ <= T₁, if the set of types
    // denoted by T₂ is provably a subset of the set of types denoted
    // by T₁ under the reflexive and transitive closure of the
    // following rules (where <: denotes subtyping (§4.10)):
    //
    // ? extends T <= ? extends S if T <: S [if T is a subtype of S, then ? extends S contains ? extends T]
    // ? extends T <= ? [? contains ? extends T]
    // ? super T <= ? super S if S <: T [if S is a subtype of T, then ? super S contains ? super T]
    // ? super T <= ? [? contains ? super T]
    // ? super T <= ? extends Object [? extends Object contains ? super T]
    // T <= T [T contains T; a reference type can only contain itself because it is only one type argument]
    // T <= ? extends T [? extends T contains T]
    // T <= ? super T) [? super T contains T]
    if (Objects.requireNonNull(type, "type") == Object.class) {
      return new Type[] { Object.class, new LowerBoundedWildcardType(Object.class), UnboundedWildcardType.INSTANCE };
    } else {
      final Collection<TypeWrapper> typeWrappers = new ArrayList<>();
      getContainingTypeArguments(type, t -> {
          final TypeWrapper wrapper = new TypeWrapper(t);
          if (typeWrappers.contains(wrapper)) {
            return false;
          } else {
            return typeWrappers.add(wrapper);
          }
        });
      /*
      final SortedSet<TypeWrapper> typeWrappers = new TreeSet<>();
      getContainingTypeArguments(type, t -> typeWrappers.add(new TypeWrapper(t)));
      */
      typeWrappers.add(new TypeWrapper(UnboundedWildcardType.INSTANCE));
      return typeWrappers.isEmpty() ? emptyTypeArray() : typeWrappers.stream().map(tw -> tw.t).toArray(Type[]::new);
      // return types.isEmpty() ? emptyTypeArray() : types.toArray(new Type[types.size()]);
    }
  }

  private static final void getContainingTypeArguments(final Type type,
                                                       final Predicate<? super Type> adder) {
    assert type != null;
    assert type != Object.class;
    if (type instanceof WildcardType) {
      getContainingTypeArguments((WildcardType)type, adder);
    } else if (adder.test(type)) { // T <= T
      getContainingTypeArguments(new UpperBoundedWildcardType(type), adder); // NOTE: recursive
      final boolean added = adder.test(new LowerBoundedWildcardType(type));
      assert added;
    }
  }

  private static final void getContainingTypeArguments(final WildcardType type,
                                                       final Predicate<? super Type> adder) {
    final Object[] lowerBounds = type.getLowerBounds();
    if (lowerBounds.length <= 0) {
      final Type[] upperBounds = type.getUpperBounds();
      assert upperBounds.length == 1;
      final Type upperBound = upperBounds[0];
      assert upperBound != null;
      assert !(upperBound instanceof WildcardType) : "Upper bound was somehow a WildcardType: " + toString(upperBound);
      if (upperBound != Object.class &&
          adder.test(type)) { // T <= T
        for (final Type upperBoundSupertype : getSupertypes(upperBound)) {
          assert upperBoundSupertype != null;
          if (upperBoundSupertype != Object.class && !equals(upperBound, upperBoundSupertype)) {
            adder.test(new UpperBoundedWildcardType(upperBoundSupertype));
          }
        }
        /*
        for (final Type upperBoundDirectSupertype : getDirectSupertypes(upperBound, true)) {
          assert upperBoundDirectSupertype != null;
          if (upperBoundDirectSupertype != Object.class && !Types.equals(upperBound, upperBoundDirectSupertype)) {
            getContainingTypeArguments(new UpperBoundedWildcardType(upperBoundDirectSupertype), adder); // NOTE: recursive
          }
        }
        */
      }
    }
  }

  static final Type[] getSupertypes(final Type type) {
    final TypeWrapperSet supertypes = new TypeWrapperSet();
    getSupertypes(type, supertypes, new TypeWrapperSet());
    return supertypes.toTypeArray();
  }

  private static final void getSupertypes(final Type type, final TypeWrapperSet supertypes, final TypeWrapperSet seen) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10
    // 4.10. Subtyping
    // …
    // The supertypes of a type are obtained by reflexive and
    // transitive closure over the direct supertype relation [see
    // getDirectSupertypes()], written S >₁ T, which is defined by
    // rules given later in this section.
    if (type != null) {
      supertypes.add(type);
      if (seen.add(type)) {
        final Type[] directSupertypes = getDirectSupertypes(type);
        for (final Type directSupertype : directSupertypes) {
          getSupertypes(directSupertype, supertypes, seen);
        }
      }
    }
  }

  static final Type getDirectSuperclassType(final Type type) {
    // JLS 17 nomenclature
    return getDirectSuperclass(type);
  }

  static final Type getDirectSuperclass(final Type type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-8.html#jls-8.1.4
    // 8.1.4. Superclasses and Subclasses
    //
    // The optional extends clause in a normal class declaration
    // specifies the direct superclass of the current class.
    // …
    // Given a (possibly generic) class declaration C<F₁,…Fₙ> (𝘯 ≥ 0)
    // [sic; e.g. the parameter list can be empty], C ≠ Object), the
    // direct superclass of the class type C<F₁,…Fₙ> is the type given
    // in the extends clause of the declaration of C if an extends
    // clause is present, or Object otherwise.
    if (type == null) {
      return null;
    } else if (type instanceof Class) {
      return getDirectSuperclass((Class<?>)type);
    } else {
      return null;
    }
  }

  static final Type getDirectSuperclassType(final Class<?> cls) {
    // JLS 17 nomenclature
    return getDirectSuperclass(cls);
  }

  static final Type getDirectSuperclass(final Class<?> cls) {
    return cls == null ? null : cls.getGenericSuperclass();
  }

  static final Type[] getDirectSupertypes(final Type type) {
    return getDirectSupertypes(type, true);
  }

  private static final Type[] getDirectSupertypes(final Type type, final boolean includeContainingTypeArguments) {
    if (type == null) {
      // The direct supertypes of the null type are all reference
      // types other than the null type itself. [There's no way to
      // represent an array of infinite size so we use an empty one
      // instead.]
      return emptyTypeArray();
    } else if (type instanceof Class) {
      return getDirectSupertypes((Class<?>)type, includeContainingTypeArguments);
    } else if (type instanceof ParameterizedType) {
      return getDirectSupertypes((ParameterizedType)type, includeContainingTypeArguments);
    } else if (type instanceof GenericArrayType) {
      return getDirectSupertypes((GenericArrayType)type, includeContainingTypeArguments);
    } else if (type instanceof FreshTypeVariable) {
      // Private type
      return getDirectSupertypes((FreshTypeVariable)type, includeContainingTypeArguments);
    } else if (type instanceof IntersectionType) {
      // Private type
      return getDirectSupertypes((IntersectionType)type, includeContainingTypeArguments);
    } else if (type instanceof TypeVariable) {
      return getDirectSupertypes((TypeVariable<?>)type, includeContainingTypeArguments);
    } else if (type instanceof WildcardType) {
      return getDirectSupertypes((WildcardType)type, includeContainingTypeArguments);
    } else {
      return emptyTypeArray();
    }
  }

  private static final Type[] getDirectSupertypes(final Class<?> type) {
    return getDirectSupertypes(type, true);
  }

  private static final Type[] getDirectSupertypes(final Class<?> type, final boolean includeContainingTypeArguments) {
    if (type == null || type == Object.class || type == boolean.class || type == double.class) {
      // The direct supertypes of the null type are all reference
      // types other than the null type itself. [There's no way to
      // represent an array of infinite size so we use an empty one
      // instead.]
      return emptyTypeArray();
    } else {
      final Class<?> ct = type.getComponentType();
      if (ct == null) {
        if (type.isPrimitive()) {
          // 4.10.1. Subtyping among Primitive Types
          //
          // The following rules define the direct supertype relation
          // [>₁] among the primitive types:
          //
          // double >₁ float
          // float >₁ long
          // long >₁ int
          // int >₁ char
          // int >₁ short
          // short >₁ byte
          if (type == float.class) {
            return new Type[] { double.class };
          } else if (type == long.class) {
            return new Type[] { float.class };
          } else if (type == int.class) {
            return new Type[] { long.class };
          } else if (type == char.class) {
            return new Type[] { int.class };
          } else if (type == short.class) {
            return new Type[] { int.class };
          } else if (type == byte.class) {
            return new Type[] { short.class };
          } else {
            throw new AssertionError("unhandled primitive type: " + type);
          }
        } else {
          // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.2
          // 4.10.2. Subtyping among [non-array] Class and Interface
          // Types [which includes TypeVariables, incidentally]
          //
          // Given a non-generic type declaration C the direct
          // supertypes of the type C are all of the following:
          //
          // * The direct superclass of C (§8.1.4) [extends clause]
          // * The direct superinterfaces of C (§8.1.5) [implements
          //   clause]
          // * The type Object, if C is an interface type with no
          //   direct superinterfaces (§9.1.3)
          //
          // Given a generic type declaration C<F₁,…Fₙ> (𝘯 > 0) the
          // direct supertypes of the raw type C (§4.8) are all of the
          // following:
          //
          // * The direct superclass of the raw type C. [JLS 17
          //   changes this to: "The erasure (§4.6) of the direct
          //   superclass type of C, if C is a class [i.e., not an
          //   interface].]
          //
          // * The direct superinterfaces of the raw type C. [JLS 17
          //   changes this to: "The erasure (§4.6) of the direct
          //   superinterface types of C.]
          //
          // * The type Object, if C<F₁,…Fₙ> is a generic interface
          //   type with no direct superinterfaces (§9.1.3)
          // […]
          final Type[] returnValue;
          final Type directSuperclass = erase(getDirectSuperclass(type));
          assert (directSuperclass == null ? type.isInterface() : directSuperclass instanceof Class) : directSuperclass == null ? "Unexpected class; should have been an interface: " + toString(type) : "Unexpected type erasure of direct superclass: " + toString(directSuperclass);
          final Type[] directSuperinterfaces = erase(type.getGenericInterfaces());
          if (directSuperinterfaces.length <= 0) {
            if (directSuperclass == null) {
              assert type.isInterface() : "Unexpected class; should have been an interface: " + toString(type);
              returnValue = new Type[] { Object.class };
            } else {
              returnValue = new Type[] { directSuperclass };
            }
          } else {
            final int offset = directSuperclass == null ? 0 : 1;
            returnValue = new Type[directSuperinterfaces.length + offset];
            System.arraycopy(directSuperinterfaces, 0, returnValue, offset, directSuperinterfaces.length);
            if (offset == 1) {
              returnValue[0] = directSuperclass;
            }
          }
          return returnValue;
        }
      } else if (ct == Object.class || ct.isPrimitive()) {
        // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.3
        // 4.10.3. Subtyping among Array Types
        // […]
        // * Object >₁ Object[] [>₁ is the direct supertype relation]
        // * Cloneable >₁ Object[]
        // * java.io.Serializable >₁ Object[]
        // * If P is a primitive type, then:
        //   * Object >₁ P[]
        //   * Cloneable >₁ P[]
        //   * java.io.Serializable >₁ P[]
        // […]
        // [It is not clear if the order is significant; probably not,
        // but we follow the order found in the JLS all the same.  It
        // is worth noting that this order is repeated in the JLS in
        // at least two places.]
        return new Type[] { Object.class, Cloneable.class, Serializable.class };
      } else {
        // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.3
        // 4.10.3. Subtyping among Array Types
        //
        // The following rules define the direct supertype relation
        // [>₁] among array types:
        //
        // * If S and T are both reference types, then
        //   S[] >₁ T[] iff S >₁ T.
        // […]
        // [We already handled the case where T is Object, i.e. where
        // T has no direct superclass.]
        final Type[] componentTypeDirectSupertypes = getDirectSupertypes(ct, includeContainingTypeArguments);
        assert componentTypeDirectSupertypes.length > 0 : "Unexpected zero length direct supertypes for " + ct;
        final Type[] returnValue = new Type[componentTypeDirectSupertypes.length];
        for (int i = 0; i < componentTypeDirectSupertypes.length; i++) {
          final Type cdst = componentTypeDirectSupertypes[i];
          assert (cdst instanceof Class || cdst instanceof ParameterizedType) : "Unexpected direct supertype: " + cdst;
          returnValue[i] = array(cdst);
        }
        return returnValue;
      }
    }
  }

  private static final Type[] getDirectSupertypes(ParameterizedType type, final boolean includeContainingTypeArguments) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.2
    // 4.10. Subtyping
    // […]
    // Subtyping does not extend through parameterized types: T <: S
    // [<: means "is a subtype of"] does not imply that C<T> <: C<S>.
    // […]
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.2
    // 4.10.2. Subtyping among Class and Interface Types
    // […]
    // Given a generic type declaration C<F₁,…,Fₙ> (𝘯 > 0), the
    // direct supertypes of the parameterized type C<T₁,…,Tₙ>, where
    // Tᵢ (1 ≤ 𝘪 ≤ 𝘯) is a type, are all of the following:
    //
    // D<U₁ θ,…,Uₖ θ>, where D<U₁,…,Uₖ> is a generic type which is a
    // direct supertype of the generic type C<F₁,…,Fₙ> and θ [theta]
    // is the substitution [F₁:=T₁,…,Fₙ:=Tₙ]. [See also
    // https://stackoverflow.com/questions/69502823/in-the-java-language-specification-version-11-section-4-10-2-how-do-i-read-u%e2%82%96.]
    //
    // (https://docs.oracle.com/javase/specs/jls/se11/html/jls-1.html#jls-1.3
    // 1.3. Notation
    // […]
    // The type system of the Java programming language occasionally
    // relies on the notion of a substitution. The notation
    // [F₁:=T₁,…,Fₙ:=Tₙ] denotes substitution of Fᵢ by Tᵢ for
    // 1 ≤ 𝘪 ≤ 𝘯.)
    //
    // C<S₁,…,Sₙ>, where Sᵢ contains Tᵢ (1 ≤ 𝘪 ≤ 𝘯)
    // (§4.5.1). [i.e. this puts synthetic wildcard-containing
    // ParameterizedTypes into the direct supertypes list]
    //
    // (https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.5.1
    // 4.5.1. Type Arguments of Parameterized Types
    // …
    // A type argument T₁ is said to contain
    // another type argument T₂, written T₂ <= T₁, if the set of types
    // denoted by T₂ is provably a subset of the set of types denoted
    // by T₁ under the reflexive and transitive closure of the
    // following rules (where <: denotes subtyping (§4.10)):
    //
    // ? extends T <= ? extends S if T <: S [if S contains T, then ? extends S contains ? extends T]
    // ? extends T <= ? [? contains ? extends T]
    // ? super T <= ? super S if S <: T [if T contains S, then ? super S contains ? super T]
    // ? super T <= ? [? contains ? super T]
    // ? super T <= ? extends Object [? extends Object contains ? super T]
    // T <= T [T contains T]
    // T <= ? extends T [? extends T contains T]
    // T <= ? super T) [? super T contains T])
    //
    // The type Object, if C<F₁,…,Fₙ> is a generic interface type
    // with no direct superinterfaces.
    //
    // The raw type C.
    // […]
    if (type == null) {
      // The direct supertypes of the null type are all reference
      // types other than the null type itself. [There's no way to
      // represent an array of infinite size so we use an empty one instead.]
      return emptyTypeArray();
    } else {

      // If the type has no wildcards, capture conversion will do
      // nothing (at the expense of a couple of array creations and a
      // few assignments into those arrays; these arrays will get
      // thrown away in this case) and will return the type as-is.  If
      // the type has wildcards, capture conversion will be applied
      // and a new type will be returned.
      final ParameterizedType oldType = type;
      type = applyCaptureConversion(oldType);
      assert (containsWildcard(oldType, false) ? type != oldType : type == oldType) : "oldType: " + toString(oldType) + "; type: " + toString(type);

      // See
      // https://stackoverflow.com/questions/69502823/in-the-java-language-specification-version-11-section-4-10-2-how-do-i-read-u%e2%82%96

      final List<Type> directSupertypes = new ArrayList<>();

      final Type ownerType = type.getOwnerType();

      final Type C = type.getRawType();
      assert C instanceof Class : "Unexpected raw type C: " + toString(C);
      assert isGeneric((Class<?>)C) : "Unexpected non-generic rawType: " + toString(C);

      final Type[] directSupertypesOfC = getDirectSupertypes(C);

      final boolean addObject = isInterface(C) && directSupertypesOfC.length <= 0;

      Type[] actualTypeArguments = type.getActualTypeArguments();
      // (It is assumed that cloning of actualTypeArguments takes
      // place as all viable implementations of ParameterizedType
      // should do.)
      assert actualTypeArguments != type.getActualTypeArguments();

      for (final Type directSupertypeOfC : directSupertypesOfC) {
        assert directSupertypeOfC instanceof Class : "Unexpected erasure of direct supertype of " + toString(C) + ": " + toString(directSupertypeOfC);
        directSupertypes.add(theta(getOwnerType(directSupertypeOfC), directSupertypeOfC, actualTypeArguments));
      }

      if (addObject) {
        // The type Object, if C<F₁,…,Fₙ> is a generic interface type
        // with no direct superinterfaces.
        directSupertypes.add(Object.class);
      }

      // The raw type C.
      directSupertypes.add(C);

      if (includeContainingTypeArguments) {

        // C<S₁,…,Sₙ>, where Sᵢ contains Tᵢ (1 ≤ 𝘪 ≤ 𝘯)

        final List<Type[]> containingTypeArguments = new ArrayList<>();
        for (final Type actualTypeArgument : actualTypeArguments) {
          containingTypeArguments.add(getContainingTypeArguments(actualTypeArgument));
        }
        permutations(ts -> directSupertypes.add(new DefaultParameterizedType(ownerType, C, ts)),
                     containingTypeArguments::get,
                     new Type[containingTypeArguments.size()]);

        /*
        for (int i = 0; i < actualTypeArguments.length; i++) {
          final Type Ti = actualTypeArguments[i];
          assert !(Ti instanceof WildcardType) : "Ti was a WildcardType (didn't we do capture conversion already?): " + toString(Ti);
          // TODO: Tentative assertion.
          // assert !(Ti instanceof FreshTypeVariable) : "Ti was a FreshTypeVariable: " + Ti;
          for (final Type Si : getContainingTypeArguments(Ti)) {
            assert (equals(Ti, Si) ? true : Si instanceof WildcardType) : "Si was not a WildcardType: " + toString(Si);
            // System.out.println("Ti <= Si: " + toString(Ti) + " <= " + toString(Si));
            if (!equals(Ti, Si)) {
              // In the JLS, parameterized types are unique among all
              // other types in that they are their own direct
              // supertype.  This is because, given List<String>, some
              // of its supertypes are List<T that contains String>.
              // One example of a T that contains String is T itself
              // (all types contain themselves).  So it follows that
              // List<String> is its own direct supertype.
              //
              // This complicates matters because every other relation
              // having to do with subtyping "goes through"
              // getDirectSupertype() as the spec requires, applying
              // it reflexively as well as transitively.
              //
              // If we allow this one case where List<String> is its
              // own direct supertype, then in getSupertypes() and
              // elsewhere we have to special-case ParameterizedType
              // situations.  That is undesirable.  So we eliminate
              // the reflexivity here.
              actualTypeArguments[i] = Si; // XXX TODO FIXME NO NO NO NO this needs to be combinatorial
            }
          }
        }
        final ParameterizedType newType = new DefaultParameterizedType(type.getOwnerType(), C, actualTypeArguments);
        directSupertypes.add(newType);
        assert actualTypeArguments != newType.getActualTypeArguments();
        */
      }
      return directSupertypes.toArray(new Type[directSupertypes.size()]);
    }
  }

  static final <T> List<T[]> permutations(List<? extends T[]> columnValues, final IntFunction<? extends T[]> constructor) {
    if (columnValues.isEmpty()) {
      return List.of();
    } else {
      columnValues = List.copyOf(columnValues);
      int permutations = 1;
      for (final Object[] x : columnValues) {
        permutations *= x.length;
      }
      final List<T[]> result = new ArrayList<>(permutations);
      final int columnCount = columnValues.size();
      final T[] row = constructor.apply(columnCount);
      if (row.length != columnCount) {
        throw new IllegalArgumentException("constructor: " + constructor);
      }
      permutations(result::add, columnValues::get, row);
      return Collections.unmodifiableList(result);
    }
  }

  private static final <T> void permutations(final Consumer<? super T[]> resultAdder,
                                             final IntFunction<? extends T[]> columnValuesGetter,
                                             final T[] row) { // mutated and reused
    permutations(resultAdder, columnValuesGetter, row, r -> r.clone(), 0);
  }

  private static final <T> void permutations(final Consumer<? super T[]> resultAdder,
                                             final IntFunction<? extends T[]> columnValuesGetter,
                                             final T[] row, // mutated and reused
                                             final UnaryOperator<T[]> rowCloner,
                                             final int columnIndex) {
    if (columnIndex < row.length) {
      for (final T columnValue : columnValuesGetter.apply(columnIndex)) {
        row[columnIndex] = columnValue;
        permutations(resultAdder, columnValuesGetter, row, rowCloner, columnIndex + 1); // NOTE: recursive
      }
    } else {
      final T[] clonedRow = rowCloner.apply(row);
      assert clonedRow != row;
      assert clonedRow.length == row.length;
      resultAdder.accept(clonedRow);
    }
  }

  static final ParameterizedType applyCaptureConversion(final ParameterizedType GT) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-5.html#jls-5.1.10
    // 5.1.10 Capture Conversion
    //
    // Let G name a generic type declaration (§8.1.2, §9.1.2) with 𝘯
    // type parameters A₁…Aₙ with corresponding bounds U₁…Uₙ [𝘯 > 0].
    //
    // There exists a capture conversion from a parameterized type
    // G<T₁…Tₙ>(§4.5) to a [new] parameterized type G<S₁…Sₙ>, where,
    // for 1 ≤ 𝘪 ≤ 𝘯:
    //
    // * If Tᵢ is a wildcard type argument (§4.5.1) of the form ?,
    //   then Sᵢ is a fresh type variable whose upper bound is
    //   Uᵢ[A₁:=S₁,…,Aₙ:=Sₙ] and whose lower bound is the null type
    //   (§4.1).
    //
    // * If Tᵢ is a wildcard type argument of the form ? extends Bᵢ,
    //   then Sᵢ is a fresh type variable whose upper bound is
    //   glb(Bᵢ, Uᵢ[A₁:=S₁,…,Aₙ:=Sₙ]) and whose lower bound is the
    //   null type.
    //
    //   glb(V₁,…,Vₘ) is defined as V₁ & … & Vₘ.
    //
    //   It is a compile-time error if, for any two classes (not
    //   interfaces) Vᵢ and Vⱼ, Vᵢ is not a subclass of Vⱼ or vice
    //   versa.
    //
    // * If Tᵢ is a wildcard type argument of the form ? super Bᵢ,
    //   then Sᵢ is a fresh type variable whose upper bound is
    //   Uᵢ[A₁:=S₁,…,Aₙ:=Sₙ] and whose lower bound is Bᵢ.
    //
    // * Otherwise, Sᵢ = Tᵢ.
    //
    // Capture conversion on any type other than a parameterized type
    // (§4.5) acts as an identity conversion (§5.1.1).
    //
    // Capture conversion is not applied recursively.
    //
    // [Also see https://stackoverflow.com/a/31209735/208288 and
    // https://stackoverflow.com/a/46061947/208288 for great
    // walkthroughs.]
    //
    // [See
    // https://github.com/openjdk/jdk/blob/b870468bdc99938fbb19a41b0ede0a3e3769ace2/src/jdk.compiler/share/classes/com/sun/tools/javac/code/Types.java#L4388-L4456
    // for algorithmic inspiration.]
    final TypeVariable<?>[] A = (TypeVariable<?>[])Types.getTypeParameters(GT);
    if (A.length <= 0) {
      throw new IllegalArgumentException("Not a generic type declaration: " + Types.toString(GT));
    }
    final Type[] T = GT.getActualTypeArguments();
    if (A.length != T.length) {
      throw new IllegalArgumentException("Mismatched lengths; A: " + Types.toString(A) + "; T: " + Types.toString(T));
    }
    final Type[] S = new Type[A.length];
    // Pass 1.  Fill S to start.  Null slots are where capture
    // conversion will happen.  Keep track of them for easy recall
    // later.  If that list is empty, there is no capture conversion
    // to be done.
    final List<Integer> wildcardSlotIndices = new ArrayList<>(T.length);
    for (int i = 0; i < T.length; i++) {
      final Type Ti = T[i];
      if (Ti instanceof WildcardType) {
        // (Leave S[i] null to denote a substitution to be performed
        // in another pass.)
        wildcardSlotIndices.add(Integer.valueOf(i));
      } else {
        // * Otherwise [if Tᵢ is not a wildcard], Sᵢ = Tᵢ.
        S[i] = Ti;
      }
    }
    final ParameterizedType GS;
    if (wildcardSlotIndices.isEmpty()) {
      GS = GT;
    } else {
      // Pass 2; perform capture conversion on the wildcards we found
      // in pass 1.
      for (final Integer wildcardSlotIndex : wildcardSlotIndices) {
        final int i = wildcardSlotIndex.intValue();
        assert S[i] == null;
        final WildcardType Ti = (WildcardType)T[i];
        final Type Bi;
        final Type[] lowerBounds = Ti.getLowerBounds();
        if (lowerBounds.length == 0) {
          Bi = Ti.getUpperBounds()[0];
          if (Bi == Object.class) {
            // * If Tᵢ is a wildcard type argument (§4.5.1) of the
            //   form ?, then Sᵢ is a fresh type variable whose upper
            //   bound is Uᵢ[A₁:=S₁,…,Aₙ:=Sₙ] and whose lower bound is
            //   the null type (§4.1).
            S[i] = new FreshTypeVariable(substitute(i, A, S));
          } else {
            // * If Tᵢ is a wildcard type argument of the form ?
            //   extends Bᵢ, then Sᵢ is a fresh type variable whose
            //   upper bound is glb(Bᵢ, Uᵢ[A₁:=S₁,…,Aₙ:=Sₙ]) and whose
            //   lower bound is the null type.
            //
            //   glb(V₁,…,Vₘ) is defined as V₁ & … & Vₘ.
            S[i] = new FreshTypeVariable(Types.glb(Bi, substitute(i, A, S)));
          }
        } else {
          // * If Tᵢ is a wildcard type argument of the form ? super
          //   Bᵢ, then Sᵢ is a fresh type variable whose upper bound
          //   is Uᵢ[A₁:=S₁,…,Aₙ:=Sₙ] and whose lower bound is Bᵢ.
          Bi = lowerBounds[0];
          S[i] = new FreshTypeVariable(substitute(i, A, S), Bi);
        }
      }
      GS = new DefaultParameterizedType(GT.getOwnerType(), GT.getRawType(), S);
    }
    return GS;
  }

  // Designed to be used solely from the applyCaptureConversion()
  // method immediately above.  Any other usage may result in
  // undefined behavior.
  private static final Type substitute(final int i,
                                       final TypeVariable<?>[] A,
                                       final Type[] S) {
    assert i >= 0 && i < A.length : "i: " + i;
    assert A.length == S.length;
    assert S[i] == null;
    Type Ui = A[i].getBounds()[0];
    for (int n = 0; n < A.length; n++) {
      final Type Sn = S[n];
      if (Sn != null) {
        // Uᵢ[A₁:=S₁,…,Aₙ:=Sₙ] [Uᵢ with the substitution in brackets applied to it]
        Ui = Types.substitute(Ui, A[n], Sn);
        assert Ui != null;
      }
    }
    return Ui;
  }

  // This is a deliberately blunt hammer designed to be used solely
  // from the substitute(int, TypeVariable<?>[], Type[]) method
  // immediately above.  Any other usage may result in undefined
  // behavior.
  static final Type substitute(final Type in, final Type from, final Type to) {
    if (to == null) {
      throw new NullPointerException("to");
    } else if (in == null) {
      return from == null ? to : null;
    } else if (from == null || equals(from, to)) {
      return in;
    } else if (equals(in, from)) {
      return to;
    } else if (in instanceof Class) {
      return substitute((Class<?>)in, from, to);
    } else if (in instanceof ParameterizedType) {
      return substitute((ParameterizedType)in, from, to);
    } else if (in instanceof GenericArrayType) {
      return substitute((GenericArrayType)in, from, to);
    } else if (in instanceof FreshTypeVariable) {
      return substitute((FreshTypeVariable)in, from, to);
    } else if (in instanceof IntersectionType) {
      return substitute((IntersectionType)in, from, to);
    } else if (in instanceof TypeVariable) {
      return substitute((TypeVariable<?>)in, from, to);
    } else if (in instanceof WildcardType) {
      return substitute((WildcardType)in, from, to);
    } else {
      throw new IllegalArgumentException("Unexpected in: " + toString(in));
    }
  }

  private static final <T> Class<T> substitute(final Class<T> in, final Type from, final Type to) {
    assert !equals(in, from);
    assert !equals(from, to);
    return in;
  }

  private static final ParameterizedType substitute(final ParameterizedType in, final Type from, final Type to) {
    return
      substitute(in,
                 Types::emptyTypeArray,
                 in::getActualTypeArguments,
                 (set0, set1) -> new DefaultParameterizedType(in.getOwnerType(), in.getRawType(), set1),
                 from,
                 to);
  }

  private static final GenericArrayType substitute(final GenericArrayType in, final Type from, final Type to) {
    return
      substitute(in,
                 () -> new Type[] { in.getGenericComponentType() },
                 Types::emptyTypeArray,
                 (set0, set1) -> new DefaultGenericArrayType(set0[0]),
                 from,
                 to);
  }

  private static final FreshTypeVariable substitute(final FreshTypeVariable in, final Type from, final Type to) {
    return
      substitute(in,
                 () -> new Type[] { in.getUpperBound() },
                 () -> in.getLowerBound() == null ? emptyTypeArray() : new Type[] { in.getLowerBound() },
                 FreshTypeVariable::new,
                 from,
                 to);
  }

  private static final IntersectionType substitute(final IntersectionType in, final Type from, final Type to) {
    return
      substitute(in,
                 in::getBounds,
                 Types::emptyTypeArray,
                 (set0, set1) -> new IntersectionType(set0),
                 from,
                 to);
  }

  private static final Type substitute(final TypeVariable<?> in, final Type from, final Type to) {
    return
      substitute(in,
                 in::getBounds,
                 Types::emptyTypeArray,
                 FreshTypeVariable::new,
                 from,
                 to);
  }

  private static final WildcardType substitute(final WildcardType in, final Type from, final Type to) {
    return
      substitute(in,
                 in::getUpperBounds,
                 in::getLowerBounds,
                 (set0, set1) -> {
                   if (set0.length <= 0) {
                     assert set0.length == 1;
                     return set0[0] == Object.class ? UnboundedWildcardType.INSTANCE : new UpperBoundedWildcardType(set0);
                   } else {
                     return new LowerBoundedWildcardType(set1);
                   }
                 },
                 from,
                 to);
  }

  // This is a deliberately blunt uncaring hammer and you can hurt
  // yourself with it.
  private static final <T extends Type> T substitute(final T in,
                                                     final Supplier<? extends Type[]> set0,
                                                     final Supplier<? extends Type[]> set1,
                                                     final BiFunction<? super Type[], ? super Type[], ? extends T> out,
                                                     final Type from,
                                                     final Type to) {
    assert !equals(in, from);
    assert !equals(from, to);
    boolean sub = false;
    Type[] old = set0 == null ? emptyTypeArray() : set0.get(); // e.g. (upper) bounds, owner-and-raw-type
    final Type[] newSet0;
    if (old != null && old.length > 0) {
      newSet0 = new Type[old.length];
      for (int i = 0; i < old.length; i++) {
        newSet0[i] = substitute(old[i], from, to);
        if (!sub) {
          sub = newSet0[i] != old[i];
        }
      }
    } else {
      newSet0 = emptyTypeArray();
    }
    old = set1 == null ? emptyTypeArray() : set1.get(); // e.g. lower bounds, actual type arguments
    final Type[] newSet1;
    if (old != null && old.length > 0) {
      newSet1 = new Type[old.length];
      for (int i = 0; i < old.length; i++) {
        newSet1[i] = substitute(old[i], from, to);
        if (!sub) {
          sub = newSet1[i] != old[i];
        }
      }
    } else {
      newSet1 = emptyTypeArray();
    }
    return sub ? out.apply(newSet0, newSet1) : in;
  }

  static final boolean containsWildcard(final Type type) {
    return containsWildcard(type, true);
  }

  static final boolean containsWildcard(final Type type, final boolean recurse) {
    return
      type instanceof WildcardType ||
      type instanceof ParameterizedType && containsWildcard(((ParameterizedType)type).getActualTypeArguments(), recurse);
  }

  static final boolean containsWildcard(final ParameterizedType type) {
    return containsWildcard(type, true);
  }

  static final boolean containsWildcard(final ParameterizedType type, final boolean recurse) {
    return type != null && containsWildcard(type.getActualTypeArguments(), recurse);
  }

  static final boolean containsWildcard(final WildcardType type) {
    return type != null;
  }

  static final boolean containsWildcard(final Type[] types) {
    return containsWildcard(types, true);
  }

  static final boolean containsWildcard(final Type[] types, final boolean recurse) {
    // Useful; see
    // https://stackoverflow.com/questions/69530080/in-the-java-language-specification-version-11-section-4-10-2-is-it-true-that-a
    if (types != null && types.length > 0) {
      if (types instanceof WildcardType[]) {
        for (final Object type : types) {
          if (type != null) {
            return true;
          }
        }
      } else if (recurse) {
        for (final Type type : types) {
          if (type instanceof WildcardType || containsWildcard(type, true)) {
            return true;
          }
        }
      } else {
        for (final Type type : types) {
          if (type instanceof WildcardType) {
            return true;
          }
        }
      }
    }
    return false;
  }

  static final Type[] getTypeParameters(final Type type) {
    if (type == null) {
      return emptyTypeArray();
    } else if (type instanceof Class) {
      return ((Class<?>)type).getTypeParameters();
    } else if (type instanceof ParameterizedType) {
      return getTypeParameters(((ParameterizedType)type).getRawType()); // NOTE: recursive
    } else {
      return emptyTypeArray();
    }
  }

  static final Type[] getTypeParameters(final Class<?> type) {
    return type == null ? emptyTypeArray() : type.getTypeParameters();
  }

  static final Type[] getTypeParameters(final ParameterizedType type) {
    return type == null ? emptyTypeArray() : getTypeParameters(type.getRawType()); // NOTE: recursive
  }

  static final Type getOwnerType(final Type type) {
    if (type instanceof Class) {
      return ((Class<?>)type).getDeclaringClass(); // TODO: or getEnclosingClass()? which also handles anonymous classes?
    } else if (type instanceof ParameterizedType) {
      return ((ParameterizedType)type).getOwnerType();
    } else {
      return null;
    }
  }

  static final Type getOwnerType(final Class<?> cls) {
    return cls == null ? null : cls.getDeclaringClass(); // TODO: or getEnclosingClass()? which also handles anonymous classes?
  }

  static final Type getOwnerType(final ParameterizedType ptype) {
    return ptype == null ? null : ptype.getOwnerType();
  }

  static final Type getRawType(final Type type) {
    return type instanceof ParameterizedType ? ((ParameterizedType)type).getRawType(): null;
  }

  static final Type getRawType(final ParameterizedType type) {
    return type == null ? null : type.getRawType();
  }

  static final Type theta(final Type ownerType, final Type rawType, final Type[] actualTypeArguments) {
    if (rawType instanceof Class) {
      return theta(ownerType, (Class<?>)rawType, actualTypeArguments);
    } else if (rawType instanceof ParameterizedType) {
      return theta(ownerType, ((ParameterizedType)rawType).getRawType(), actualTypeArguments);
    } else {
      throw new IllegalArgumentException("Unexpected rawType: " + rawType);
    }
  }

  static final Type theta(final Type ownerType, final Class<?> cls, final Type[] actualTypeArguments) {
    Objects.requireNonNull(cls, "cls");
    // Not sure we need to accept ownerType.
    assert ownerType == null ? cls.getDeclaringClass() == null : ownerType.equals(cls.getDeclaringClass()) : "Unexpected ownerType: " + toString(ownerType) + "; cls.getDeclaringClass(): " + toString(cls.getDeclaringClass());
    return
      isGeneric(cls) ? new DefaultParameterizedType(ownerType, cls, theta(cls.getTypeParameters(), actualTypeArguments)) : cls;
  }

  static final Type[] theta(final Type[] formalTypeParameters, final Type[] actualTypeArguments) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.2
    // 4.10.2. Subtyping among Class and Interface Types
    // …
    // θ [theta] is the substitution [F₁:=T₁,…,Fₙ:=Tₙ].
    // …
    // [That is: given a formal type parameter Fₙ, replace it with a
    // corresponding actual type argument Tₙ.]
    if (formalTypeParameters == null || actualTypeArguments == null || formalTypeParameters.length <= 0) {
      return emptyTypeArray();
    } else if (formalTypeParameters.length > actualTypeArguments.length) {
      throw new IllegalArgumentException();
    } else {
      final Type[] returnValue = new Type[formalTypeParameters.length];
      for (int i = 0; i < formalTypeParameters.length; i++) {
        returnValue[i] = theta(formalTypeParameters[i], actualTypeArguments[i]);
      }
      return returnValue;
    }
  }

  /**
   * In the context of computing direct supertypes for a {@link
   * ParameterizedType}, ensures {@code formalTypeParameter} is a
   * valid {@link Type} for a {@linkplain Class#getTypeParameters()
   * formal type parameter} (a {@link TypeVariable}), and ensures
   * {@code actualTypeArgument} is a valid {@link Type} for an
   * {@linkplain ParameterizedType#getActualTypeArguments() actual
   * type argument} (anything other than a {@link WildcardType}, and
   * returns {@code actualTypeArgument}, thus implementing the θ
   * substitution defined by <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.2"
   * target="_parent">Section 4.10.2 of The Java Language
   * Specification</a>.
   *
   * @param formalTypeParameter a formal {@linkplain
   * Class#getTypeParameters() type parameter}; must be a {@link TypeVariable}
   *
   * @param actualTypeParameter an {@linkplain
   * ParameterizedType#getActualTypeArguments() actual type argument};
   * must not be {@link null} and must not be a {@link WildcardType}
   *
   * @return actualTypeParameter if both arguments pass validation
   *
   * @exception NullPointerException if either argument is {@code
   * null}
   *
   * @exception IllegalArgumentException if {@code
   * formalTypeParameter} is not a {@link TypeVariable} or if {@code
   * actualTypeParameter} is a {@link WildcardType}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  static final Type theta(final Type formalTypeParameter, final Type actualTypeArgument) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.2
    // 4.10.2. Subtyping among Class and Interface Types
    // …
    // θ [theta] is the substitution [F₁:=T₁,…,Fₙ:=Tₙ].
    // …
    // [That is: given a formal type parameter Fₙ, replace it with a
    // corresponding actual type argument Tₙ.]
    Objects.requireNonNull(formalTypeParameter);
    Objects.requireNonNull(actualTypeArgument);
    if (!(formalTypeParameter instanceof TypeVariable)) {
      throw new IllegalArgumentException("Unexpected type parameter: " + toString(formalTypeParameter));
    } else if (actualTypeArgument instanceof WildcardType) {
      throw new IllegalArgumentException("Unexpected WildcardType type argument: " + toString(actualTypeArgument));
    } else {
      return actualTypeArgument;
    }
  }

  private static final Type[] getDirectSupertypes(final GenericArrayType type, final boolean includeContainingTypeArguments) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.3
    // 4.10.3. Subtyping among Array Types
    //
    // The following rules define the direct supertype relation
    // [>₁] among array types:
    //
    // * If S and T are both reference types, then
    //   S[] >₁ T[] iff S >₁ T.
    // …
    if (type == null) {
      // The direct supertypes of the null type are all reference
      // types other than the null type itself. [There's no way to
      // represent an array of infinite size so we use an empty one
      // instead.]
      return emptyTypeArray();
    } else {
      final Type[] genericComponentTypeDirectSupertypes = getDirectSupertypes(type.getGenericComponentType(), includeContainingTypeArguments);
      final Type[] returnValue = new Type[genericComponentTypeDirectSupertypes.length];
      for (int i = 0; i < genericComponentTypeDirectSupertypes.length; i++) {
        final Type ds = genericComponentTypeDirectSupertypes[i];
        assert (ds instanceof Class ? ds == Object.class || isGeneric(ds) : true) : "Unexpected direct supertype: " + toString(ds);
        returnValue[i] = array(ds);
      }
      return returnValue;
    }
  }

  private static final Type[] getDirectSupertypes(final FreshTypeVariable type, final boolean includeContainingTypeArguments) {
    if (type == null) {
      // The direct supertypes of the null type are all reference
      // types other than the null type itself. [There's no way to
      // represent an array of infinite size so we use an empty one instead.]
      return emptyTypeArray();
    } else {
      // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.2
      // 4.10.2. Subtyping among [non-array] Class and Interface Types
      // […]
      // The direct supertypes of a type variable are the types listed
      // in its bound.
      // […]
      return new Type[] { type.getUpperBound() };
    }
  }

  private static final Type[] getDirectSupertypes(final IntersectionType type, final boolean includeContainingTypeArguments) {
    if (type == null) {
      // The direct supertypes of the null type are all reference
      // types other than the null type itself. [There's no way to
      // represent an array of infinite size so we use an empty one instead.]
      return emptyTypeArray();
    } else {
      // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.2
      // 4.10.2. Subtyping among [non-array] Class and Interface Types
      // […]
      // The direct supertypes of an intersection type T₁ & … & Tₙ are
      // Tᵢ (1 ≤ 𝑖 ≤ 𝘯).
      // […]
      return type.getBounds();
    }
  }

  private static final Type[] getDirectSupertypes(final TypeVariable<?> type, final boolean includeContainingTypeArguments) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.2
    // 4.10.2. Subtyping among [non-array] Class and Interface Types
    // […]
    // The direct supertypes of an intersection type T₁ & … & Tₙ are
    // Tᵢ (1 ≤ 𝑖 ≤ 𝘯).
    //
    // The direct supertypes of a type variable are the types listed
    // in its bound.
    //
    // (Intersection types aren't modeled explicitly but are
    // permissible in TypeVariables.  So these statements appear to
    // translate "down" to the same thing: the direct supertypes of
    // a TypeVariable are simply the contents of its bounds.)
    // […]
    if (type == null) {
      // The direct supertypes of the null type are all reference
      // types other than the null type itself. [There's no way to
      // represent an array of infinite size so we use an empty one instead.]
      return emptyTypeArray();
    } else {
      final Type[] bounds = type.getBounds();
      return bounds == null || bounds.length <= 0 ? new Type[] { Object.class } : bounds;
    }
  }

  @Incomplete
  private static final Type[] getDirectSupertypes(final WildcardType type, final boolean includeContainingTypeArguments) {
    if (type == null) {
      // The direct supertypes of the null type are all reference
      // types other than the null type itself. [There's no way to
      // represent an array of infinite size so we use an empty one instead.]
      return emptyTypeArray();
    } else {
      throw new UnsupportedOperationException("getDirectSupertypes() does not yet handle WildcardTypes (and may never do so): " + type);
    }
  }

  /**
   * Returns the <em>effective bounds</em> of the supplied {@link
   * TypeVariable}.
   *
   * <p>The effective bounds of a {@link TypeVariable} are one of the
   * following:</p>
   *
   * <ul>
   *
   * <li>The (recursive) effective bounds of its sole {@link
   * TypeVariable}-typed bound</li>
   *
   * <li>Its non-{@link TypeVariable}-typed bounds</li>
   *
   * </ul>
   *
   * @param type the {@link TypeVariable} in question; must not be {@code null}
   *
   * @return a non-{@code null} array of {@link Type}s representing
   * the supplied {@link TypeVariable}'s effective bounds with one or
   * more elements
   *
   * @exception NullPointerException if {@code type} is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final Type[] getEffectiveBounds(final TypeVariable<?> type) {
    final Type[] bounds = type.getBounds();
    if (bounds == null || bounds.length <= 0) {
      return new Type[] { Object.class };
    } else if (bounds.length == 1) {
      final Type soleBound = bounds[0];
      if (soleBound instanceof TypeVariable) {
        return getEffectiveBounds((TypeVariable<?>)soleBound);
      } else {
        return new Type[] { soleBound };
      }
    } else {
      return bounds.clone();
    }
  }

  /**
   * Returns a {@link Serializable} {@link Type} that represents the
   * supplied {@link Type}.
   *
   * @param <T> a {@link Serializable} {@link Type}
   *
   * @param type the {@link Type} in question; may be {@code null} in
   * which case {@code null} will be returned
   *
   * @return a {@link Serializable} version of the supplied {@link
   * Type}
   *
   * @exception IllegalArgumentException if {@code type} is non-{@code
   * null} and is not an instance of {@link Serializable}, {@link
   * ParameterizedType}, {@link GenericArrayType}, {@link
   * TypeVariable} or {@link WildcardType}
   *
   * @nullability This method will return {@code null} only when the
   * supplied {@link Type} is {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @SuppressWarnings("unchecked")
  public static final <T extends Serializable & Type> T toSerializableType(final Type type) {
    if (type == null || type instanceof Serializable) {
      return (T)type;
    } else if (type instanceof ParameterizedType) {
      return (T)DefaultParameterizedType.valueOf((ParameterizedType)type);
    } else if (type instanceof GenericArrayType) {
      return (T)DefaultGenericArrayType.valueOf((GenericArrayType)type);
    } else if (type instanceof FreshTypeVariable) {
      throw new IllegalArgumentException("Unexpected type: " + type);
    } else if (type instanceof IntersectionType) {
      throw new IllegalArgumentException("Unexpected type: " + type);
    } else if (type instanceof TypeVariable) {
      return (T)DefaultTypeVariable.valueOf((TypeVariable<? extends GenericDeclaration>)type);
    } else if (type instanceof WildcardType) {
      return (T)AbstractWildcardType.valueOf((WildcardType)type);
    } else {
      throw new IllegalArgumentException("Unexpected type: " + type);
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
    } else if (type instanceof Class<?>) {
      return hashCode((Class<?>)type);
    } else if (type instanceof ParameterizedType) {
      return hashCode((ParameterizedType)type);
    } else if (type instanceof GenericArrayType) {
      return hashCode((GenericArrayType)type);
    } else if (type instanceof FreshTypeVariable) {
      return hashCode((FreshTypeVariable)type);
    } else if (type instanceof IntersectionType) {
      return hashCode((IntersectionType)type);
    } else if (type instanceof TypeVariable) {
      return hashCode((TypeVariable<?>)type);
    } else if (type instanceof WildcardType) {
      return hashCode((WildcardType)type);
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

  private static final int hashCode(final FreshTypeVariable type) {
    return System.identityHashCode(type);
    // return type == null ? 0 : hashCode(type.getUpperBound()) ^ hashCode(type.getLowerBound());
  }

  private static final int hashCode(final IntersectionType type) {
    return type == null ? 0 : Arrays.hashCode(type.getBounds());
  }

  private static final int hashCode(final TypeVariable<?> type) {
    return type == null ? 0 : type.hashCode();
  }

  private static final int hashCode(final WildcardType type) {
    return type == null ? 0 : Arrays.hashCode(type.getUpperBounds()) ^ Arrays.hashCode(type.getLowerBounds());
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
    } else if (type0 instanceof Class) {
      return type1 instanceof Class && equals((Class<?>)type0, (Class<?>)type1);
    } else if (type0 instanceof ParameterizedType) {
      return type1 instanceof ParameterizedType && equals((ParameterizedType)type0, (ParameterizedType)type1);
    } else if (type0 instanceof GenericArrayType) {
      return type1 instanceof GenericArrayType && equals((GenericArrayType)type0, (GenericArrayType)type1);
    } else if (type0 instanceof FreshTypeVariable) {
      return type1 instanceof FreshTypeVariable && equals((FreshTypeVariable)type0, (FreshTypeVariable)type1);
    } else if (type0 instanceof IntersectionType) {
      return type1 instanceof IntersectionType && equals((IntersectionType)type0, (IntersectionType)type1);
    } else if (type0 instanceof TypeVariable) {
      return type1 instanceof TypeVariable && equals((TypeVariable<?>)type0, (TypeVariable<?>)type1);
    } else if (type0 instanceof WildcardType) {
      return type1 instanceof WildcardType && equals((WildcardType)type0, (WildcardType)type1);
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

  private static final boolean equals(final FreshTypeVariable tv0, final FreshTypeVariable tv1) {
    if (tv0 == null) {
      return tv1 == null;
    } else if (tv1 == null) {
      return false;
    } else if (tv0 == tv1) {
      return true;
    } else {
      return false;
      /*
      // TODO: REALLY not sure about this.
      // My rationale is as follows:
      // Two TypeVariables are equal to one another if their
      // GenericDeclarations and names are equal.
      // A FreshTypeVariable has no GenericDeclaration.
      // A FreshTypeVariable has no name.
      // So the only thing left to look at is the bounds.
      final boolean returnValue =
        equals(tv0.getUpperBound(), tv1.getUpperBound()) &&
        equals(tv0.getLowerBound(), tv1.getLowerBound());
      if (returnValue) {
        System.out.println("*** equals called successfully with two non-identical FreshTypeVariable arguments: " + toString(tv0) + ", " + toString(tv1));
      }
      return returnValue;
      */
    }
  }

  private static final boolean equals(final IntersectionType tv0, final IntersectionType tv1) {
    if (tv0 == null) {
      return tv1 == null;
    } else if (tv1 == null) {
      return false;
    } else if (tv0 == tv1) {
      return true;
    } else {
      return equals(tv0.getBounds(), tv1.getBounds());
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
    } else if (gd0 instanceof Class) {
      return gd1 instanceof Class && equals((Class<?>)gd0, (Class<?>)gd1);
    } else if (gd0 instanceof Executable) {
      return gd1 instanceof Executable && equals((Executable)gd0, (Executable)gd1);
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
    } else if (e0 instanceof Constructor) {
      return e1 instanceof Constructor && equals((Constructor<?>)e0, (Constructor<?>)e1);
    } else if (e0 instanceof Method) {
      return e1 instanceof Method && equals((Method)e0, (Method)e1);
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

  private static final String toString(final Object[] a) {
    if (a == null) {
      return "null";
    } else if (a instanceof Type[]) {
      return toString((Type[])a);
    } else if (a instanceof TypeWrapper[]) {
      return toString((TypeWrapper[])a);
    } else {
      return Arrays.toString(a);
    }
  }

  public static final String toString(final Type[] types) {
    if (types == null) {
      return "null";
    } else if (types.length <= 0) {
      return "[]";
    } else {
      final StringJoiner sj = new StringJoiner(", ", "[", "]");
      for (final Type type : types) {
        sj.add(toString(type));
      }
      return sj.toString();
    }
  }

  private static final String toString(final TypeWrapper typeWrapper) {
    return String.valueOf(typeWrapper);
  }

  private static final String toString(final TypeWrapper[] typeWrappers) {
    if (typeWrappers == null) {
      return "null";
    } else if (typeWrappers.length <= 0) {
      return "[]";
    } else {
      return toString(Arrays.stream(typeWrappers).map(tw -> tw.t).toArray(Type[]::new));
    }
  }

  public static final String toString(final Collection<?> types) {
    if (types == null) {
      return "null";
    } else if (types.isEmpty()) {
      return "[]";
    } else {
      final StringJoiner sj = new StringJoiner(", ", "[", "]");
      for (final Object type : types) {
        sj.add(toString(type));
      }
      return sj.toString();
    }
  }

  private static final String toString(final Object o) {
    if (o == null) {
      return "null";
    } if (o instanceof Collection) {
      return toString((Collection<?>)o);
    } else if (o instanceof Type) {
      return toString((Type)o);
    } else if (o instanceof TypeWrapper) {
      return toString((TypeWrapper)o);
    } else {
      return o.toString();
    }
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
    } else if (type instanceof Class) {
      return toString((Class<?>)type);
    } else if (type instanceof ParameterizedType) {
      return toString((ParameterizedType)type);
    } else if (type instanceof GenericArrayType) {
      return toString((GenericArrayType)type);
    } else if (type instanceof FreshTypeVariable) {
      return toString((FreshTypeVariable)type);
    } else if (type instanceof IntersectionType) {
      return toString((IntersectionType)type);
    } else if (type instanceof TypeVariable) {
      return toString((TypeVariable<?>)type);
    } else if (type instanceof WildcardType) {
      return toString((WildcardType)type);
    } else {
      return type.getTypeName();
    }
  }

  private static final String toString(final Class<?> c) {
    return c == null ? "null" : c.getName();
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
        if (ownerType instanceof ParameterizedType) {
          sb.append(toString(rawType).replace(toString(((ParameterizedType)ownerType).getRawType()) + "$", ""));
        } else if (rawType instanceof Class) {
          sb.append(((Class<?>)rawType).getSimpleName());
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

  private static final String toString(final FreshTypeVariable ftv) {
    return ftv == null ? "null" : ftv.getTypeName();
  }

  private static final String toString(final IntersectionType type) {
    return type == null ? "null" : type.getTypeName();
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

  private static final Type array(final Type type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-10.html#jls-10.1
    // 10.1 Array Types
    // …
    // The element type of an array may be any type, whether
    // primitive or reference.
    // …
    // [That rules out wildcards, but nothing else.]
    if (Objects.requireNonNull(type, "type") instanceof Class) {
      return array((Class<?>)type);
    } else if (type instanceof GenericArrayType) {
      return type;
    } else if (!(type instanceof WildcardType)) {
      return new DefaultGenericArrayType(type);
    } else {
      throw new IllegalArgumentException("!isReferenceType(type): " + type);
    }
  }

  private static final Class<?> array(final Class<?> type) {
    return type.isArray() ? type : Array.newInstance(type, 0).getClass();
  }

  private static final GenericArrayType array(final GenericArrayType type) {
    return Objects.requireNonNull(type, "type");
  }

  static final Type glb(final Type... types) {
    // https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.10
    // 5.1.10 Capture Conversion
    // […]
    // glb(V₁,…,Vₘ) is defined as V₁ & … & Vₘ.
    if (types == null || types.length <= 0) {
      return null;
    } else if (types.length == 1) {
      return types[0];
    } else {
      for (int i = 1; i < types.length; i++) {
        if (!equals(types[0], types[i])) {
          return new IntersectionType(types);
        }
      }
      return types[0];
    }
  }

  private static final Type lub(final Type[] types) {
    Objects.requireNonNull(types, "types");
    // see
    // https://github.com/openjdk/jdk/blob/b870468bdc99938fbb19a41b0ede0a3e3769ace2/src/jdk.compiler/share/classes/com/sun/tools/javac/code/Types.java#L3959
    // for inspiration
    throw new UnsupportedOperationException("lub()");
  }


  private static final boolean formalTypeParametersAreTypeVariableInstances(final Type[] formalTypeParameters) {
    return formalTypeParameters instanceof TypeVariable<?>[];
  }

  private static final boolean parameterizedTypeIsItsOwnDirectSupertype(final Type type, final Type directSupertype) {
    if (type instanceof GenericArrayType || type instanceof ParameterizedType) {
      return equals(type, directSupertype);
    } else {
      return false;
    }
  }

  public static final Type[] emptyTypeArray() {
    return EMPTY_TYPE_ARRAY;
  }


  /*
   * Inner and nested classes.
   */


  private static final class TypeComparator implements Comparator<Type> {

    @Override
    public final int compare(final Type t1, final Type t2) {
      if (t1 == null) {
        return t2 == null ? 0 : 1; // nulls go to the right
      } else if (t2 == null) {
        return -1; // nulls go to the right
      } else if (t1 == t2 || Types.equals(t1, t2)) {
        return 0;
      } else {
        return Types.toString(t1).compareTo(Types.toString(t2));
      }
    }

  }

  static final class IntersectionType implements Type {

    private final Type[] bounds;

    private IntersectionType() {
      super();
      this.bounds = new Type[] { Object.class };
    }

    private IntersectionType(final Type bound) {
      super();
      this.bounds = new Type[] { bound == null ? Object.class : bound };
    }

    private IntersectionType(final Type[] bounds) {
      super();
      if (bounds == null || bounds.length <= 0) {
        this.bounds = new Type[] { Object.class };
      } else {
        this.bounds = bounds.clone();
      }
    }

    final Type[] getBounds() {
      return this.bounds.clone();
    }

    @Override
    public final int hashCode() {
      return Types.hashCode(this);
    }

    @Override
    public final boolean equals(final Object other) {
      if (other == this) {
        return true;
      } else if (other == null) {
        return false;
      } else if (this.getClass() == other.getClass()) {
        return Types.equals(this, (IntersectionType)other);
      } else {
        return false;
      }
    }

    @Override
    public final String toString() {
      final StringJoiner sj = new StringJoiner(" & ");
      for (final Type t : this.bounds) {
        sj.add(Types.toString(t));
      }
      return sj.toString();
    }

  }

  static final class FreshTypeVariable implements Type {

    private final Type upperBound;

    private final Type lowerBound;

    private FreshTypeVariable(final Type[] upperBounds) {
      this(upperBounds, emptyTypeArray());
    }

    private FreshTypeVariable(final Type[] upperBounds, final Type[] lowerBounds) {
      super();
      if (upperBounds == null || upperBounds.length <= 0) {
        this.upperBound = Object.class;
      } else if (upperBounds.length == 1) {
        this.upperBound = upperBounds[0];
      } else {
        this.upperBound = new IntersectionType(upperBounds);
      }
      if (lowerBounds == null || lowerBounds.length <= 0) {
        this.lowerBound = null;
      } else {
        this.lowerBound = lowerBounds.length == 1 ? lowerBounds[0] : new IntersectionType(lowerBounds);
      }
      this.validate();
    }

    private final void validate() {
      if (isPrimitive(this.upperBound) ||
          isWildcard(this.upperBound)) {
        throw new IllegalArgumentException("upperBound: " + this.upperBound);
      } else if (isPrimitive(this.lowerBound) ||
                 isWildcard(this.lowerBound) ||
                 this.lowerBound instanceof FreshTypeVariable) {
        throw new IllegalArgumentException("lowerBound: " + this.lowerBound);
      }
    }

    FreshTypeVariable() {
      super();
      this.upperBound = Object.class;
      this.lowerBound = null;
    }

    FreshTypeVariable(final Type upperBound) {
      super();
      this.upperBound = upperBound == null ? Object.class : upperBound;
      this.lowerBound = null;
      this.validate();
    }

    private FreshTypeVariable(final Type upperBound, final Type lowerBound) {
      super();
      this.upperBound = upperBound == null ? Object.class : upperBound;
      this.lowerBound = lowerBound;
      this.validate();
    }

    final Type getUpperBound() {
      return this.upperBound;
    }

    final Type getLowerBound() {
      return this.lowerBound;
    }

    @Override
    public final int hashCode() {
      return Types.hashCode(this);
    }

    @Override
    public final boolean equals(final Object other) {
      if (other == this) {
        return true;
      } else if (other == null) {
        return false;
      } else if (this.getClass() == other.getClass()) {
        return Types.equals(this, (FreshTypeVariable)other);
      } else {
        return false;
      }
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("CAP extends ").append(Types.toString(upperBound));
      if (this.lowerBound != null) {
        sb.append(" super ").append(Types.toString(lowerBound));
      }
      return sb.toString();
    }

  }

  private static final class TypeWrapper implements Comparable<TypeWrapper> { // NOTE: does NOT implement Type

    private final Type t;

    private TypeWrapper(final Type t) {
      super();
      this.t = t;
    }

    @Override
    public final int hashCode() {
      return Types.hashCode(this.t);
    }

    @Override
    public final int compareTo(final TypeWrapper tw) {
      if (tw == this) {
        return 0;
      } else if (tw == null) {
        return -1; // nulls go to the right
      } else if (this.equals(tw)) {
        return 0;
      } else {
        return typeComparator.compare(this.t, tw.t);
      }
    }

    @Override
    public final boolean equals(final Object other) {
      if (other == this) {
        return true;
      } else if (other == null) {
        return false;
      } else if (this.getClass() == other.getClass()) {
        return Types.equals(this.t, ((TypeWrapper)other).t);
      } else if (other instanceof Type) {
        return Types.equals(this.t, (Type)other);
      } else {
        return false;
      }
    }

    @Override
    public final String toString() {
      return Types.toString(this.t);
    }

    private static final TypeWrapper of(final Type type) {
      return new TypeWrapper(type);
    }

  }

  private static final class TypeWrapperSet extends HashSet<TypeWrapper> {

    private static final long serialVersionUID = 1L;

    private TypeWrapperSet() {
      super();
    }

    private final boolean add(final Type type) {
      return super.add(TypeWrapper.of(type));
    }

    @Override
    public final boolean contains(final Object o) {
      if (super.contains(o)) {
        return true;
      } else if (o instanceof Type) {
        return super.contains(TypeWrapper.of((Type)o));
      } else {
        return false;
      }
    }

    private final Type[] toTypeArray() {
      final int size = this.size();
      if (size <= 0) {
        return emptyTypeArray();
      } else {
        final Type[] returnValue = new Type[size];
        int i = 0;
        for (final TypeWrapper tw : this) {
          returnValue[i++] = tw.t;
        }
        return returnValue;
      }
    }

  }

}
