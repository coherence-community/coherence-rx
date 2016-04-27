Oracle Coherence Reactive Extensions (RX) Temperature Monitor Example
=====================================================================

Overview
--------

This example shows how to use Oracle Coherence Reactive Extensions
to return "hot" Observables from standard NamedCache operations.

Use the following to create "hot" Observables.

```java
   // create the listener
   ObservableMapListener listener = ObservableMapListener.create();
   
   // add a subscriber to print out only readings that have a temp > 50
   listener.map(entry -> entry.getNewEntry().getValue())
           .filter(reading -> reading.getTemperature() > 50)
           .subscribe(System.out::println); 
   
   // Add the listener to the cache
   myCache.addMapListener(listener);
   
```  

Prerequisites
-------------

Ensure that the prerequisites for the building of Coherence-Rx has been met.
  
Build Instructions
------------------

Build the Coherence Reactive Extensions example by using:
```
   mvn clean install
```
     
Running the Example
-------------------
    
The example can be run via either:
    
1. IDE
 
   Import the "temperature-monitor" example project into your IDE and run **com.oracle.coherence.rx.examples.temp.App.java**.
       
2. Command line   
    
   After building the example, run the example using:
```
   mvn exec:exec -DtemperatureMonitor     
```

This demonstration uses Oracle Tools to startup 3 cache servers that will be used to store data.
 See [coherence.java.net](coherence.java.net) for more information on Oracle Tools.

Once the example is running, you will see two GUIs.
- **Data Generator** - shows 3 Devices and once you click the "Start Emitting" button it will 
insert data into the cache.  You can change the temperature manually or generate random temperature
by checking the check-box.
- **Data Monitor** - uses various subscribers to the cache data to display 15, 30 and 60 second 
averages, current temperatures and trends. 
        