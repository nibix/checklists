[![codecov](https://codecov.io/gh/nibix/checklists/graph/badge.svg?token=957EGPZ5OE)](https://codecov.io/gh/nibix/checklists)

# checklists
Dedicated check list and check table data structures for Java.

**This is now part of [com.selectivem.collections](https://github.com/nibix/collections). Please see there.

Check lists and check tables take a set of items as input; initially, all items will be marked as unchecked.
You can then use the various methods to mark individual items as checked. Further methods allow you to check
whether your check list or check table is complete - and when not, what is missing.

A check list is a one-dimensional list, a check table is a two-dimensional matrix.

One typical use case for these data structures are complex access control rules. You can use
a check table to tick off what privileges are present and which are not present. The tabular
`toString()` representation helps quickly identifying what privileges are missing:

```
          | indices:data/read/search |
 index_a11| ok                       |
 index_a12| MISSING                  |
 index_a13| MISSING                  |
 index_a14| MISSING                  |
```

## Maven Dependency

Add this library using this Maven dependency:

```
<dependency>
    <groupId>com.selectivem</groupId>
    <artifactId>checklists</artifactId>
    <version>1.0.0</version>
</dependency>
```

## License

This code is licensed under the Apache 2.0 License.

## Copyright

Copyright 2024 Nils Bandener <code@selectiveminimalism.com>

Based on fluent collections which is Copyright 2022 by floragunn GmbH: https://git.floragunn.com/util/fluent-collections
