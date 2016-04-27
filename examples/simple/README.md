Oracle Coherence Reactive Extensions (RX) Simple Example
========================================================

Overview
--------

This example shows how to use Oracle Coherence Reactive Extensions
to return "cold" Observables from standard NamedCache operations.

To turn a NamedCache into a RxNamedCache, do the following:
```
   NamedCache<String, String> myMache = ...;
   
   RxNamedCache<String, String> rxCache = RxNamedCache.rx(myCache);
```
   
Once you have an RxNamedCache, you can perform operations on the cache and
an Observable will be returned from which you can perform standard RxJava
operations such as:
```
   // display all the trades
   rxCache.entrySet()
           .map(entry -> entry.getValue())
           .subscribe(System.out::println);
```

Prerequisites
-------------

Ensure that the prerequisites for the building of Coherence-Rx has been met.
  
Build Instructions
------------------

Build the Coherence Reactive Extensions example by using:
   "mvn clean install"
     
Running the Example
-------------------
    
The example can be run via either:
    
1. IDE
    
   Import the "simple" example project into your IDE. Build and run com.oracle.coherence.rx.examples.simple.App.java
       
2. Command line   
    
   After building the example, run the example using:
```
   mvn exec:exec -DsimpleExample      
```