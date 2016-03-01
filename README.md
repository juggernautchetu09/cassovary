# Cassovary [![Build Status](https://secure.travis-ci.org/twitter/cassovary.png)](http://travis-ci.org/twitter/cassovary)
Cassovary is a simple "big graph" processing library for the JVM.
Most JVM-hosted graph libraries are flexible but not
space efficient. Cassovary is designed from the ground up to first be
able to efficiently handle graphs with billions of nodes
and edges. A typical example usage is to do large scale
graph mining and analysis of a <a href="https://twitter.com">big network</a>.
Cassovary is written in Scala and can be used with any JVM-hosted language.
It comes with some common data structures and algorithms.

Please follow the cassovary project on twitter at [@cassovary](https://twitter.com/cassovary)
for updates.

## Quick Start and Examples
There is a subproject included here called cassovary-examples containing simple java and scala
examples of using the library. See
[this README](https://github.com/twitter/cassovary/blob/master/cassovary-examples/ExamplesREADME.md)
to get started with the examples.

Some other subprojects to check are cassovary-benchmarks for helping benchmark
some graph algorithms and cassovary-server that exposes Cassovary on a web server.

## Building

Cassovary is built using [sbt](https://github.com/sbt/sbt) and was tested last using sbt version 0.13.

1. ```./sbt update``` (might take a couple of minutes)
2. ```./sbt test```
3. ```./sbt package```

## Alternative for using for local projects
1. ```./sbt publish-local```
2. ```cd ../<dependent project>```
3. ```./sbt update```

## Using maven published version of library

Cassovary is published to maven central with crosspath scala versions 2.10.4 and 2.11.5.

To use with sbt, use:

```libraryDependencies += "com.twitter" %% "cassovary-core" % "5.0.0"```

and

```resolvers += "twitter" at "http://maven.twttr.com"```

The last Cassovary version to support scala 2.9 is 3.4.0, and
support for scala version 2.9.x has been discontinued since.

The only dependency that Cassovary uses which is not bundled with it (because of its size)
is ```it.unimi.dsi.fastutil```. You can add that dependency in your sbt project as follows:

```libraryDependencies += "it.unimi.dsi" % "fastutil" % "6.6.0"```

## Comparison to Other Graph Libraries
There are many excellent graph mining libraries already in existence. Most of
them have one or more of the following characteristics:

1. Written in C/C++. Examples include [SNAP](http://snap.stanford.edu/) from Stanford and
[GraphLab](http://graphlab.org/) from CMU. The typical way to use these from JVM is to use
JNI bridges.
2. Sacrifice storage efficiency for flexibility. Examples include
[JUNG](http://jung.sourceforge.net/) which is written in Java but
stores nodes and edges as big objects.
3. Are meant to do much more, typically a full graph database. Examples include
[Neo4J](http://neo4j.org).

On the other hand, Cassovary is intended to be easy to use in a JVM-hosted
environment and yet be efficient enough to scale to billions of edges.
It is deliberately not designed to provide any persistence or database functionality.
Also, it currently skips any concerns of partitioning the graph and hence is
not directly comparable to distributed graph processing systems like
[Apache Giraph](http://incubator.apache.org/giraph/). This allows complex algorithms
to be run on the graph efficiently, an otherwise recurring issue with distributed
graph processing systems because of the known difficulty of achieving good
graph partitions. On the flip side, the size of the
graph it works with is bounded by the memory available in a machine, though
the use of space efficient data structures does not seem to make this a
limitation for most practical graphs. For example, an ```ArrayBasedDirectedGraph```
instance of a unidirectional graph with 10M nodes and 1B edges consumes
less than 6GB of memory, and scales linearly beyond that.

## Mailing list
http://groups.google.com/group/twitter-cassovary

Please follow the cassovary project on twitter at [@cassovary](https://twitter.com/cassovary)
for updates.

## Bugs
Please report any bugs to: <https://github.com/twitter/cassovary/issues>

## Acknowledgments
Thanks to all the [contributors](https://github.com/twitter/cassovary/graphs/contributors) of Cassovary.

We use the [Yourkit](http://yourkit.com) Java Profiler for profiling and tuning Cassovary. [![Yourkit logo](http://projects.collide.info/attachments/download/1289/yklogo.png)](http://yourkit.com)

## License
Copyright 2015 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0



[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/juggernautchetu09/cassovary/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

