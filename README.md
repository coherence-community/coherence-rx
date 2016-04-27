# Oracle Coherence Reactive Extensions (Rx)

CoherenceRx project provides [reactive](http://reactivex.io/) API for [Oracle
Coherence](https://www.oracle.com/middleware/coherence/index.html) in-memory data grid,
based on a popular [RxJava](https://github.com/ReactiveX/RxJava) library.

It is implemented as a thin wrapper around [Oracle Coherence Asynchronous API](https://www.youtube.com/watch?v=xuUqV9ayBoU&index=4&list=PLxqhEJ4CA3JtazSZ0CI9JyriEjyHkb-9R),
which implies that it requires Coherence 12.2.1 or a newer release.  

## Why CoherenceRx?

Reactive Programming is somewhat of an all-or-nothing proposition, or as [Andre Staltz](http://andre.staltz.com/)
pointed out in [his excellent tutorial](https://gist.github.com/staltz/868e7e9bc2a7b8c1f754):

![Everything is a Stream](https://gist.githubusercontent.com/staltz/868e7e9bc2a7b8c1f754/raw/35cc1edb69b7175fd1308800a244410890bc9b5f/zmantra.jpg)

When you are writing reactive application and need to access data source that doesn't provide
a reactive API, life can get complicated, so in order to simplify our users' lives we decided
to implement CoherenceRx and release it as an open source add-on for Coherence.

## Using CoherenceRx

The easiest way to include CoherenceRx into your own project is to add it as a Maven dependency
(along with Coherence itself and RxJava):

```xml
  <dependency>
    <groupId>com.oracle.coherence</groupId>
    <artifactId>coherence</artifactId>
    <version>${coherence.version}</version>
  </dependency>

  <dependency>
    <groupId>com.oracle.coherence</groupId>
    <artifactId>coherence-rx</artifactId>
    <version>${coherence-rx.version}</version>
  </dependency>

  <dependency>
    <groupId>io.reactivex</groupId>
    <artifactId>rxjava</artifactId>
    <version>${rxjava.version}</version>
  </dependency>
```

and configure versions within Maven `properties` section:

```xml
  <coherence.version>12.2.1-0-0</coherence.version>
  <coherence-rx.version>1.0.0</coherence-rx.version>
  <rxjava.version>1.1.0</rxjava.version>
```

Once you have the necessary dependencies properly configured, you can use the static
`RxNamedCache.rx` method to create an instance of `RxNamedCache`:

```java
NamedCache<Long, Product>   cache   = CacheFactory.getTypedCache("trades", withTypes(Long.class, Product.class));
RxNamedCache<Long, Product> rxCache = RxNamedCache.rx(cache);
```

Of course, you can also use static import for the `RxNamedCache.rx` method, which
would make the code even simpler.

The `RxNamedCache` interface will be familiar to anyone who has used Coherence
`NamedCache` API before, with one major difference: all the methods return an
[`Observable`](http://reactivex.io/documentation/observable.html).

For example, `RxNamedCache.get` will return an `Observable<V>` which will eventually
emit the value of the cache entry for the given key and complete:

```java
rxCache.get(5L).subscribe(product -> System.out.println("Got: " + product));
```

Another important difference is that the bulk read operations, such as `getAll`,
`keySet`, `entrySet` and `values` do not return a single container value like
their `NamedCache` counterparts, but an `Observable` stream of individual values:

```java
rxCache.values().subscribe(product -> System.out.println("Got: " + product));
```

This is both more efficient, as it doesn't realize full result set on the client,
and simpler, as it allows you to process each individual value as it is emitted
by the underlying `Observable`.

For example, if you wanted to process batches of 10 products at a time, you could
trivially accomplish that using [`buffer`](http://reactivex.io/documentation/operators/buffer.html)
operation:

```java
rxCache.values()
       .buffer(10)
       .subscribe(productList -> System.out.println("Got: " + productList));
```

### Observing Event Streams

Oracle Coherence provides rich event notification functionality, so it only made
sense to provide an adapter that allows you to use [RxJava](https://github.com/ReactiveX/RxJava)
to process stream of event notifications.

CoherenceRx introduces `ObservableMapListener`, which extends RxJava `Observable`
and implements Coherence `MapListener` interface. The `ObservableMapListener` simply
propagates each received event to all of its subscribers:

```java
ObservableMapListener<Long, Product> listener = ObservableMapListener.create();
listener.subscribe(System.out::println);

cache.addMapListener(listener);
```

Of course, the above is not very interesting, and could be easily achieved using
standard `SimpleMapListener` as well. But it becomes a lot more interesting
when you start applying various RxJava [operators](http://reactivex.io/documentation/operators.html)
to transform, filter and even combine event streams.

```java
ObservableMapListener<Long, Trade> listener = ObservableMapListener.create();
listener.filter(evt -> evt.getId() == MapEvent.ENTRY_INSERTED)
        .map(MapEvent::getNewValue)
        .buffer(10, TimeUnit.SECONDS)
        .subscribe(trades -> System.out.println("Trades placed in the last 10 seconds: " + trades));

cache.addMapListener(listener);
```

It is important to note that unlike `Observable`s returned by the `RxNamedCache`
methods, which are 'cold', the `ObservableMapListener` is a 'hot' `Observable`
and will start receiving and processing the events as soon as it is registered
with the cache using `NamedCache.addMapListener` method.

Because of that, it is important that you add `Subscriber`s to it *before* calling
`NamedCache.addMapListener`, or you could miss some events.

## Building CoherenceRx

The following sections describe the steps necessary to build CoherenceRx from the source.

### Prerequisites

  In order to build or use the Coherence Reactive Extensions you must have the following installed:

  1. Java 8 SE Development Kit or Runtime environment.

     You can download the software from:
     - Java SE Development Kit - http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
     - JAVA SE Runtime Environment - http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

  2. Maven version 3.0.5 or above installed and configured.
  3. Coherence 12.2.1.0.0 or above installed.

  Ensure the following environment variables are set:

  JAVA_HOME
    Make sure that the JAVA_HOME environment variable points to the location of a JDK supported by the
    Oracle Coherence version you are using.

  COHERENCE_HOME
    Make sure COHERENCE_HOME is set to point to your Coherence install directory.
    This is only required for the Maven install-file commands.

  MAVEN_HOME
    If mvn command is not in your path then you should set MAVEN_HOME and then add MAVEN_HOME\bin to your PATH
    in a similar way to Java being added to the path below.

  You must also ensure the java command is in the path.
    E.g. for Linux/UNIX:
      export PATH=$JAVA_HOME/bin:$PATH

    For Windows:
      set PATH=%JAVA_HOME%\bin;%PATH%

  You must have Coherence installed into your local maven repository. If you
  do not, then carry out the following, replacing the version number with the version
  of Coherence you have installed.

  E.g. for Linux/UNIX/Mac:

    mvn install:install-file -Dfile=$COHERENCE_HOME/lib/coherence.jar      -DpomFile=$COHERENCE_HOME/plugins/maven/com/oracle/coherence/coherence/12.2.1/coherence.12.2.1.pom

  E.g. for Windows:

    mvn install:install-file -Dfile=%COHERENCE_HOME%\lib\coherence.jar      -DpomFile=%COHERENCE_HOME%\plugins\maven\com\oracle\coherence\coherence\12.2.1\coherence.12.2.1.pom

### Build Instructions

  Build the Coherence Reactive Extensions by using:

     mvn clean install

  The target directory will contain a number of files:

     - coherence-rx-x.y.z.jar          - JAR file
     - coherence-rx-x.y.z-javadoc.jar  - javadoc
     - coherence-rx-x.y.z-sources.jar  - sources

    (where x.y.x are the current version of the Coherence Reactive Extensions)

## References

   For more information on Oracle Coherence, please see the following links:
   - [Download Coherence](http://www.oracle.com/technetwork/middleware/coherence/downloads/index.html)
   - [Coherence Documentation](http://docs.oracle.com/middleware/1221/coherence/index.html)
   - [Coherence Community](http://coherence.java.net/)
