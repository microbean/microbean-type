# Notes

Type resolution again.

If you have:
```java
final Object m = new HashMap<String, String>();
```
…and you do:
```
final ParameterizedType p = (ParameterizedType)m.getClass().getGenericSuperclass();
```
…then `p`'s type arguments will be `[ String, String ]`.  (Verified.)

That's fine, but although `p`'s _arguments_ will be `[ String, String ]`,
when it comes time to find the supertype of `p`, its _arguments_ are not taken
into account, only its raw type's _parameters_, which are type variables.
