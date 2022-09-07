package de.cbrachem.example.traffic

import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers._

import scala.collection.mutable.Stack

class DataValidationSpec extends AnyFlatSpec with should.Matchers {

  "The data validation logic" should "reject an empty dataset" in {
    val empty1 = TrafficData(List())
    val empty2 = TrafficData(List(TrafficMeasurement(1, List())))

    val result1 = DataFileHandler.validateData(empty1)
    result1 should equal (Left(DataFileValidationError()))

    val result2 = DataFileHandler.validateData(empty2)
    result2 should equal (Left(DataFileValidationError()))
  }

  it should "reject datasets where sets of measurements contain different measurements" in {
    val example1 = TrafficData(List(
      TrafficMeasurement(1, List(Measurement("A", "1", "A", "2", 1))),
      TrafficMeasurement(1, List(Measurement("A", "1", "A", "3", 1)))
    ))

    val example2 = TrafficData(List(
      TrafficMeasurement(1, List(Measurement("A", "1", "A", "2", 1))),
      TrafficMeasurement(1, List(Measurement("A", "1", "A", "2", 1), Measurement("B", "1", "B", "2", 1)))
    ))

    val result1 = DataFileHandler.validateData(example1)
    result1 should equal (Left(DataFileValidationError()))

    val result2 = DataFileHandler.validateData(example2)
    result2 should equal (Left(DataFileValidationError()))
  }

  it should "reject datasets with incomplete measurements" in {
    // Data with 2 streets, 2 avenues should contain 4 measurements
    val example = TrafficData(List(TrafficMeasurement(1, List(
      Measurement("A", "1", "A", "2", 1),
      Measurement("A", "2", "B", "2", 1),
      Measurement("B", "2", "B", "1", 1)
    ))))

    val result = DataFileHandler.validateData(example)
    result should equal (Left(DataFileValidationError()))
  }

  it should "reject datasets with multiple measurements of the same section" in {
    val example = TrafficData(List(TrafficMeasurement(1, List(
      Measurement("A", "1", "A", "2", 1),
      Measurement("A", "1", "A", "2", 2)
    ))))

    val result = DataFileHandler.validateData(example)
    result should equal (Left(DataFileValidationError()))
  }

  it should "accept a valid dataset" in {
    val example = TrafficData(List(TrafficMeasurement(1, List(
      Measurement("A", "1", "A", "2", 1),
      Measurement("A", "2", "B", "2", 1),
      Measurement("B", "2", "B", "1", 1),
      Measurement("B", "1", "A", "1", 1)
    ))))

    val result = DataFileHandler.validateData(example)
    result should equal (Right(example))
  }
}