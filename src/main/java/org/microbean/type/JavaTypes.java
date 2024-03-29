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

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;

import java.lang.invoke.MethodHandles;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import java.util.function.Predicate;

import org.microbean.development.annotation.Convenience;

import org.microbean.type.Type.CovariantSemantics;

import static java.lang.constant.ConstantDescs.BSM_GET_STATIC_FINAL;
import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_Class;
import static java.lang.constant.ConstantDescs.CD_MethodHandle;
import static java.lang.constant.ConstantDescs.CD_MethodHandles;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.DEFAULT_NAME;
import static java.lang.constant.ConstantDescs.NULL;

import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC;

import static org.microbean.type.ConstantDescs.CD_Constructor;
import static org.microbean.type.ConstantDescs.CD_DefaultGenericArrayType;
import static org.microbean.type.ConstantDescs.CD_DefaultParameterizedType;
import static org.microbean.type.ConstantDescs.CD_GenericArrayType;
import static org.microbean.type.ConstantDescs.CD_GenericDeclaration;
import static org.microbean.type.ConstantDescs.CD_LowerBoundedWildcardType;
import static org.microbean.type.ConstantDescs.CD_ParameterizedType;
import static org.microbean.type.ConstantDescs.CD_Member;
import static org.microbean.type.ConstantDescs.CD_Method;
import static org.microbean.type.ConstantDescs.CD_Type;
import static org.microbean.type.ConstantDescs.CD_TypeVariable;
import static org.microbean.type.ConstantDescs.CD_UnboundedWildcardType;
import static org.microbean.type.ConstantDescs.CD_UpperBoundedWildcardType;
import static org.microbean.type.ConstantDescs.CD_WildcardType;
import static org.microbean.type.ConstantDescs.DMHD_REFLECT_AS;

