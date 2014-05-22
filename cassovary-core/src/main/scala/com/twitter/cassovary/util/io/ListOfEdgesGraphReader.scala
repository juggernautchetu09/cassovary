/*
 * Copyright 2014 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.twitter.cassovary.util.io

import com.twitter.cassovary.util.NodeNumberer
import com.twitter.cassovary.graph.NodeIdEdgesMaxId
import com.twitter.logging.Logger
import it.unimi.dsi.fastutil.ints.{Int2IntArrayMap, Int2ObjectMap, Int2ObjectLinkedOpenHashMap}
import scala.io.Source
import scala.collection.mutable.ArrayBuffer
import java.util.concurrent.ExecutorService

/**
 * Reads in a multi-line list of edges from multiple files in a directory, which nodes have ids of type T.
 * Does not check for duplicate edges or nodes.
 *
 * You can optionally specify which files in a directory to read. For example, you may have files starting with
 * "part-" that you'd like to read. Only these will be read in if you specify that as the file prefix.
 *
 * You should also specify `nodeNumberer`, `idReader`, `separator` between nodes in an edge
 * and `quotationMark` if used with node ids.
 *
 * For a default version for `Int` graphs see [[ListOfEdgesGraphReader.forIntIds]] builder method.
 *
 * In each file, a directed edges is defined by a pair of T: from and to.
 * For example, we use `String` ids with `"` `quotationMark` and ` ` (space) `separator`, when
 * reading file:
 * {{{
 * "a" "b"
 * "b" "d"
 * "d" "c"
 * "a" "e"
 * ...
 * }}}
 * In this file, node `a` has two outgoing edges (to `b` and `e`), node `b` has an outgoing edge
 * to node `d` and node `d` has an outgoing edge to node `c`.
 *
 * In the simplest case of Int graph from file:
 * {{{
 * 1 3
 * 2 4
 * 4 3
 * 1 5
 * ...
 * }}}
 * We use empty `quotationMark` and `separator` and default `nodeRenumberer`.
 *
 * Note that, it is recommended to use AdjacencyListGraphReader, because of its efficiency.
 *
 * @param directory the directory to read from
 * @param prefixFileNames the string that each part file starts with
 * @param nodeNumberer nodeNumberer to use with node ids
 * @param idReader function that can read id from String
 * @param separator sign between nodes forming edge
 * @param quotationMark quotation mark used with ids
 */
class ListOfEdgesGraphReader[T](val directory: String, override val prefixFileNames: String,
                                val nodeNumberer: NodeNumberer[T], idReader: (String => T),
                                separator: String = " ", quotationMark: String = "")
  extends GraphReaderFromDirectory[T] {

  private lazy val log = Logger.get

  private class OneShardReader(filename: String, nodeNumberer: NodeNumberer[T])
    extends Iterator[NodeIdEdgesMaxId] {

    private val holder = NodeIdEdgesMaxId(-1, null, -1)

    def readEdgesBySource(): (Int2ObjectMap[ArrayBuffer[Int]], Int2IntArrayMap) = {
      log.info("Starting reading from file %s...\n", filename)
      val quotationMarkRegex = if (quotationMark.isEmpty) "" else "\\" + quotationMark
      val labelRegex = quotationMarkRegex + """(\w+)""" + quotationMarkRegex
      val directedEdgePattern = (labelRegex + separator + labelRegex).r
      val commentPattern = """(^#.*)""".r
      val lines = Source.fromFile(filename).getLines()

      val edgesBySource = new Int2ObjectLinkedOpenHashMap[ArrayBuffer[Int]]()
      val nodeMaxOutEdgeId = new Int2IntArrayMap()

      def updateNodeMaxOutEdgeId(node: Int, out: Int) {
        if (nodeMaxOutEdgeId.containsKey(node)) {
          nodeMaxOutEdgeId.put(node, nodeMaxOutEdgeId.get(node) max out)
        } else {
          nodeMaxOutEdgeId.put(node, node max out)
        }
      }

      lines.foreach {
        line =>
          line.trim match {
            case commentPattern(s) => ()
            case directedEdgePattern(from, to) =>
              val internalFromId = nodeNumberer.externalToInternal(idReader(from))
              val internalToId = nodeNumberer.externalToInternal(idReader(to))
              if (edgesBySource.containsKey(internalFromId)) {
                edgesBySource.get(internalFromId) += internalToId
              } else {
                edgesBySource.put(internalFromId, ArrayBuffer(internalToId))
              }
              updateNodeMaxOutEdgeId(internalFromId, internalToId)
          }
      }
      log.info("Finished reading from file %s...\n", filename)
      (edgesBySource, nodeMaxOutEdgeId)
    }

    val (edgesBySource, nodeMaxOutEdgeId) = readEdgesBySource()

    lazy val edgesIterator = edgesBySource.entrySet().iterator()

    override def hasNext: Boolean = edgesIterator.hasNext

    override def next(): NodeIdEdgesMaxId = {
      val elem = edgesIterator.next()
      holder.id = elem.getKey
      holder.edges = elem.getValue.toArray
      holder.maxId = nodeMaxOutEdgeId.get(elem.getKey)
      holder
    }
  }

  def oneShardReader(filename: String): Iterator[NodeIdEdgesMaxId] = {
    new OneShardReader(filename, nodeNumberer)
  }
}

object ListOfEdgesGraphReader {
  def forIntIds(directory: String, prefixFileNames: String = "", threadPool: ExecutorService,
                nodeNumberer: NodeNumberer[Int] = new NodeNumberer.IntIdentity()) =
    new ListOfEdgesGraphReader[Int](directory, prefixFileNames, new NodeNumberer.IntIdentity(), _.toInt) {
      override val executorService = threadPool
    }
}