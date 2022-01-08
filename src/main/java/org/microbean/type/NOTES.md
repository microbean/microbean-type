# Notes

The root issue is `TypeVariable`.  A Java `TypeVariable` is compared
for equality with another `TypeVariable` without considering their
bounds.  Instead, it's just their names and declarations (`Class`,
`Method` or `Constructor`).

A `TypeVariable` is declared by something.

A `Type`, I guess, is declared by something.

_muse muse muse_

