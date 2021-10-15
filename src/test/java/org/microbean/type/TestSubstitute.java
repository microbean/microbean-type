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
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.List;
import java.util.Objects;

import java.util.function.BiFunction;

import jakarta.enterprise.util.TypeLiteral;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.microbean.type.Types.FreshTypeVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestSubstitute {

  private TestSubstitute() {
    super();
  }

  @Test
  final void explore() {
    // https://stackoverflow.com/questions/46050311/second-order-generics-seem-to-behave-differently-than-first-order-generics/46061947#46061947

    // Let G name a generic type declaration...
    final Class<?> G = C.class;

    // ...with n type parameters A1...An...
    final TypeVariable<?>[] A = (TypeVariable<?>[])G.getTypeParameters();
    assertNotNull(A);
    assertEquals(2, A.length);

    final TypeVariable<?> V = A[0];
    assertEquals("V", V.getName());
    final TypeVariable<?> A1 = V;

    final TypeVariable<?> W = A[1];
    assertEquals("W", W.getName());
    final TypeVariable<?> A2 = W;

    // ...with corresponding bounds U1...Un.
    final Class<?> vUpperBound = (Class<?>)V.getBounds()[0];
    assertSame(Object.class, vUpperBound);
    final Class<?> U1 = vUpperBound;

    final ParameterizedType wUpperBound = (ParameterizedType)W.getBounds()[0];
    assertEquals(List.class, wUpperBound.getRawType());
    assertSame(V, wUpperBound.getActualTypeArguments()[0]);
    assertSame(A1, V);
    final ParameterizedType U2 = wUpperBound;

    // There exists a capture conversion from a parameterized type G<T1,...,Tn>...
    final ParameterizedType GT = (ParameterizedType)new TypeLiteral<C<Number, ?>>() {}.getType();
    assertSame(G, GT.getRawType());
    assertEquals(2, GT.getActualTypeArguments().length);

    final Class<?> T1 = (Class<?>)GT.getActualTypeArguments()[0];
    assertSame(Number.class, T1);

    final WildcardType T2 = (WildcardType)GT.getActualTypeArguments()[1];
    assertEquals(0, T2.getLowerBounds().length);
    assertSame(Object.class, T2.getUpperBounds()[0]);

  }

  @Test
  final void testSubstituteNullTo() {
    // You can't specify the null type as a valid substitution.
    assertThrows(NullPointerException.class,
                 () -> Types.substitute(new TypeLiteral<List<CharSequence>>() {}.getType(), Number.class, null));
  }
  
  @Test
  final void testSubstituteNullIn() {
    final Type result = Types.substitute(null, Number.class, String.class);
    // The null type equals no other non-null type so the substitution
    // never happens.
    assertNull(result);
  }

  @Test
  final void testSubstituteNullInNullFrom() {
    final Type result = Types.substitute(null, null, String.class);
    // null == null so the substitution of String.class is applied and
    // returned
    assertSame(String.class, result);
  }

  @Test
  final void testSubstituteAllNulls() {
    // to is null so this throws a NullPointerException.
    assertThrows(NullPointerException.class,
                 () -> Types.substitute(null, null, null));
  }

  @Test
  final void testSubstituteInEqualsFrom() {
    final Type result = Types.substitute(Object.class, Object.class, String.class);
    assertSame(String.class, result);
  }

  @Test
  final void testSubstituteAllSame() {
    final Type result = Types.substitute(Object.class, Object.class, Object.class);
    assertSame(Object.class, result);
  }

  @Test
  final void testSubstituteFromEqualsTo() {
    final Type result = Types.substitute(Object.class, String.class, String.class);
    assertSame(Object.class, result);
  }

  @Test
  final void testSubstituteTypeArgument() {
    final DefaultParameterizedType result =
      (DefaultParameterizedType)Types.substitute(new TypeLiteral<List<CharSequence>>() {}.getType(),
                                                 CharSequence.class,
                                                 String.class);
    assertNotNull(result);
    assertSame(List.class, result.getRawType());
    assertSame(String.class, result.getActualTypeArguments()[0]);
  }

  @Test
  final void testOwnerTypeAndRawTypeAreNeverSubstituted() {
    final ParameterizedType in = (ParameterizedType)new TypeLiteral<List<CharSequence>>() {}.getType();
    assertNull(in.getOwnerType());
    final ParameterizedType result = (ParameterizedType)Types.substitute(in, List.class, Class.class);
    assertSame(in, result);
  }

  @Test
  final void testSubstituteInIsolation() throws ReflectiveOperationException {

    // As detailed
    // [elsewhere](https://stackoverflow.com/a/46061947/208288), the
    // substitution means Ui, the bound of a new type variable Si, is
    // the bound of Ai (its corresponding type parameter) with the
    // substitution of each similarly substituted type argument (S1,
    // S2, etc.) for each corresponding type parameter (A1, A2, etc.).
    // So U2, the bound of S2, is the bound of A2, and if A2 features
    // references to A1 and A3, then its substitution, S2, will
    // feature references to S1 and S3.
    
    final TypeVariable<?>[] typeParameters = C.class.getTypeParameters();
    assertEquals(2, typeParameters.length);
    final TypeVariable<?> V = typeParameters[0];
    assertEquals("V", V.getName());
    assertSame(Object.class, V.getBounds()[0]);
    final TypeVariable<?> W = typeParameters[1];
    assertEquals("W", W.getName());
    assertSame(List.class, ((ParameterizedType)W.getBounds()[0]).getRawType());
    assertSame(V, ((ParameterizedType)W.getBounds()[0]).getActualTypeArguments()[0]);

    // The owner type and the raw type don't actually matter here.
    // Remember, we're testing substitution in isolation, not the
    // capture conversion algorithm.
    final ParameterizedType p =
      new DefaultParameterizedType(C.class.getDeclaringClass(),
                                   C.class,
                                   Number.class,
                                   UnboundedWildcardType.INSTANCE);
    final FreshTypeVariable s2 = new FreshTypeVariable(W.getBounds()[0]);
    assertEquals(List.class, ((ParameterizedType)s2.getUpperBound()).getRawType());
    assertSame(V, ((ParameterizedType)s2.getUpperBound()).getActualTypeArguments()[0]);
    final Type result = Types.substitute(p, UnboundedWildcardType.INSTANCE, s2);
    assertSame(s2, ((ParameterizedType)result).getActualTypeArguments()[1]);
  }

  @Disabled
  @Test
  final void testCaptureConversion() {
    final ParameterizedType G = (DefaultParameterizedType)Types.normalize(C.class);
    assertNotNull(G);
    final ParameterizedType GT = (ParameterizedType)new TypeLiteral<C<Number, ?>>() {}.getType();
    final ParameterizedType GS = applyCaptureConversion(GT);
    assertNotNull(GS);
    // System.out.println(Types.toString(GS));
  }

  private static final ParameterizedType applyCaptureConversion(final ParameterizedType in) {
    final TypeVariable<?>[] A = Types.erase(in.getRawType()).getTypeParameters();
    if (A.length > 0) {
      final Type[] T = in.getActualTypeArguments();
      assert T.length == A.length : "Unexpected type arguments: " + Types.toString(T);
      final Type[] S = new Type[A.length];
      boolean sub = false; // has there been a substitution?
      for (int i = 0; i < A.length; i++) {
        if (T[i] instanceof WildcardType) {
          TypeVariable<?> Ai = A[i];
          final Type Ui = Ai.getBounds()[0];
          // Type Si = new FreshTypeVariable(Ui, null);
          final Type Si;
          final WildcardType Ti = (WildcardType)T[i];
          final Type Bi;
          final Type[] lowerBounds = Ti.getLowerBounds();
          if (lowerBounds.length <= 0) {
            final Type[] upperBounds = Ti.getUpperBounds();
            assert upperBounds.length == 1 : "Unexpected upper bounds: " + Types.toString(upperBounds);
            Bi = upperBounds[0];
            if (Bi == Object.class) {
              // Unbounded wildcard.
              // Si = substitute(Ui, A, (index, bound) -> S[index] == null ? new FreshTypeVariable(bound) : S[index]);
              Si = substitute(Ui, A, (index, bound) -> S[index] == null ? bound : S[index]);
            } else {
              // Upper bounded wildcard.
              // Si = substitute(Ui, A, (index, bound) -> S[index] == null ? Types.glb(Bi, new FreshTypeVariable(bound)) : S[index]);
              Si = substitute(Ui, A, (index, bound) -> S[index] == null ? Types.glb(Bi, bound) : S[index]);
            }
          } else {
            // Lower bounded wildcard.
            assert lowerBounds.length == 1 : "Unexpected lower bounds: " + Types.toString(lowerBounds);
            Bi = lowerBounds[0];
            // Si = 
            throw new UnsupportedOperationException("Lower bounded capture conversion is not yet supported");
          }
          S[i] = Si;
          if (!sub) {
            sub = true;
          }
        } else {
          S[i] = T[i];
        }
      }
      return sub ? new DefaultParameterizedType(in.getOwnerType(), in.getRawType(), S) : in;
    } else {
      return in;
    }
  }

  private static final <BI extends Type> FreshTypeVariable frob(final TypeVariable<?>[] A,
                                                                final Type[] S,
                                                                FreshTypeVariable Si,
                                                                final BI Bi,
                                                                final BiFunction<BI, Type, Type> f) {
    Objects.requireNonNull(A, "A");
    Objects.requireNonNull(S, "S");
    Objects.requireNonNull(Si, "Si");
    if (A.length <= 0 || A.length != S.length) {
      throw new IllegalArgumentException("A: " + Types.toString(A) + "; S: " + Types.toString(S) + "; Si: " + Types.toString(Si));
    }
    for (int j = 0; j < A.length; j++) {
      final TypeVariable<?> Aj = A[j];
      assert Aj.getBounds().length == 1 : "Unexpected type parameter: " + Types.toString(Aj);
      final Type Uj = Aj.getBounds()[0]; // upper bound of the type parameter
      Type Sj = S[j];
      if (Sj == null) {
        Sj = new FreshTypeVariable(Uj);
      }
      // Si = Types.substitute(Si, Aj, Types.glb(Bi, Sj));
      // Si = Types.substitute(Si, Aj, Sj);
      Si = (FreshTypeVariable)Types.substitute(Si, Aj, f.apply(Bi, Sj));
    }
    return Si;
  }

  private static final Type substitute(Type inBound,
                                       final TypeVariable<?>[] typeParameters,
                                       final BiFunction<? super Integer, ? super Type, ? extends Type> substitutionProvider) {
    // The (upper) bound of a type parameter we're going to do
    // substitution surgery "in".  This is, again, not the type
    // parameter itself, but its (sole, upper) bound.
    Objects.requireNonNull(inBound, "inBound");    
    if (typeParameters.length > 0) {      
      for (int i = 0; i < typeParameters.length; i++) {
        final Type fromBound = typeParameters[i].getBounds()[0];
        // Go get a corresponding existing substitution or make a new
        // one.  A substitution is an application of
        // [typeParameter[0]:=toBound[i],…,typeParameter[i]:=toBound[i]]
        // where ":=" means "substitute with".
        //
        // (For BiFunction authors) If a new toBound needs to be made:
        // For an unbounded wildcard: new FreshTypeVariable(bound);
        //
        // For an upper-bounded wildcard:
        // Types.glb(wildcardUpperBound, new FreshTypeVariable(bound));
        //
        // For a lower-bounded wildcard: new FreshTypeVariable(bound);
        // (Just like the unbounded wildcard case.)  The caller will
        // then wrap the return value of this method in a
        // FreshTypeVariable: new FreshTypeVariable(returnValue,
        // wildcardLowerBound).
        final Type toBound = substitutionProvider.apply(Integer.valueOf(i), fromBound);
        final StringBuilder sb = new StringBuilder()
          .append("i: ").append(i)
          .append("; typeParameters[").append(i).append("]: ").append(Types.toString(typeParameters[i]))
          .append("; inBound: ").append(Types.toString(inBound))
          .append("; fromBound: ").append(Types.toString(fromBound))
          .append("; toBound: ").append(Types.toString(toBound));          
        // Ui[A1:=S1,…,An:=Sn]
        inBound = Types.substitute(inBound, fromBound, toBound);
        // System.out.println(sb.append("; iteration result (inBound): ").append(Types.toString(inBound)));
      }
    }
    return inBound;
  }

  @Test
  final void testSubstitutions() {
    final ParameterizedType G = (DefaultParameterizedType)Types.normalize(C.class);
    assertNotNull(G);
    final ParameterizedType GT = (ParameterizedType)new TypeLiteral<C<Number, ?>>() {}.getType();
    final Type[] S = substitutions((TypeVariable<?>[])Types.getTypeParameters(GT), GT.getActualTypeArguments());
    // System.out.println("*** substitutions: " + Types.toString(S));
  }
  
  /**
   * @param A type parameters
   *
   * @param T type arguments
   */
  private static final Type[] substitutions(final TypeVariable<?>[] A, final Type[] T) {
    assert A.length == T.length;
    final Type[] S = new Type[A.length];
    for (int i = 0; i < A.length; i++) {
      if (Types.isWildcard(T[i])) {
        handleWildcard(i, A, T, S);
      } else {
        S[i] = T[i];
      }
    }
    return S;
  }

  private static final void handleWildcard(final int i,
                                           final TypeVariable<?>[] A,
                                           final Type[] T,
                                           final Type[] S) {
    final WildcardType Ti = (WildcardType)T[i];
    final Type uBi = Types.getSoleUpperBound(Ti);
    assert uBi != null;
    final Type uBl = Types.getSoleLowerBound(Ti);
    if (uBl == null) {
      if (uBi == Object.class) {
        // Unbounded wildcard
        handleUnboundedWildcard(i, A, S);
      } else {
        // Upper bounded wildcard
        throw new UnsupportedOperationException();
      }
    } else {      
      // Lower bounded wildcard
      throw new UnsupportedOperationException();
    }
  }

  private static final void handleUnboundedWildcard(final int i,
                                                    final TypeVariable<?>[] A,
                                                    final Type[] S) {
    assert i >= 0 && i < A.length : "i: " + i;
    assert A.length == S.length;
    final TypeVariable<?> Ai = A[i];
    final Type Ui = Ai.getBounds()[0];
    assert S[i] == null;
    S[i] = new FreshTypeVariable(Ui);
    for (int j = 0; j < A.length; j++) {
      assert j < i ? S[i] != null : true;
      if (j != i) {
        final TypeVariable<?> Aj = A[j];
        if (S[j] == null) {
          final Type Uj = Aj.getBounds()[0];
          S[j] = new FreshTypeVariable(Uj);
        }
        S[i] = Types.substitute(S[i], Aj, S[j]);
      }
    }
  }

  private static final void handleUpperBoundedWildcard(final int i,
                                                       final Type Bi,
                                                       final TypeVariable<?>[] A,
                                                       final Type[] S) {
    assert i >= 0 && i < A.length : "i: " + i;
    assert A.length == S.length;
    final Type Ui = A[i].getBounds()[0];
    assert S[i] == null;
    S[i] = Types.glb(Bi, new FreshTypeVariable(Ui));
    // for (int j = 0; j < A.length; j++) {
    for (int j = 0; j < i; j++) {
      assert j < i ? S[i] != null : true;
      if (j != i) {
        final TypeVariable<?> Aj = A[j];
        assert S[j] != null;
        /*
        if (S[j] == null) {
          final Type Uj = Aj.getBounds()[0];
          S[j] = new FreshTypeVariable(Uj);
        }
        */
        S[i] = Types.substitute(S[i], Aj, S[j]);
      }
    }
  }
  
  // https://stackoverflow.com/questions/46050311/second-order-generics-seem-to-behave-differently-than-first-order-generics/46061947#46061947
  private static final class C<V, W extends List<V>> {

    private C() {
      super();
    }

  }

  private static class A<B, C extends List<B>, D extends List<List<C>>> {

    private A() {
      super();
    }

  }

}
