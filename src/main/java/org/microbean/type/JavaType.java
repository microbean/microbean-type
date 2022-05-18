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

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;

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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import java.util.function.Function;

import org.microbean.constant.Constables;

import org.microbean.development.annotation.Convenience;
import org.microbean.development.annotation.Experimental;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_List;
import static java.lang.constant.ConstantDescs.FALSE;
import static java.lang.constant.ConstantDescs.TRUE;

import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC;

import static org.microbean.type.ConstantDescs.CD_JavaType;
import static org.microbean.type.ConstantDescs.CD_Type;

/**
 * A {@link org.microbean.type.Type} that models a {@link
 * java.lang.reflect.Type java.lang.reflect.Type} for use primarily by
 * a {@link org.microbean.type.Type.Semantics} instance.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see org.microbean.type.Type.Semantics
 */
@Experimental
public class JavaType extends org.microbean.type.Type<Type> implements Constable {


  /*
   * Instance fields.
   */


  private final boolean box;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link JavaType}.
   *
   * @param box whether boxing of primitive types will be in effect;
   * even if {@code true} boxing only happens to primitive types
   *
   * @param type the Java {@link Type} being modeled; must not be {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}
   */
  protected JavaType(final boolean box, final Type type) {
    super(box ? JavaTypes.box(type) : type);
    this.box = box;
  }

  /**
   * Creates a new {@link JavaType} that models custom supertypes.
   *
   * <p>Among other things, this means that the {@link #object()}
   * method will return {@code null}.</p>
   *
   * @param box whether boxing of primitive types will be in effect;
   * even if {@code true} boxing only happens to primitive types
   *
   * @param supertypes a {@link List} of supertypes; must not be
   * {@code null}
   *
   * @exception NullPointerException if {@code supertypes} is {@code
   * null}
   */
  protected JavaType(final boolean box, final List<?> supertypes) {
    super(map(supertypes, box ? JavaType::ofBoxed : JavaType::of));
    this.box = box;
  }


  /*
   * Instance methods.
   */


  /**
   * Returns {@code true} if and only if this {@link JavaType}
   * represents a Java type that has a name.
   *
   * <p>In the Java reflective type system, only {@link Class} and
   * {@link TypeVariable} instances have names.</p>
   *
   * @return {@code true} if and only if this {@link JavaType} represents
   * a Java type that has a name
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #name()
   *
   * @see Class#getName()
   *
   * @see TypeVariable#getName()
   */
  @Override // org.microbean.type.Type<Type>
  public final boolean named() {
    final Type type = this.object();
    return type instanceof Class || type instanceof TypeVariable;
  }

  /**
   * Returns the name of this {@link JavaType} if it has one
   * <strong>or {@code null} if it does not</strong>.
   *
   * <p>Only classes and type variables in the Java reflective type
   * system have names.</p>
   *
   * @return the name of this {@link JavaType}, or {@code null}
   *
   * @nullability This method and its overrides may, and often will,
   * return {@code null}.
   *
   * @idempotency This method is, and its overrides must be, idempotent
   * and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   */
  @Override // org.microbean.type.Type<Type>
  public final String name() {
    final Object type = this.object();
    if (type == null) {
      return null;
    } else if (type instanceof Class<?> c) {
      return c.getName();
    } else if (type instanceof TypeVariable<?> tv) {
      return tv.getName();
    }
    return null;
  }

  /**
   * Returns {@code true} if and only if the return value of {@link
   * #object()} is identical to {@link Object Object.class}.
   *
   * @return {@code true} if and only if the return value of {@link
   * #object()} is identical to {@link Object Object.class}
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // org.microbean.type.Type<Type>
  public final boolean top() {
    return this.object() == Object.class;
  }

  /**
   * Returns a {@link JavaType}, <strong>usually new</strong>, whose
   * {@linkplain #object() modeled type} is {@linkplain
   * JavaTypes#equals(Type, Type) equal to} the supplied {@link Type}.
   *
   * @param type the new {@link Type}; must not be {@code null}
   *
   * @return a {@link JavaType}, usually new
   *
   * @exception NullPointerException if {@code type} is {@code null}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #withBox(boolean)
   *
   * @see #of(boolean, Type)
   */
  @Override // org.microbean.type.Type<Type>
  public final JavaType withObject(final Type type) {
    return of(this.box, type);
  }

  /**
   * Returns a {@link JavaType}, <strong>usually new</strong>, whose
   * boxing strategy is modeled by the supplied {@code boolean}.
   *
   * @param box the new boxing strategy
   *
   * @return a {@link JavaType}, usually new
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #withObject(Type)
   *
   * @see #of(boolean, Type)
   */
  public final JavaType withBox(final boolean box) {
    return of(box, this.object());
  }

  /**
   * Returns an {@linkplain
   * Collections#unmodifiableList(List) unmodifiable and
   * immutable <code>List</code>} of the computed <em>direct
   * supertypes</em> of this {@link JavaType}, or an {@linkplain
   * Collection#isEmpty() empty <code>List</code>} if there are
   * no direct supertypes.
   *
   * <p>This implementation uses the {@link
   * JavaTypes#directSupertypes(Type)} method.</p>
   *
   * @return an {@linkplain
   * Collections#unmodifiableList(List) unmodifiable and
   * immutable <code>List</code>} of the computed <em>direct
   * supertypes</em> of this {@link JavaType}; never {@code null}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   */
  @Override // org.microbean.type.Type<Type>
  protected final List<? extends org.microbean.type.Type<Type>> computeDirectSupertypes() {
    final Type type = this.object();
    return type == null ? List.of() : map(JavaTypes.directSupertypes(type), this::withObject);
  }

  /**
   * If this {@link JavaType} represents a {@link ParameterizedType}
   * or a {@link GenericArrayType} returns a {@link JavaType}
   * representing its {@linkplain ParameterizedType#getRawType() raw
   * type} or {@linkplain GenericArrayType#getGenericComponentType()
   * generic component type}, or, if this {@link JavaType} does not
   * represent a {@link ParameterizedType} or a {@link
   * GenericArrayType}, returns {@code this}.
   *
   * @return a suitable {@link JavaType}; never {@code null}; often {@code
   * this}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   */
  @Override // org.microbean.type.Type<Type>
  public final JavaType type() {
    final Object type = this.object();
    if (type instanceof ParameterizedType p) {
      return this.withObject(p.getRawType());
    } else if (type instanceof GenericArrayType g) {
      return this.withObject(g.getGenericComponentType());
    } else {
      return this;
    }
  }

  /**
   * Returns the owner of this {@link JavaType}, or {@code null} if
   * there is no such {@link Owner}.
   *
   * @return the owner of this {@link JavaType}, or {@code null}
   *
   * @nullability This method may return {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @Experimental
  @Override // org.microbean.type.Type<Type>
  public final Owner<Type> owner() {
    final Object type = this.object();
    if (type == null) {
      return null;
    } else if (type instanceof Class<?> c) {
      final Class<?> enclosingClass = c.getEnclosingClass();
      return enclosingClass == null ? null : this.withObject(enclosingClass);
    } else if (type instanceof ParameterizedType p) {
      final Type ownerType = p.getOwnerType();
      return ownerType == null ? null : this.withObject(ownerType);
    } else if (type instanceof TypeVariable<?> tv) {
      final GenericDeclaration gd = tv.getGenericDeclaration();
      if (gd instanceof Class<?> c) {
        return this.withObject(c);
      } else if (gd instanceof Executable e) {
        return new JavaExecutable(e, this.box);
      } else {
        throw new AssertionError("gd: " + gd);
      }
    } else {
      return null;
    }
  }

  /**
   * Returns {@code true} if and only if this {@link JavaType}
   * represents a generic {@link Class} by virtue of having
   * {@linkplain Class#getTypeParameters() type parameters}.
   *
   * <p>This implementation checks to see if the return value of
   * {@link #object()} is an instance of {@link Class} and, if so, if
   * that {@link Class} {@linkplain Class#getTypeParameters() has any
   * type parameters}.
   *
   * @return {@code true} if and only if this {@link Type} represents
   * a generic class by virtue of having {@linkplain
   * Class#getTypeParameters() type parameters}
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @see #object()
   *
   * @see #typeParameters()
   */
  @Override // org.microbean.type.Type<Type>
  public final boolean hasTypeParameters() {
    return this.object() instanceof Class<?> c && c.getTypeParameters().length > 0;
  }

  /**
   * Returns {@code true} if and only if this {@link Type} represents
   * a {@link ParameterizedType} by virtue of {@linkplain
   * ParameterizedType#getActualTypeArguments() having type
   * arguments}.
   *
   * <p>This implementation checks to see if the return value of
   * {@link #object()} is a {@link ParameterizedType}.</p>
   *
   * @return {@code true} if and only if this {@link Type} represents
   * a parameterized type by virtue of having {@linkplain
   * ParameterizedType#getActualTypeArguments() type arguments}
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @see #object()
   *
   * @see #typeArguments()
   */
  @Override // org.microbean.type.Type<Type>
  public final boolean hasTypeArguments() {
    return this.object() instanceof ParameterizedType;
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s type
   * arguments.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link JavaType} models a
   * {@link ParameterizedType}.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s
   * {@linkplain ParameterizedType#getActualTypeArguments() type
   * arguments}; never {@code null}
   *
   * @nullability This method does not return {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @Override // org.microbean.type.Type<Type>
  public final List<? extends JavaType> typeArguments() {
    return this.object() instanceof ParameterizedType p ? map(p.getActualTypeArguments(), this::withObject) : List.of();
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s type
   * parameters.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link JavaType} models a generic
   * {@link Class}.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s
   * {@linkplain Class#getTypeParameters() type parameters}; never
   * {@code null}
   *
   * @nullability This method does not return {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @Override // org.microbean.type.Type<Type>
  public final List<? extends JavaType> typeParameters() {
    return this.object() instanceof Class<?> c ? map(c.getTypeParameters(), this::withObject) : List.of();
  }

  /**
   * Returns the {@linkplain Class#getComponentType() component type}
   * of this {@link JavaType}, if there is one, <strong>or {@code
   * null} if there is not</strong>.
   *
   * <p>This method returns a non-{@code null} result only when this
   * {@link JavaType} represents a {@link Type} that is either
   * {@linkplain Class#isArray() an array} or a {@link
   * GenericArrayType}.</p>
   *
   * @return the {@linkplain Class#getComponentType() component type}
   * of this {@link JavaType}, if there is one, or {@code null} if
   * there is not
   *
   * @nullability This method does not return {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @see Class#getComponentType()
   *
   * @see GenericArrayType#getGenericComponentType()
   */
  @Override // org.microbean.type.Type<Type>
  public final JavaType componentType() {
    Type type = this.object();
    if (type == null) {
      return null;
    } else if (type instanceof Class<?> c) {
      type = c.getComponentType();
    } else if (type instanceof GenericArrayType g) {
      type = g.getGenericComponentType();
    } else {
      return null;
    }
    return type == null ? null : this.withObject(type);
  }

  /**
   * Returns {@code true} if and only if this {@link JavaType}
   * represents either a {@link TypeVariable} or a {@link
   * WildcardType}.
   *
   * <p>This implementation checks to see if the return value of
   * {@link #object()} is either a {@link TypeVariable} or a {@link
   * WildcardType}.</p>
   *
   * @return {@code true} if and only if this {@link JavaType}
   * represents either a {@link TypeVariable} or a {@link
   * WildcardType}
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #upperBounds()
   */
  @Override // org.microbean.type.Type<Type>
  public final boolean upperBounded() {
    final Object type = this.object();
    return type instanceof WildcardType || type instanceof TypeVariable;
  }

  /**
   * Returns {@code true} if and only if this {@link Type} represents
   * a {@link WildcardType} {@linkplain WildcardType#getLowerBounds()
   * with a lower bound}.
   *
   * <p>This implementation checks to see if the return value of
   * {@link #object()} is a {@link WildcardType}, and, if so, if that
   * {@link WildcardType} {@linkplain WildcardType#getLowerBounds()
   * has a lower bound}.</p>
   *
   * @return {@code true} if and only if this {@link JavaType}
   * represents a {@link WildcardType} {@linkplain
   * WildcardType#getLowerBounds() with a lower bound}
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #lowerBounds()
   *
   * @see WildcardType#getLowerBounds()
   */
  @Override // org.microbean.type.Type<Type>
  public final boolean lowerBounded() {
    return this.object() instanceof WildcardType w && w.getLowerBounds().length > 0;
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s lower
   * bounds.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link Type} models a {@link
   * WildcardType} {@linkplain WildcardType#getLowerBounds() with a
   * lower bound}.</p>
   *
   * <p>The returned {@link List} will contain at most one
   * element.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s lower
   * bounds; never {@code null}; often {@linkplain List#isEmpty()
   * empty}
   *
   * @nullability This method does not return {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @see WildcardType#getLowerBounds()
   */
  @Override // org.microbean.type.Type<Type>
  public final List<? extends JavaType> lowerBounds() {
    return this.object() instanceof WildcardType w ? map(w.getLowerBounds(), this::withObject) : List.of();
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s upper
   * bounds.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link Type} models either a {@link
   * WildcardType} or a {@link TypeVariable}.
   *
   * <p>The returned {@link List} will contain at most one
   * element.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s lower
   * bounds; never {@code null}; often {@linkplain List#isEmpty()
   * empty}
   *
   * @nullability This method does not return {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @see TypeVariable#getBounds()
   *
   * @see WildcardType#getUpperBounds()
   */
  @Override // org.microbean.type.Type<Type>
  public final List<? extends JavaType> upperBounds() {
    final Object type = this.object();
    if (type instanceof WildcardType w) {
      return map(w.getUpperBounds(), this::withObject);
    } else if (type instanceof TypeVariable<?> t) {
      return map(t.getBounds(), this::withObject);
    }
    return List.of();
  }

  @Override // Owner<Type>
  public final boolean objectEquals(final Object other) {
    return
      this.object() == other ||
      other instanceof Type && JavaTypes.equals(this.object(), this.box ? JavaTypes.box((Type)other) : (Type)other);
  }

  @Override // Constable
  public final Optional<? extends ConstantDesc> describeConstable() {
    final ConstantDesc boxCd = this.box ? TRUE : FALSE;
    if (this.customSupertyped()) {
      final ConstantDesc supertypesCd = Constables.describeConstable(this.supertypes()).orElse(null);
      if (supertypesCd != null) {
        return
          Optional.of(DynamicConstantDesc.of(BSM_INVOKE,
                                             MethodHandleDesc.ofMethod(STATIC,
                                                                       CD_JavaType,
                                                                       "ofExactly",
                                                                       MethodTypeDesc.of(CD_JavaType, CD_boolean, CD_List)),
                                             boxCd,
                                             supertypesCd));
      }
    } else {
      final Type object = this.object();
      if (object != null) {
        final ConstantDesc objectCd = JavaTypes.describeConstable(object).orElse(null);
        if (objectCd != null) {
          return
            Optional.of(DynamicConstantDesc.of(BSM_INVOKE,
                                               MethodHandleDesc.ofMethod(STATIC,
                                                                         CD_JavaType,
                                                                         "of",
                                                                         MethodTypeDesc.of(CD_JavaType, CD_Type)),
                                               objectCd));
        }
      }
    }
    return Optional.empty();
  }


  /*
   * Static methods.
   */


  /**
   * Returns {@code true} if and only if the {@linkplain
   * JavaTypes#erase(Type) type erasure} of the supplied {@link
   * org.microbean.type.Type}'s {@linkplain #object() modeled type}
   * {@linkplain Class#isInterface() is an interface}.
   *
   * <p>This method is most commonly used as a method reference
   * supplied to an invocation of the {@link
   * #mostSpecialized(Predicate)} method.</p>
   *
   * @param t a {@link org.microbean.type.Type} whose {@linkplain
   * #object() modeled type} is a {@link Type java.lang.reflect.Type};
   * must not be {@code null}
   *
   * @return {@code true} if and only if the {@linkplain
   * JavaTypes#erase(Type) type erasure} of the supplied {@link
   * org.microbean.type.Type}'s {@linkplain #object() modeled type}
   * {@linkplain Class#isInterface() is an interface}
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @see #mostSpecialized(Predicate)
   */
  @Convenience
  public static final boolean interfaceType(final org.microbean.type.Type<Type> t) {
    final Class<?> c = JavaTypes.erase(t.object());
    return c != null && c.isInterface();
  }

  /**
   * Returns {@code true} if and only if the {@linkplain
   * JavaTypes#erase(Type) type erasure} of the supplied {@link
   * org.microbean.type.Type}'s {@linkplain #object() modeled type}
   * {@linkplain Class#isInterface() is not an interface}.
   *
   * <p>This method is most commonly used as a method reference
   * supplied to an invocation of the {@link
   * #mostSpecialized(Predicate)} method.</p>
   *
   * @param t a {@link org.microbean.type.Type} whose {@linkplain
   * #object() modeled type} is a {@link Type java.lang.reflect.Type};
   * must not be {@code null}
   *
   * @return {@code true} if and only if the {@linkplain
   * JavaTypes#erase(Type) type erasure} of the supplied {@link
   * org.microbean.type.Type}'s {@linkplain #object() modeled type}
   * {@linkplain Class#isInterface() is not an interface}
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @see #mostSpecialized(Predicate)
   */
  @Convenience
  public static final boolean nonInterfaceType(final org.microbean.type.Type<Type> t) {
    final Class<?> c = JavaTypes.erase(t.object());
    return c != null && !c.isInterface();
  }

  private static final JavaType of(final Object o) {
    return of(false, o);
  }

  private static final JavaType ofBoxed(final Object o) {
    return of(true, o);
  }

  @SuppressWarnings("unchecked")
  private static final JavaType of(final boolean box, final Object o) {
    if (o instanceof Type type) {
      return of(box, type);
    } else if (o instanceof Token<?> token) {
      return of(box, token);
    } else if (o instanceof org.microbean.type.Type<?> type) {
      return of(box, (org.microbean.type.Type<? extends Type>)type);
    } else {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param type a {@link Token} representing the type to model; must
   * not be {@code null}
   *
   * @return a new {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(boolean, Token)
   */
  public static final JavaType of(final Token<?> type) {
    return of(false, type);
  }

  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param box whether autoboxing is enabled
   *
   * @param type a {@link Token} representing the type to model; must
   * not be {@code null}
   *
   * @return a new {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(boolean, Type)
   */
  public static final JavaType of(final boolean box, final Token<?> type) {
    return of(box, type.type());
  }

  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param type the {@link Type} that will be modeled; must not be
   * {@code null}
   *
   * @return a new {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(boolean, Type)
   */
  public static final JavaType of(final Type type) {
    return of(false, type);
  }

  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param box whether autoboxing is enabled
   *
   * @param type the {@link Type} that will be modeled; must not be
   * {@code null}
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  // This method is used by describeConstable().
  public static final JavaType of(final boolean box, final Type type) {
    return new JavaType(box, type);
  }

  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param type the {@link org.microbean.type.Type} whose {@linkplain
   * org.microbean.type.Type#object() represented <code>Type</code>}
   * will be modeled; must not be {@code null}
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(boolean, org.microbean.type.Type)
   */
  public static final JavaType of(final org.microbean.type.Type<? extends Type> type) {
    return of(false, type);
  }

  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param box whether autoboxing is enabled
   *
   * @param type the {@link org.microbean.type.Type} whose {@linkplain
   * org.microbean.type.Type#object() represented <code>Type</code>}
   * will be modeled; must not be {@code null}
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(boolean, Type)
   */
  public static final JavaType of(final boolean box, final org.microbean.type.Type<? extends Type> type) {
    if (type instanceof JavaType jt && box == jt.box) {
      return jt;
    }
    return of(box, type.object());
  }

  /**
   * Returns a {@linkplain #customSupertyped() custom supertyped}
   * {@link JavaType} suitable for the supplied arguments.
   *
   * @param supertypes a {@link List} of custom supertypes; must not
   * be {@code null}
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code supertypes} is {@code
   * null}.
   *
   * @exception IllegalArgumentException if any supertype is neither a
   * {@link Type} nor a {@link Token} nor a {@link JavaType}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #ofExactly(boolean, List)
   */
  public static final JavaType ofExactly(final List<?> supertypes) {
    return ofExactly(false, supertypes);
  }

  /**
   * Returns a {@linkplain #customSupertyped() custom supertyped}
   * {@link JavaType} suitable for the supplied arguments.
   *
   * @param box whether autoboxing is enabled
   *
   * @param supertypes a {@link List} of custom supertypes; must not
   * be {@code null}
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code supertypes} is {@code
   * null}.
   *
   * @exception IllegalArgumentException if any supertype is neither a
   * {@link Type} nor a {@link Token} nor a {@link JavaType}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(boolean, org.microbean.type.Type)
   */
  // This method is used by describeConstable().
  public static final JavaType ofExactly(final boolean box, final List<?> supertypes) {
    return new JavaType(box, supertypes);
  }

  /**
   * Returns a {@linkplain #customSupertyped() custom supertyped}
   * {@link JavaType} suitable for the supplied arguments.
   *
   * @param type the sole supertype the returned {@link JavaType} will
   * represent; must not be {@code null}
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code
   * null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #ofExactly(boolean, Type)
   */
  public static final JavaType ofExactly(final Type type) {
    return ofExactly(false, List.of(type));
  }

  /**
   * Returns a {@linkplain #customSupertyped() custom supertyped}
   * {@link JavaType} suitable for the supplied arguments.
   *
   * @param type the sole supertype the returned {@link JavaType} will
   * represent; must not be {@code null}
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code
   * null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #ofExactly(boolean, Type)
   */
  public static final JavaType ofExactly(final Token<?> type) {
    return ofExactly(false, List.of(type.type()));
  }

  /**
   * Returns a {@linkplain #customSupertyped() custom supertyped}
   * {@link JavaType} suitable for the supplied arguments.
   *
   * @param box whether autoboxing is enabled
   *
   * @param type the sole supertype the returned {@link JavaType} will
   * represent; must not be {@code null}
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code
   * null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #ofExactly(boolean, List)
   */
  public static final JavaType ofExactly(final boolean box, final Type type) {
    return ofExactly(box, List.of(type));
  }

  /**
   * Returns a {@linkplain #customSupertyped() custom supertyped}
   * {@link JavaType} suitable for the supplied arguments.
   *
   * @param box whether autoboxing is enabled
   *
   * @param type the sole supertype the returned {@link JavaType} will
   * represent; must not be {@code null}
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code
   * null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #ofExactly(boolean, List)
   */
  public static final JavaType ofExactly(final boolean box, final Token<?> type) {
    return ofExactly(box, List.of(type.type()));
  }

  /**
   * Returns a {@linkplain #customSupertyped() custom supertyped}
   * {@link JavaType} suitable for the supplied arguments.
   *
   * @param supertypes custom supertypes; must not be {@code null}
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code supertypes} is {@code
   * null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #ofExactly(boolean, List)
   */
  public static final JavaType ofExactly(final Object... supertypes) {
    return ofExactly(false, List.of(supertypes));
  }

  /**
   * Returns a {@linkplain #customSupertyped() custom supertyped}
   * {@link JavaType} suitable for the supplied arguments.
   *
   * @param box whether autoboxing is enabled
   *
   * @param supertypes custom supertypes; must not be {@code null}
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code supertypes} is {@code
   * null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #ofExactly(boolean, List)
   */
  public static final JavaType ofExactly(final boolean box, final Object... supertypes) {
    return ofExactly(box, List.of(supertypes));
  }

  /**
   * Returns a boxed version of the supplied {@link
   * org.microbean.type.Type}, if appropriate.
   *
   * @param type the {@link org.microbean.type.Type}; must not be
   * {@code null}
   *
   * @return a boxed version of the supplied {@link
   * org.microbean.type.Type}, if appropriate
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is deterministic, but not necessarily
   * idempotent.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(boolean, org.microbean.type.Type)
   *
   * @see JavaTypes#box(Type)
   */
  public static final JavaType box(final org.microbean.type.Type<? extends Type> type) {
    return of(true, type);
  }

  /**
   * Returns a {@link JavaType} representing a boxed version of the
   * supplied {@link Type}, if appropriate.
   *
   * @param type the {@link Type}; must not be {@code null}
   *
   * @return a {@link JavaType} representing a boxed version of the
   * supplied {@link Type}, if appropriate
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is deterministic, but not necessarily
   * idempotent.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(boolean, Type)
   *
   * @see JavaTypes#box(Type)
   */
  public static final JavaType box(final Type type) {
    return of(true, type);
  }


  /*
   * Inner and nested classes.
   */


  /**
   * A holder of a {@link Type} that embodies <a
   * href="http://gafter.blogspot.com/2006/12/super-type-tokens.html"
   * target="_parent">Gafter's gadget</a>.
   *
   * <p>To use this class, create a new instance of an anonymous
   * subclass of it, and then call {@link #type() type()} on it.  For
   * example:</p>
   *
   * <blockquote><pre>
   * // type will be a {@link ParameterizedType} whose {@link ParameterizedType#getRawType() rawType} is {@link java.util.List List.class} and
   * // whose {@linkplain ParameterizedType#getActualTypeArguments() sole type argument} is {@link String String.class}
   * {@link Type} type = new {@link Token Token}&lt;{@link java.util.List List}&lt;{@link String}&gt;&gt;() {}.{@link #type() type()};</pre></blockquote>
   *
   * @param <T> the modeled type; often parameterized
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see #type()
   */
  public static abstract class Token<T> {


    /*
     * Instance fields.
     */


    private final Type type;


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link Token}.
     */
    protected Token() {
      super();
      this.type = mostSpecializedParameterizedSuperclass(this.getClass()).getActualTypeArguments()[0];
    }


    /*
     * Instance methods.
     */


    /**
     * Returns the {@link Type} modeled by this {@link Token}.
     *
     * @return the {@link Type} modeled by this {@link Token}; never
     * {@code null}
     *
     * @nullability This method never returns {@code null}.
     *
     * @threadsafety This method is safe for concurrent use by multiple
     * threads.
     *
     * @idempotency This method is idempotent and deterministic.
     */
    public final Type type() {
      return this.type;
    }

    /**
     * Returns the {@linkplain JavaTypes#erase(Type) type erasure} of
     * this {@link Token}'s {@linkplain #type() modeled
     * <code>Type</code>}, or {@code null} if erasing the {@link Type}
     * would result in a non-{@link Class} erasure (in which case the
     * erasure is simply the {@link Type} itself), or if an erasure
     * cannot be determined.
     *
     * @return the {@linkplain JavaTypes#erase(Type) type erasure} of
     * this {@link Token}'s {@linkplain #type() modeled
     * <code>Type</code>}, or {@code null} if erasing the {@link Type}
     * would result in a non-{@link Class} erasure, or if an erasure
     * cannot be determined
     *
     * @nullability This method never returns {@code null}.
     *
     * @threadsafety This method is safe for concurrent use by multiple
     * threads.
     *
     * @idempotency This method is idempotent and deterministic.
     */
    public final Class<?> erase() {
      return JavaTypes.erase(this.type());
    }

    /**
     * Returns a hashcode for this {@link Token} computed from the
     * {@link Type} it {@linkplain #type() models}.
     *
     * @return a hashcode for this {@link Token}
     *
     * @threadsafety This method is, and its overrides must be, safe for
     * concurrent use by multiple threads.
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @see #equals(Object)
     */
    @Override // Object
    public int hashCode() {
      final Type type = this.type();
      return type == null ? 0 : type.hashCode();
    }

    /**
     * Returns {@code true} if the supplied {@link Object} is equal to
     * this {@link Token}.
     *
     * <p>This method returns {@code true} if the supplied {@link
     * Object}'s {@linkplain Object#getClass() class} is this {@link
     * Token}'s class and if its {@linkplain #type() modeled
     * <code>Type</code>} is equal to this {@link Token}'s
     * {@linkplain #type() modeled <code>Type</code>}.</p>
     *
     * @param other the {@link Object} to test; may be {@code null} in
     * which case {@code false} will be returned
     *
     * @return {@code true} if the supplied {@link Object} is equal to
     * this {@link Token}; {@code false} otherwise
     *
     * @threadsafety This method is, and its overrides must be, safe for
     * concurrent use by multiple threads.
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @see #hashCode()
     */
    @Override // Object
    public boolean equals(final Object other) {
      if (other == this) {
        return true;
      } else if (other instanceof Token<?> tt) {
        return Objects.equals(this.type(), tt.type());
      } else {
        return false;
      }
    }

    /**
     * Returns a {@link String} representation of this {@link
     * Token}.
     *
     * <p>This method returns a value equal to that returned by {@link
     * Type#getTypeName() this.type().getTypeName()}.</p>
     *
     * @return a {@link String} representation of this {@link
     * Token}; never {@code null}
     *
     * @nullability This method does not, and its overrides must not,
     * return {@code null}.
     *
     * @threadsafety This method is, and its overrides must be, safe for
     * concurrent use by multiple threads.
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     */
    @Override // Object
    public String toString() {
      final Type type = this.type();
      return type == null ? "null" : type.getTypeName();
    }


    /*
     * Static methods.
     */


    private static final ParameterizedType mostSpecializedParameterizedSuperclass(final Type type) {
      if (type == null || type == Object.class || type == Token.class) {
        return null;
      } else {
        final Class<?> erasure = JavaTypes.erase(type);
        if (erasure == null || erasure == Object.class || !(Token.class.isAssignableFrom(erasure))) {
          return null;
        } else {
          return type instanceof ParameterizedType p ? p : mostSpecializedParameterizedSuperclass(erasure.getGenericSuperclass());
        }
      }
    }

  }

}
