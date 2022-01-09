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
…then `p`'s type arguments will be `String`, `String`.

TODO: verify
