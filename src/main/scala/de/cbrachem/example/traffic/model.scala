package de.cbrachem.example.traffic

import io.circe.Error

trait Edge[T] {
  def from: T

  def to: T

  def weight: Double
}

case class Intersection(street: String, avenue: String) {
  override def toString: String = avenue + street
}

object Intersection {
  def fromString(s: String): Option[Intersection] = {
    val pattern = "([A-Z])([0-9]+)".r
    s match {
      case pattern(avenue, street) => Some(Intersection(street, avenue))
      case _ => None
    }
  }
}

case class Measurement(startAvenue: String, startStreet: String, endAvenue: String, endStreet: String, transitTime: Double)
  extends Edge[Intersection] {
  def start = Intersection(startStreet, startAvenue)

  def end = Intersection(endStreet, endAvenue)

  def intersectionPair = (start, end)

  override def from = start

  override def to = end

  override def weight = transitTime
}

case class TrafficMeasurement(measurementTime: Int, measurements: List[Measurement]) {
  def pairs = measurements.map(_.intersectionPair)
}

case class TrafficData(trafficMeasurements: List[TrafficMeasurement]) {
  def average = {
    trafficMeasurements
      .map(_.measurements)
      .transpose // We can only do this because each measurement set contains the same pairs in the same order
      .map(ms => ms(0).copy(transitTime = ms.map(_.transitTime).sum / ms.length))
  }
}


sealed trait AppError {
  def display: String
}

case class DataFileReadError(t: Throwable) extends AppError {
  override def display: String = s"Error reading data file: ${t.getMessage}"
}

case class DataFileParseError(e: Error) extends AppError {
  override def display: String = s"Error parsing data file: ${e.getMessage}"
}

case class DataFileValidationError() extends AppError {
  override def display: String = "Data file did not pass validation checks"
}

case class InvalidStartOrEndIntersectionError() extends AppError {
  override def display: String = "Given starting or ending intersections are not part of the grid"
}

case class NoValidRouteError[T](start: T, end: T) extends AppError {
  override def display: String = s"No valid route found from $start to $end"
}