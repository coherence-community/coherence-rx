# CoherenceRx Examples

This document describes the examples available for Oracle Coherence Reactive Extensions.

## Prerequisites

Ensure that the prerequisites for the building of CoherenceRx have been met, as
described in the [CoherenceRx Documentation](../README.md).

## Examples

There are two examples available to show usage of CoherenceRx:

- [Simple Example](simple/README.md)

  Demonstrates how to use `RxNamedCache` interface to create 'cold' `Observables`
  and perform simple operations such as querying and aggregating data stored in Coherence.

- [Temperature Monitor](temperature-monitor/README.md)

  Demonstrates how to use `ObservableMapListener` class to create 'hot' `Observables`
  in order to subscribe to and process asynchronous events.
