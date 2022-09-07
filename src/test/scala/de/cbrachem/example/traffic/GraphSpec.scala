package de.cbrachem.example.traffic

import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class GraphSpec extends AnyFlatSpec with should.Matchers {
  def toyProblem = Graph.build(List(
    Measurement("A", "1", "A", "2", 1),
    Measurement("A", "2", "B", "2", 1),
    Measurement("B", "1", "B", "2", 4),
    Measurement("B", "1", "A", "1", 1)
  ))

  "The shortest path algorithm" should "find the shortest path on a toy problem" in {
    val start = Intersection.fromString("B1").get
    val end = Intersection.fromString("B2").get
    val solution = Graph.findShortestPath(toyProblem, start, end)

    solution should matchPattern { case Right(_) => }
    inside(solution) { case Right(s) =>
      s.start should equal (start)
      s.end should equal (end)
      s.totalTime should equal (3.0)
      s.path should equal (List(start, Intersection.fromString("A1").get, Intersection.fromString("A2").get, end))
    }
  }

  it should "fail if starting or ending intersections are not part of the problem" in {
    val start = Intersection.fromString("B1").get
    val end = Intersection.fromString("C1").get
    val solution = Graph.findShortestPath(toyProblem, start, end)

    solution should equal (Left(InvalidStartOrEndIntersectionError()))
  }

  it should "fail if there is no valid path from start to end" in {
    val start = Intersection.fromString("A1").get
    val end = Intersection.fromString("B1").get
    val solution = Graph.findShortestPath(toyProblem, start, end)

    solution should equal (Left(NoValidRouteError(start, end)))
  }
}
