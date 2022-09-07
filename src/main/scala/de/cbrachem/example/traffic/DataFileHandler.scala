package de.cbrachem.example.traffic

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.util.Using
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

object DataFileHandler {
  /**
   * Loads traffic measurement data from given file, parses the JSON and validates dataset.
   */
  def loadAndValidateData(path: Path) = {
    for {
      dataStr <- loadDataFromFile(path)
      data <- decode[TrafficData](dataStr).left.map(DataFileParseError)
      validatedData <- validateData(data)
    } yield validatedData
  }

  def loadDataFromFile(path: Path) = {
    Using(Source.fromFile(path.toFile))(source => source.getLines().mkString)
      .toEither
      .left.map(DataFileReadError)
  }

  /**
   * Validates some reasonable constraints on the dataset.
   */
  def validateData(data: TrafficData) = {
    // ensure that dataset is not empty,
    // that all measurements are the same and that all neighbors are connected
    val notEmpty = !data.trafficMeasurements.flatMap(_.measurements).isEmpty
    if (notEmpty && allSetsContainMeasurementsForSamePairs(data) &&
      gridIsCompletelyMeasured(data) && thereAreNoDuplicateMeasurements(data)) {
      Right(data)
    } else {
      Left(DataFileValidationError())
    }
  }

  /**
   * Tests if each set of measurements contains measurements between the same pairs of intersections.
   */
  def allSetsContainMeasurementsForSamePairs(data: TrafficData) = {
    val ms = data.trafficMeasurements
    val firstPairs = ms(0).pairs

    ms.forall(_.pairs == firstPairs)
  }

  /**
   * Tests if all valid paths between neighboring intersections are measured
   * in each set of measurements.
   */
  def gridIsCompletelyMeasured(data: TrafficData) = {
    val first = data.trafficMeasurements(0)
    val allCombinations = generateAllValidPairsOfDirectNeighbors(data)
    allCombinations subsetOf first.pairs.toSet[(Intersection, Intersection)]
  }

  /**
   * Tests if there are multiple measurements of the same intersection pair in the first
   * set of measurements.
   */
  def thereAreNoDuplicateMeasurements(data: TrafficData) = {
    val first = data.trafficMeasurements(0)

    val duplicates = first.measurements
      .groupBy(_.intersectionPair)
      .collect { case (x,ys) if ys.lengthCompare(1) > 0 => x }

    duplicates.isEmpty
  }

  def getAllStreets(tm: TrafficMeasurement) = {
    tm.measurements.flatMap(m => List(m.startStreet, m.endStreet)).distinct.sortBy(_.toInt)
  }

  def getAllAvenues(tm: TrafficMeasurement) = {
    tm.measurements.flatMap(m => List(m.startAvenue, m.endAvenue)).distinct.sorted
  }

  /**
   * Generates a list of all Intersections pairs between direct neighbors on the grid
   * that can have a traffic measurement (i.e. only pairs along the driving direction)
   */
  def generateAllValidPairsOfDirectNeighbors(data: TrafficData) = {
    val first = data.trafficMeasurements(0)
    val allStreets = getAllStreets(first)
    val allAvenues = getAllAvenues(first)



    // We first need to get the correct direction for all streets and avenues.
    // There might be streets or avenues for which there is no data do determine which direction they go.
    // Such a dataset must be invalid, so we can just pretend they go west/north.
    val regularEdges = first.measurements.filter(m => (m.startAvenue == m.endAvenue || m.startStreet == m.endStreet) &&
      (m.startStreet.toInt - m.endStreet.toInt).abs <= 1 && (m.startAvenue.charAt(0) - m.endAvenue.charAt(0)).abs <= 1)
    val streetsGoingEast = regularEdges.filter(m => (m.startAvenue.charAt(0) - m.endAvenue.charAt(0)) == -1).map(_.startStreet).toSet
    val streetsGoingWest = allStreets.toSet -- streetsGoingEast
    val avesGoingSouth = regularEdges.filter(m => (m.startStreet.toInt - m.endStreet.toInt) == -1).map(_.startAvenue).toSet
    val avesGoingNorth = allAvenues.toSet -- avesGoingSouth

    // Then, we can generate all valid intersection pairs between direct neighbors
    val c1 = for {
      s <- streetsGoingEast
      (a1, a2) <- allAvenues.init zip allAvenues.tail
    } yield (Intersection(s, a1), Intersection(s, a2))

    val c2 = for {
      s <- streetsGoingWest
      (a1, a2) <- allAvenues.tail zip allAvenues.init
    } yield (Intersection(s, a1), Intersection(s, a2))

    val c3 = for {
      (s1, s2) <- allStreets.init zip allStreets.tail
      a <- avesGoingSouth
    } yield (Intersection(s1, a), Intersection(s2, a))

    val c4 = for {
      (s1, s2) <- allStreets.tail zip allStreets.init
      a <- avesGoingNorth
    } yield (Intersection(s1, a), Intersection(s2, a))

    c1 ++ c2 ++ c3 ++ c4
  }
}
