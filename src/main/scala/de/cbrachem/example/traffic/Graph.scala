package de.cbrachem.example.traffic

import scala.annotation.tailrec
import scala.collection.{MapView, mutable}
import io.circe._, io.circe.generic.semiauto._

case class Graph[T](adj: Map[T, List[Edge[ T]]], vertices: Set[T])

object Graph {
  def build[T](edges: List[Edge[T]]) = {
    val vertices = edges.flatMap(e => List(e.from, e.to)).toSet
    Graph(edges.groupBy(_.from), vertices)
  }

  /**
   * Adapted from https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm#Using_a_priority_queue
   * and https://medium.com/se-notes-by-alexey-novakov/algorithms-in-scala-dijkstra-shortest-path-78c4291dd8ab
   *
   * This exercise is supposed to be solved in a functional programming style, so it might be surprising to find mutable maps, a queue and a while loop
   * which mutates the local state here. In my opinion, the main goal of functional programming is to make reading and reasoning about the code easier.
   * Immutable data makes it easier to track changes, especially across larger parts of a programm or even in asynchronous or multi-threaded environments.
   * In this case I think having Dijkstra's algorithm in a well-knwon form with some local mutable state (which is kept local and contained to this function)
   * is preferrable to adopting the algorithm to a more functional form, possibly losing clarity in the process, simply because the result would look unfamiliar.
   */
  def findShortestPath[T](g: Graph[T], start: T, end: T): Either[AppError, ShortestPathSolution[T]] = {
    if (!g.vertices.contains(start) || !g.vertices.contains(end)) {
      return Left(InvalidStartOrEndIntersectionError())
    }

    val dist = mutable.Map.from(g.vertices.map((_, Double.PositiveInfinity)))
    val prev: mutable.Map[T, Option[T]] = mutable.Map.from(g.vertices.map((_, None)))

    dist(start) = 0.0
    val startDist = (start, dist(start))
    val sortByWeight = Ordering.by[(T, Double),Double](_._2).reverse
    val Q = mutable.PriorityQueue(startDist)(sortByWeight)

    while (Q.nonEmpty) {
      val (u, _) = Q.dequeue()
      if (g.adj.contains(u)) {
        for (v <- g.adj(u)) {
          val alt = dist(u) + v.weight
          if (alt < dist(v.to)) {
            dist(v.to) = alt
            prev(v.to) = Some(u)
            if (! Q.exists(_._1 == v.to)) {
              Q.enqueue((v.to, alt))
            }
          }
        }
      }
    }

    // if no solution exists, fail
    if (!ShortestPathSolution.exists(start, end, prev.view)) {
      Left(NoValidRouteError(start, end))
    } else {
      Right(ShortestPathSolution.build(start, end, prev.view, dist(end)))
    }
  }
}

case class ShortestPathSolution[T](start: T, end: T, path: List[T], totalTime: Double)

object ShortestPathSolution {
  def build[T](start: T, end: T, prev: MapView[T, Option[T]], distance: Double) = {
    val path = pathTo(end, prev) :+ end
    ShortestPathSolution(start, end, path, distance)
  }

  def exists[T](start: T, end: T, prev: MapView[T, Option[T]]) = {
    val path = pathTo(end, prev)
    !path.isEmpty // path exists iff path is not empt,
  }

  private def pathTo[T](end: T, prev: MapView[T, Option[T]]) = {
    @tailrec
    def go(list: List[T], to: T): List[T] = {
      prev(to) match {
        case Some(e) => go(e +: list, e)
        case None => list
      }
    }

    go(List(), end)
  }
}

