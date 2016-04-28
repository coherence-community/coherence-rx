# CoherenceRx: Simple Example

This example shows how to use `RxNamedCache` interface to create 'cold' `Observables`
and perform simple operations such as querying and aggregating data stored in Coherence.

To turn a `NamedCache` into a `RxNamedCache`, do the following:

```java
NamedCache<String, String>   myCache = ...;
RxNamedCache<String, String> rxCache = RxNamedCache.rx(myCache);
```

Once you have an `RxNamedCache`, you can perform operations on the cache and
an `Observable` will be returned from which you can perform standard RxJava
operations such as:

```java
// display all the values
rxCache.entrySet()
       .map(Map.Entry::getValue)
       .subscribe(System.out::println);
```

## Prerequisites

Ensure that the prerequisites for the building of CoherenceRx have been met, as
described in the [CoherenceRx Documentation](../../README.md).

## Build Instructions

Build the Simple Example by running:

```
mvn clean install
```

## Running the Example

The example can be run via either:

1. IDE

   Import the `simple` example project into your IDE. Build and run `com.oracle.coherence.rx.examples.simple.App` class.

2. Command line   

   After building the example, run it using the following command:

   ```
   mvn exec:exec -DrunExample      
   ```