/**
 * A utility class providing useful operations related to Java {@link
 * Type}s.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public final class JavaTypes {


  /*
   * Static fields.
   */


  private static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

  /**
   * An immutable {@link Map} of Java wrapper {@linkplain Class
   * classes} indexed by their primitive equivalents.
   */
  public static final Map<Type, Class<?>> boxedTypes =
    Map.of(boolean.class, Boolean.class,
           byte.class,    Byte.class,
           char.class,    Character.class,
           double.class,  Double.class,
           float.class,   Float.class,
           int.class,     Integer.class,
           long.class,    Long.class,
           short.class,   Short.class,
           void.class,    Void.class);


  /*
   * Constructors.
   */


  private JavaTypes() {
    super();
  }


  /*
   * Static methods.
   */


  /**
   * Returns the supplied {@link Type} if it is not a {@link Class} or
   * if it is {@linkplain Class#isPrimitive() not primitive}, or a
   * {@linkplain #boxedTypes boxed type} representing it.
   *
   * @param type the {@link Type}; must not be {@code null}
   *
   * @return a boxed representation of the supplied {@link Type}, or
   * the {@link Type} itself
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is deterministic, but not necessarily
   * idempotent.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #boxedTypes
   */
  public static final Type box(final Type type) {
    if (type == void.class) {
      // This is such a ridiculously common case we avoid the map lookup
      return Void.class;
    } else if (type == int.class) {
      // This is such a ridiculously common case we avoid the map lookup
      return Integer.class;
    } else if (type instanceof Class<?> c && c.isPrimitive()) {
      return boxedTypes.get(c);
    } else {
      return type;
    }
  }

  /**
   * Returns {@code true} if and only if a reference bearing the
   * type modeled by the supplied {@code payloadType} is assignable
   * to a reference bearing the type modeled by the supplied {@code
   * receiverType}, according to the assignability rules modeled by
   * the {@link CovariantSemantics} class.
   *
   * <p>This method calls {@link CovariantSemantics#assignable(Type,
   * Type)} with the supplied arguments and returns the result.</p>
   *
   * @param receiverType the receiver type as described above; must
   * not be {@code null}
   *
   * @param payloadType the payload type as described above; must
   * not be {@code null}
   *
   * @return {@code true} if and only if a reference bearing the type
   * modeled by the supplied {@code payloadType} is assignable to a
   * reference bearing the type modeled by the {@link
   * CovariantSemantics} class
   *
   * @exception NullPointerException if either {@code receiverType}
   * or {@code payloadType} is {@code null}
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see CovariantSemantics#assignable(Type, Type, boolean)
   */
  @Convenience
  public static final boolean assignable(final Type receiverType, final Type payloadType) {
    return CovariantSemantics.INSTANCE.assignable(receiverType, payloadType);
  }

  /**
   * Returns {@code true} if and only if a reference bearing the
   * type modeled by the supplied {@code payloadType} is assignable
   * to a reference bearing the type modeled by the supplied {@code
   * receiverType}, according to the assignability rules modeled by
   * the {@link CovariantSemantics} class.
   *
   * <p>This method calls {@link CovariantSemantics#assignable(Type,
   * Type, boolean)} with the supplied arguments and returns the
   * result.</p>
   *
   * @param receiverType the receiver type as described above; must
   * not be {@code null}
   *
   * @param payloadType the payload type as described above; must
   * not be {@code null}
   *
   * @param box whether autoboxing is enabled
   *
   * @return {@code true} if and only if a reference bearing the type
   * modeled by the supplied {@code payloadType} is assignable to a
   * reference bearing the type modeled by the {@link
   * CovariantSemantics} class
   *
   * @exception NullPointerException if either {@code receiverType}
   * or {@code payloadType} is {@code null}
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see CovariantSemantics#assignable(Type, Type, boolean)
   */
  @Convenience
  public static final boolean assignable(final Type receiverType, final Type payloadType, final boolean box) {
    return CovariantSemantics.INSTANCE.assignable(receiverType, payloadType, box);
  }

  /**
   * Returns a {@link Type} array with a length of {@code 0}.
   *
   * @return a {@link Type} array with a length of {@code 0}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final Type[] emptyTypeArray() {
    return EMPTY_TYPE_ARRAY;
  }

  /**
   * Returns the <em>direct supertypes</em> of the supplied {@link
   * Type}, provided that it is either a {@link Class}, a {@link
   * ParameterizedType}, a {@link GenericArrayType} or a {@link
   * TypeVariable}.
   *
   * <p>The direct supertypes of a type do not include the type
   * itself.</p>
   *
   * <p>The returned {@link Collection} will contain no duplicate
   * elements but is not guaranteed to be a {@link Set}
   * implementation.</p>
   *
   * @param type the {@link Type} to introspect; must not be {@code
   * null}
   *
   * @return a {@link Collection} of the direct supertypes of the
   * supplied {@code type}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @exception IllegalArgumentException if {@code type} is not a
   * {@link Class} and not a {@link ParameterizedType} and not a
   * {@link GenericArrayType} and not a {@link TypeVariable}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final Collection<? extends Type> directSupertypes(final Type type) {
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

  /**
   * Returns the <em>direct supertypes</em> of the supplied {@link
   * Type}, provided that it is either a {@link Class}, a {@link
   * ParameterizedType}, a {@link GenericArrayType} or a {@link
   * TypeVariable}.
   *
   * <p>The direct supertypes of a type do not include the type
   * itself.</p>
   *
   * <p>The returned {@link Collection} will contain no duplicate
   * elements but is not guaranteed to be a {@link Set}
   * implementation.</p>
   *
   * @param type the {@link Type} to introspect; must not be {@code
   * null}
   *
   * @param acceptancePredicate a {@link Predicate} controlling
   * membership of a {@link Type} in the returned {@link Collection};
   * must not be {@code null}
   *
   * @return a {@link Collection} of the direct supertypes of the
   * supplied {@code type}; never {@code null}
   *
   * @exception NullPointerException if either {@code type} or {@code
   * acceptancePredicate} is {@code null}.
   *
   * @exception IllegalArgumentException if {@code type} is not a
   * {@link Class} and not a {@link ParameterizedType} and not a
   * {@link GenericArrayType} and not a {@link TypeVariable}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final Collection<? extends Type> directSupertypes(final Type type,
                                                                  final Predicate<? super Type> acceptancePredicate) {
    final Collection<? extends Type> directSupertypes = directSupertypes(type);
    if (directSupertypes.isEmpty()) {
      return List.of();
    }
    final ArrayList<Type> c = new ArrayList<>(directSupertypes.size());
    for (final Type ds : directSupertypes) {
      if (acceptancePredicate.test(ds)) {
        c.add(ds);
      }
    }
    c.trimToSize();
    return c.isEmpty() ? List.of() : Collections.unmodifiableCollection(c);
  }

  static final <T> boolean acceptAll(final T type) {
    return true;
  }

  private static final Collection<? extends Type> directSupertypes(final Class<?> c) {
    if (c == Object.class) {
      return List.of();
    } else if (c.isPrimitive()) {
      // 4.10.1. Subtyping among Primitive Types
      //
      // The following rules define the direct supertype relation
      // among the primitive types:
      //
      // double >₁ float
      //
      // float >₁ long
      //
      // long >₁ int
      //
      // int >₁ char
      //
      // int >₁ short
      //
      // short >₁ byte
      //
      // [We skip all this.]
      return List.of();
    } else {
      final ArrayList<Type> directSupertypes = new ArrayList<>(11);
      final Class<?> componentType = c.getComponentType();
      if (componentType == null) {
        final Type[] parameters = c.getTypeParameters();
        if (parameters.length > 0) {
          // 4.10.2. Subtyping among Class and Interface Types
          //
          // […]
          //
          // Given a generic class or interface C with type parameters
          // F₁,…,Fₙ (𝑛 > 0), the direct supertypes of the [this]
          // raw type C (§4.8) are all of the following:
          //
          // The erasure (§4.6) of the direct superclass type of C, if
          // C is a class [i.e. not an interface].
          //
          // The erasure of the direct superinterface types of C.
          //
          // The type Object, if C is an interface with no direct
          // superinterface types.
          final Class<?> directSuperclassTypeErasure = c.getSuperclass(); // let the JDK do the erasure for us
          if (directSuperclassTypeErasure != null) {
            directSupertypes.add(directSuperclassTypeErasure);
          }
          final Class<?>[] directSuperinterfaceTypeErasures = c.getInterfaces(); // let the JDK do the erasure for us
          if (directSuperinterfaceTypeErasures.length > 0) {
            for (final Class<?> directSuperinterfaceTypeErasure : directSuperinterfaceTypeErasures) {
              directSupertypes.add(directSuperinterfaceTypeErasure);
            }
          } else if (c.isInterface()) {
            directSupertypes.add(Object.class);
          }
        } else {
          // 4.10.2. Subtyping among Class and Interface Types
          //
          // Given a non-generic class or interface C, the direct
          // supertypes of the type of C are all of the following:
          //
          // The direct superclass type of C (§8.1.4), if C is a class.
          //
          // The direct superinterface types of C (§8.1.5, §9.1.3).
          //
          // The type Object, if C is an interface with no direct
          // superinterface types (§9.1.3).
          final Type directSuperclassType = c.getGenericSuperclass(); // let the JDK construct what may be either a Class or a ParameterizedType
          if (directSuperclassType != null) {
            assert (directSuperclassType instanceof Class<?>) || (directSuperclassType instanceof ParameterizedType) : "Unexpected directSuperclassType: " + directSuperclassType;
            directSupertypes.add(directSuperclassType);
          }
          final Type[] directSuperinterfaceTypes = c.getGenericInterfaces(); // let the JDK construct what may be either Class or ParameterizedType instances
          if (directSuperinterfaceTypes.length > 0) {
            for (final Type directSuperinterfaceType : directSuperinterfaceTypes) {
              assert (directSuperinterfaceType instanceof Class<?>) || (directSuperinterfaceType instanceof ParameterizedType) : "Unexpected directSuperinterfaceType: " + directSuperinterfaceType;
              directSupertypes.add(directSuperinterfaceType);
            }
          } else if (c.isInterface()) {
            directSupertypes.add(Object.class);
          }
        }
      } else {
        // 4.10.3. Subtyping among Array Types
        //
        // The following rules define the direct supertype relation
        // among array types:
        //
        // If S and T are both reference types, then S[] >₁ T[] iff
        // S >₁ T.
        //
        // Object >₁ Object[]
        //
        // Cloneable >₁ Object[]
        //
        // java.io.Serializable >₁ Object[]
        //
        // If P is a primitive type, then:
        //
        // Object >₁ P[]
        //
        // Cloneable >₁ P[]
        //
        // java.io.Serializable >₁ P[]
        if (componentType == Object.class || componentType.isPrimitive()) {
          // Object[] or, say, int[]
          directSupertypes.add(Object.class);
          directSupertypes.add(Cloneable.class);
          directSupertypes.add(Serializable.class);
        } else {
          // Reference type (which could be a Class or a ParameterizedType).
          for (final Type componentTypeDirectSupertype : directSupertypes(componentType)) {
            directSupertypes.add(array(componentTypeDirectSupertype));
          }
        }
      }
      directSupertypes.trimToSize();
      return Collections.unmodifiableCollection(directSupertypes);
    }
  }

  private static final Collection<Type> directSupertypes(final ParameterizedType p) {
    final ArrayList<Type> directSupertypes = new ArrayList<>(11);
    // 4.10.2. Subtyping among Class and Interface Types
    //
    // […]
    //
    // Given a generic class or interface C with type parameters
    // F₁,…,Fₙ (𝑛 > 0), the direct supertypes of the parameterized
    // type C<T₁,…,Tₙ>, where each of Tᵢ (1 ≤ 𝑖 ≤ 𝑛) is a type, are
    // all of the following:
    //
    // The substitution [F₁:=T₁,…,Fₙ:=Tₙ] applied to the direct
    // superclass type of C, if C is a class [i.e. not an interface].
    //
    // The substitution [F₁:=T₁,…,Fₙ:=Tₙ] applied to the direct
    // superinterface types of C.
    //
    // C<S₁,…,Sₙ>, where Sᵢ contains Tᵢ (1 ≤ 𝑖 ≤ 𝑛) (§4.5.1) [we're
    // going to skip this].
    //
    // The type Object, if C is an interface with no direct
    // superinterface types.
    //
    // The raw type C.
    final Class<?> c = erase(p.getRawType());
    final Type[] typeArguments = p.getActualTypeArguments();
    final Type directSuperclassType = c.getGenericSuperclass();
    if (directSuperclassType != null) {
      if (directSuperclassType instanceof ParameterizedType dst) {
        final Class<?> directSuperclassTypeErasure = erase(dst.getRawType());
        assert directSuperclassTypeErasure.getTypeParameters().length > 0 : "Unexpected empty type parameters";
        assert typeArguments.length == directSuperclassTypeErasure.getTypeParameters().length;
        directSupertypes.add(new DefaultParameterizedType(dst.getOwnerType(), directSuperclassTypeErasure, typeArguments));
      } else if (directSuperclassType instanceof Class<?> nonGenericClass) {
        assert nonGenericClass.getTypeParameters().length == 0;
        directSupertypes.add(nonGenericClass);
      } else {
        throw new AssertionError("Unexpected directSuperclassType: " + directSuperclassType);
      }
    }
    final Type[] directSuperinterfaceTypes = c.getGenericInterfaces();
    if (directSuperinterfaceTypes.length > 0) {
      for (final Type directSuperinterfaceType : directSuperinterfaceTypes) {
        if (directSuperinterfaceType instanceof ParameterizedType dst) {
          final Class<?> directSuperinterfaceTypeErasure = erase(dst.getRawType());
          assert directSuperinterfaceTypeErasure.getTypeParameters().length > 0 : "Unexpected empty type parameters";
          assert typeArguments.length == directSuperinterfaceTypeErasure.getTypeParameters().length;
          directSupertypes.add(new DefaultParameterizedType(dst.getOwnerType(), directSuperinterfaceTypeErasure, typeArguments));
        } else if (directSuperinterfaceType instanceof Class<?> nonGenericInterface) {
          assert nonGenericInterface.getTypeParameters().length == 0;
          directSupertypes.add(nonGenericInterface);
        } else {
          throw new AssertionError("Unexpected directSuperinterfaceType: " + directSuperinterfaceType);
        }
      }
    } else if (c.isInterface()) {
      directSupertypes.add(Object.class);
    }
    directSupertypes.add(c);
    directSupertypes.trimToSize();
    return Collections.unmodifiableCollection(directSupertypes);
  }

  private static final Collection<Type> directSupertypes(final GenericArrayType g) {
    final Collection<? extends Type> genericComponentTypeDirectSupertypes = directSupertypes(g.getGenericComponentType());
    final Collection<Type> returnValue = new ArrayList<>(genericComponentTypeDirectSupertypes.size());
    for (final Type ds : genericComponentTypeDirectSupertypes) {
      returnValue.add(array(ds));
    }
    return Collections.unmodifiableCollection(returnValue);
  }

  private static final Collection<Type> directSupertypes(final TypeVariable<?> tv) {
    // 4.10.2. Subtyping among Class and Interface Types
    //
    // […]
    //
    // The direct supertypes of a type variable are the types listed
    // in its bound.
    return List.of(tv.getBounds());
  }

  /**
   * Returns the <em>supertypes</em> of the supplied {@link
   * Type}, provided that it is either a {@link Class}, a {@link
   * ParameterizedType}, a {@link GenericArrayType} or a {@link
   * TypeVariable}.
   *
   * <p><strong>The supertypes of a type include the type
   * itself.</strong></p>
   *
   * <p>The returned {@link Collection} will contain no duplicate
   * elements but is not guaranteed to be a {@link Set}
   * implementation.</p>
   *
   * @param type the {@link Type} to introspect; must not be {@code
   * null}
   *
   * @return a {@link Collection} of the supertypes of the supplied
   * {@code type}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @exception IllegalArgumentException if {@code type} is not a
   * {@link Class} and not a {@link ParameterizedType} and not a
   * {@link GenericArrayType} and not a {@link TypeVariable}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final Collection<Type> supertypes(final Type type) {
    return supertypes0(type, new HashSet<>()::add);
  }

  /**
   * Returns the <em>supertypes</em> of the supplied {@link
   * Type}, provided that it is either a {@link Class}, a {@link
   * ParameterizedType}, a {@link GenericArrayType} or a {@link
   * TypeVariable}.
   *
   * <p><strong>The supertypes of a type include the type
   * itself.</strong></p>
   *
   * <p>The returned {@link Collection} will contain no duplicate
   * elements but is not guaranteed to be a {@link Set}
   * implementation.</p>
   *
   * @param type the {@link Type} to introspect; must not be {@code
   * null}
   *
   * @param acceptancePredicate a {@link Predicate} controlling
   * membership of a {@link Type} in the returned {@link Collection};
   * must not be {@code null}
   *
   * @return a {@link Collection} of the supertypes of the supplied
   * {@code type}; never {@code null}
   *
   * @exception NullPointerException if either {@code type} or {@code
   * acceptancePredicate} is {@code null}.
   *
   * @exception IllegalArgumentException if {@code type} is not a
   * {@link Class} and not a {@link ParameterizedType} and not a
   * {@link GenericArrayType} and not a {@link TypeVariable}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final Collection<Type> supertypes(final Type type, final Predicate<? super Type> acceptancePredicate) {
    final ArrayList<Type> c = new ArrayList<>(11);
    for (final Type st : supertypes(type)) {
      if (acceptancePredicate.test(st)) {
        c.add(st);
      }
    }
    c.trimToSize();
    return c.isEmpty() ? List.of() : Collections.unmodifiableCollection(c);
  }

  private static final Collection<Type> supertypes0(final Type type, final Predicate<? super JavaType> unseen) {
    if (unseen.test(JavaType.of(type))) {
      final ArrayList<Type> supertypes = new ArrayList<>(11);
      supertypes.add(type); // reflexive
      for (final Type ds : directSupertypes(type)) {
        supertypes.addAll(supertypes0(ds, unseen)); // transitive/recursive
      }
      supertypes.trimToSize();
      return Collections.unmodifiableCollection(supertypes);
    } else {
      return List.of();
    }
  }

  /**
   * Returns {@code true} if and only if {@code sup} is a supertype of
   * {@code sub}.
   *
   * <p>This method obeys the <a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.10"
   * target="_parent">subtyping rules from the Java Language
   * Specification</a> with the following exceptions:</p>
   *
   * <ul>
   *
   * <li><a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.5.1"
   * target="_parent">Containing types</a> are discarded when
   * calculating the supertypes of a parameterized type.</li>
   *
   * <li>{@linkplain Class#isPrimitive() Primitive types} have no
   * supertypes.</li>
   *
   * </ul>
   *
   * @param sup the purported supertype; must not be {@code null}
   *
   * @param sub the purported subtype; must not be {@code null}
   *
   * @return {@code true} if and only if {@code sup} is a supertype of
   * {@code sub}
   *
   * @exception NullPointerException if either argument is {@code
   * null}
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #supertypes(Type)
   */
  public static final boolean supertype(final Type sup, final Type sub) {
    return supertype(sup, sub, null);
  }

  /**
   * Returns {@code true} if and only if {@code sup} is a supertype of
   * {@code sub}.
   *
   * <p>This method obeys the <a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.10"
   * target="_parent">subtyping rules from the Java Language
   * Specification</a> with the following exceptions:</p>
   *
   * <ul>
   *
   * <li><a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.5.1"
   * target="_parent">Containing types</a> are discarded when
   * calculating the supertypes of a parameterized type.</li>
   *
   * <li>{@linkplain Class#isPrimitive() Primitive types} have no
   * supertypes.</li>
   *
   * </ul>
   *
   * @param sup the purported supertype; must not be {@code null}
   *
   * @param sub the purported subtype; must not be {@code null}
   *
   * @param acceptancePredicate a {@link Predicate} controlling which
   * supertypes are considered in this method's calculation; may be
   * {@code null} in which case all supertypes will be considered
   *
   * @return {@code true} if and only if {@code sup} is a supertype of
   * {@code sub}
   *
   * @exception NullPointerException if either {@code sup} or {@code
   * sub} is {@code null}
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #supertypes(Type, Predicate)
   */
  public static final boolean supertype(final Type sup, final Type sub, Predicate<? super Type> acceptancePredicate) {
    // Is sup a supertype of sub?
    Objects.requireNonNull(sup, "sup");
    Objects.requireNonNull(sub, "sub");
    final boolean acceptAll;
    if (acceptancePredicate == null) {
      acceptancePredicate = JavaTypes::acceptAll;
      acceptAll = true;
    } else {
      acceptAll = acceptancePredicate == (Predicate<? super Type>)JavaTypes::acceptAll;
    }
    if (acceptAll || acceptancePredicate.test(sup) && acceptancePredicate.test(sub)) {
      if (equals(sup, sub)) {
        return true;
      } else if (acceptAll && sup instanceof Class<?> supC && sub instanceof Class<?> subC) {
        // Easy optimization
        return supC.isAssignableFrom(subC);
      } else {
        for (final Type supertype : supertypes(sub, acceptancePredicate)) {
          if (equals(supertype, sup)) {
            return true;
          }
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
    if (type == null) {
      throw new NullPointerException("type");
    } else if (type instanceof Class<?> c) {
      return c.arrayType();
    } else if (!(type instanceof WildcardType)) {
      return new DefaultGenericArrayType(type);
    } else {
      throw new IllegalArgumentException("type: " + toString(type));
    }
  }

  static final boolean isReferenceType(final Type type) {
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
   * java.lang.reflect.Array#newInstance(Class, int)} with the return
   * value of an invocation of {@link #erase(Type)} on its {@linkplain
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
   * <li>If any other {@link Type} implementation is supplied, {@code
   * null} is returned.</li>
   *
   * </ul>
   *
   * @param type the {@link Type} for which the corresponding type
   * erasure is to be returned; may be {@code null} in which case
   * {@code null} will be returned
   *
   * @return a {@link Class}, or {@code null} if a suitable {@link
   * Class}-typed type erasure could not be determined, indicating
   * that the type erasure is the supplied {@link Type} itself
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
    // The erasure of a parameterized type (§4.5) G<T1,…,Tn> is |G|.
    //
    // The erasure of a nested type T.C is |T|.C.
    //
    // The erasure of an array type T[] is |T|[].
    //
    // The erasure of a type variable (§4.4) is the erasure of its
    // leftmost bound.
    //
    // The erasure of every other type is the type itself.
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
    // The erasure of a parameterized type (§4.5) G<T1,…,Tₙ> is |G|
    // [|G| means the erasure of G, i.e. the erasure of
    // type.getRawType()].
    return erase(type.getRawType());
  }

  private static final Class<?> erase(final GenericArrayType type) {
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.6
    //
    // The erasure of an array type T[] is |T|[]. [|T| means the
    // erasure of T. We erase the genericComponentType() and use
    // Class#arrayType() to find the "normal" array class for the
    // erasure.]
    final Class<?> componentType = erase(type.getGenericComponentType());
    if (componentType == null) {
      return null;
    } else {
      return componentType.arrayType();
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
    return bounds.length > 0 ? erase(bounds[0]) : Object.class;
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

  /**
   * Returns an {@link Optional} representing the supplied {@link
   * ConstantDesc}.
   *
   * @param constantDesc the {@link ConstantDesc} to represent; may be
   * {@code null}; if {@code null}, then an {@link Optional}
   * consisting of {@link java.lang.constant.ConstantDescs#NULL} will
   * be returned
   *
   * @return an {@link Optional} representing the supplied {@link
   * ConstantDesc}; never {@code null}; never {@linkplain
   * Optional#isEmpty() empty}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  private static final Optional<? extends ConstantDesc> describeConstable(final ConstantDesc constantDesc) {
    return constantDesc == null ? Optional.of(NULL) : Optional.of(constantDesc);
  }

  /**
   * Returns an {@link Optional} representing the supplied {@link
   * Constable}.
   *
   * @param constable the {@link Constable} to represent; may be
   * {@code null}; if {@code null}, then an {@link Optional}
   * consisting of {@link java.lang.constant.ConstantDescs#NULL} will
   * be returned
   *
   * @return an {@link Optional} representing the supplied {@link
   * Constable}; never {@code null}; possibly {@linkplain
   * Optional#isEmpty() empty}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  private static final Optional<? extends ConstantDesc> describeConstable(final Constable constable) {
    return constable == null ? Optional.of(NULL) : constable.describeConstable();
  }

  /**
   * Returns an {@link Optional} containing the nominal descriptor in
   * the form of a {@link ConstantDesc} for the supplied {@link Type}, or
   * an {@linkplain Optional#isEmpty() empty <code>Optional</code>} if
   * there is no such descriptor.
   *
   * @param type the {@link Type} in question; must be either {@code
   * null}, a {@link ConstantDesc} itself, a {@link Constable}, a
   * {@link ParameterizedType}, a {@link GenericArrayType}, a {@link
   * TypeVariable} or a {@link WildcardType}
   *
   * @return an {@link Optional} as described above; never {@code
   * null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see Constable
   */
  public static final Optional<? extends ConstantDesc> describeConstable(final Type type) {
    if (type == null) {
      return Optional.of(NULL);
    } else if (type instanceof ConstantDesc constantDesc) {
      return describeConstable(constantDesc);
    } else if (type instanceof Constable constable) {
      return describeConstable(constable); // includes Class<?>
    } else if (type instanceof ParameterizedType p) {
      return describeConstable(p);
    } else if (type instanceof GenericArrayType g) {
      return describeConstable(g);
    } else if (type instanceof TypeVariable<?> tv) {
      return describeConstable(tv);
    } else if (type instanceof WildcardType w) {
      return describeConstable(w);
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns an {@link Optional} containing the nominal descriptor in
   * the form of a {@link ConstantDesc} for the supplied {@link
   * ParameterizedType}, or an {@linkplain Optional#isEmpty() empty
   * <code>Optional</code>} if there is no such descriptor.
   *
   * @param ptype the {@link ParameterizedType} in question; may be
   * {@code null}
   *
   * @return an {@link Optional} as described above; never {@code
   * null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see Constable
   */
  private static final Optional<? extends ConstantDesc> describeConstable(final ParameterizedType ptype) {
    if (ptype == null) {
      return Optional.of(NULL);
    } else if (ptype instanceof ConstantDesc constantDesc) {
      return describeConstable(constantDesc);
    } else if (ptype instanceof Constable constable) {
      return describeConstable(constable);
    } else {
      final Optional<? extends ConstantDesc> ownerType = describeConstable(ptype.getOwnerType());
      if (ownerType.isPresent()) {
        final Optional<? extends ConstantDesc> rawType = describeConstable(ptype.getRawType());
        if (rawType.isPresent()) {
          final Type[] actualTypeArguments = ptype.getActualTypeArguments();
          final int bsmInvokeArgumentsLength = actualTypeArguments.length + 3;
          final ConstantDesc[] bsmInvokeArguments = new ConstantDesc[bsmInvokeArgumentsLength];
          bsmInvokeArguments[0] =
            MethodHandleDesc.ofConstructor(CD_DefaultParameterizedType,
                                           CD_Type,
                                           CD_Type,
                                           CD_Type.arrayType());
          bsmInvokeArguments[1] = ownerType.orElseThrow();
          bsmInvokeArguments[2] = rawType.orElseThrow();
          for (int i = 3; i < bsmInvokeArgumentsLength; i++) {
            final Optional<? extends ConstantDesc> arg = describeConstable(actualTypeArguments[i - 3]);
            if (arg.isEmpty()) {
              return Optional.empty();
            }
            bsmInvokeArguments[i] = arg.orElseThrow();
          }
          return Optional.of(DynamicConstantDesc.ofNamed(BSM_INVOKE,
                                                         DEFAULT_NAME,
                                                         CD_DefaultParameterizedType,
                                                         bsmInvokeArguments));
        }
      }
      return Optional.empty();
    }
  }

  /**
   * Returns an {@link Optional} containing the nominal descriptor in
   * the form of a {@link ConstantDesc} for the supplied {@link
   * GenericArrayType}, or an {@linkplain Optional#isEmpty() empty
   * <code>Optional</code>} if there is no such descriptor.
   *
   * @param g the {@link GenericArrayType} in question; may be
   * {@code null}
   *
   * @return an {@link Optional} as described above; never {@code
   * null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see Constable
   */
  private static final Optional<? extends ConstantDesc> describeConstable(final GenericArrayType g) {
    if (g == null) {
      return Optional.of(NULL);
    } else if (g instanceof ConstantDesc constantDesc) {
      return describeConstable(constantDesc);
    } else if (g instanceof Constable c) {
      return describeConstable(c);
    } else {
      final Optional<? extends ConstantDesc> genericComponentType = describeConstable(g.getGenericComponentType());
      if (genericComponentType.isPresent()) {
        return
          Optional.of(DynamicConstantDesc.ofNamed(BSM_INVOKE,
                                                  DEFAULT_NAME,
                                                  CD_DefaultGenericArrayType,
                                                  MethodHandleDesc.ofConstructor(CD_DefaultGenericArrayType,
                                                                                 CD_Type),
                                                  genericComponentType.orElseThrow()));
      }
      return Optional.empty();
    }
  }

  /**
   * Returns an {@link Optional} containing the nominal descriptor in
   * the form of a {@link ConstantDesc} for the supplied {@link
   * TypeVariable}, or an {@linkplain Optional#isEmpty() empty
   * <code>Optional</code>} if there is no such descriptor.
   *
   * @param tv the {@link TypeVariable} in question; may be {@code
   * null}
   *
   * @return an {@link Optional} as described above; never {@code
   * null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see Constable
   */
  private static final Optional<? extends ConstantDesc> describeConstable(final TypeVariable<?> tv) {
    if (tv == null) {
      return Optional.of(NULL);
    } else if (tv instanceof ConstantDesc constantDesc) {
      return describeConstable(constantDesc);
    } else if (tv instanceof Constable constable) {
      return describeConstable(constable);
    } else {
      final Optional<? extends ConstantDesc> gd = describeConstable(tv.getGenericDeclaration());
      if (gd.isPresent()) {
        final String name = tv.getName();
        return
          Optional.of(DynamicConstantDesc.ofNamed(BSM_INVOKE,
                                                  name,
                                                  CD_TypeVariable,
                                                  MethodHandleDesc.ofMethod(STATIC,
                                                                            Bootstraps.CD_Bootstraps,
                                                                            "getTypeVariable",
                                                                            MethodTypeDesc.of(CD_TypeVariable,
                                                                                              CD_GenericDeclaration,
                                                                                              CD_String)),
                                                  gd.orElseThrow(),
                                                  name));
      }
      return Optional.empty();
    }
  }

  /**
   * Returns an {@link Optional} containing the nominal descriptor in
   * the form of a {@link ConstantDesc} for the supplied {@link
   * WildcardType}, or an {@linkplain Optional#isEmpty() empty
   * <code>Optional</code>} if there is no such descriptor.
   *
   * @param w the {@link WildcardType} in question; may be {@code
   * null}
   *
   * @return an {@link Optional} as described above; never {@code
   * null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see Constable
   */
  private static final Optional<? extends ConstantDesc> describeConstable(final WildcardType w) {
    if (w == null) {
      return Optional.of(NULL);
    } else if (w instanceof ConstantDesc constantDesc) {
      return describeConstable(constantDesc);
    } else if (w instanceof Constable c) {
      return describeConstable(c);
    } else {
      final Type[] lowerBounds = w.getLowerBounds();
      if (lowerBounds.length <= 0) {
        final Type[] upperBounds = w.getUpperBounds();
        if (upperBounds.length <= 0 || (upperBounds.length == 1 && upperBounds[0] == Object.class)) {
          // Unbounded.
          return Optional.of(DynamicConstantDesc.ofNamed(BSM_GET_STATIC_FINAL,
                                                         "INSTANCE",
                                                         CD_WildcardType,
                                                         CD_UnboundedWildcardType));
        } else {
          // Upper bounded (extends).
          final int bsmInvokeArgumentsLength = upperBounds.length + 1;
          final ConstantDesc[] bsmInvokeArguments = new ConstantDesc[bsmInvokeArgumentsLength];
          bsmInvokeArguments[0] = MethodHandleDesc.ofConstructor(CD_UpperBoundedWildcardType, CD_Type.arrayType());
          for (int i = 1; i < bsmInvokeArgumentsLength; i++) {
            final Optional<? extends ConstantDesc> arg = describeConstable(upperBounds[i - 1]);
            if (arg.isEmpty()) {
              return Optional.empty();
            }
            bsmInvokeArguments[i] = arg.orElseThrow();
          }
          return Optional.of(DynamicConstantDesc.ofNamed(BSM_INVOKE,
                                                         DEFAULT_NAME,
                                                         CD_UpperBoundedWildcardType,
                                                         bsmInvokeArguments));
        }
      } else {
        // Lower bounded.
        final int bsmInvokeArgumentsLength = lowerBounds.length + 1;
        final ConstantDesc[] bsmInvokeArguments = new ConstantDesc[bsmInvokeArgumentsLength];
        bsmInvokeArguments[0] = MethodHandleDesc.ofConstructor(CD_LowerBoundedWildcardType, CD_Type.arrayType());
        for (int i = 1; i < bsmInvokeArgumentsLength; i++) {
          final Optional<? extends ConstantDesc> arg = describeConstable(lowerBounds[i - 1]);
          if (arg.isEmpty()) {
            return Optional.empty();
          }
          bsmInvokeArguments[i] = arg.orElseThrow();
        }
        return Optional.of(DynamicConstantDesc.ofNamed(BSM_INVOKE,
                                                       DEFAULT_NAME,
                                                       CD_LowerBoundedWildcardType,
                                                       bsmInvokeArguments));
      }
    }
  }

  /**
   * Returns an {@link Optional} containing the nominal descriptor in
   * the form of a {@link ConstantDesc} for the supplied {@link
   * GenericDeclaration}, or an {@linkplain Optional#isEmpty() empty
   * <code>Optional</code>} if there is no such descriptor.
   *
   * @param gd the {@link GenericDeclaration} in question; must be
   * either {@code null}, a {@link Class}, a {@link Constructor} or a
   * {@link Method}
   *
   * @return an {@link Optional} as described above; never {@code
   * null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see Constable
   */
  static final Optional<? extends ConstantDesc> describeConstable(final GenericDeclaration gd) {
    if (gd instanceof ConstantDesc constantDesc) {
      return describeConstable(constantDesc);
    } else if (gd instanceof Constable constable) {
      // Includes Class<?>
      return describeConstable(constable);
    } else if (gd instanceof Constructor<?> c) {
      return describeConstable(c);
    } else if (gd instanceof Method m) {
      return describeConstable(m);
    } else {
      return Optional.empty();
    }
  }

  /**
   * Returns an {@link Optional} containing the nominal descriptor in
   * the form of a {@link ConstantDesc} for the supplied {@link
   * Constructor}, or an {@linkplain Optional#isEmpty() empty
   * <code>Optional</code>} if there is no such descriptor.
   *
   * @param constructor the {@link Constructor} in question; may be
   * {@code null}
   *
   * @return an {@link Optional} as described above; never {@code
   * null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see Constable
   */
  private static final Optional<? extends ConstantDesc> describeConstable(final Constructor<?> constructor) {
    Optional<? extends ConstantDesc> c = null;
    try {
      c = MethodHandles.lookup().unreflectConstructor(constructor).describeConstable();
    } catch (final IllegalAccessException illegalAccessException) {
      c = Optional.empty();
    }
    if (c != null && c.isPresent()) {
      return
        Optional.of(DynamicConstantDesc.ofNamed(BSM_INVOKE,
                                                DEFAULT_NAME,
                                                CD_Constructor,
                                                DMHD_REFLECT_AS,
                                                CD_Constructor,
                                                c.orElseThrow()));
    }
    return Optional.empty();
  }

  /**
   * Returns an {@link Optional} containing the nominal descriptor in
   * the form of a {@link ConstantDesc} for the supplied {@link Method},
   * or an {@linkplain Optional#isEmpty() empty <code>Optional</code>}
   * if there is no such descriptor.
   *
   * @param method the {@link Method} in question; may be {@code null}
   *
   * @return an {@link Optional} as described above; never {@code
   * null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see Constable
   */
  static final Optional<? extends ConstantDesc> describeConstable(final Method method) {
    Optional<? extends ConstantDesc> m = null;
    try {
      m = MethodHandles.lookup().unreflect(method).describeConstable();
    } catch (final IllegalAccessException illegalAccessException) {
      m = Optional.empty();
    }
    if (m != null && m.isPresent()) {
      return
        Optional.of(DynamicConstantDesc.ofNamed(BSM_INVOKE,
                                                DEFAULT_NAME,
                                                CD_Method,
                                                DMHD_REFLECT_AS,
                                                CD_Method,
                                                m.orElseThrow()));
    }
    return Optional.empty();
  }

}
