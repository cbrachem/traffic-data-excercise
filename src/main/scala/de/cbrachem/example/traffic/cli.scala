package de.cbrachem.example.traffic

import cats.data.{Validated, ValidatedNel}
import com.monovore.decline.Argument
import io.circe._
import io.circe.generic.auto._
import io.circe.generic.semiauto.deriveEncoder
import io.circe.parser._
import io.circe.syntax._

import scala.collection.mutable

object Cli {
  /**
   * Custom command line argument type for Decline that directly gives us a parsed Intersection instance.
   */
  implicit val intersectionArgument: Argument[Intersection] = new Argument[Intersection] {
    override def read(string: String): ValidatedNel[String, Intersection] = {
      Intersection.fromString(string) match {
        case Some(intersection) => Validated.valid(intersection)
        case None => Validated.invalidNel(s"Invalid intersection: $string (needs to be of the form A1)")
      }
    }

    override def defaultMetavar: String = "intersection"
  }

  def displayResults[T](solution: ShortestPathSolution[T])(implicit tEncoder: Encoder[T]) = {
    implicit def encoder: Encoder[ShortestPathSolution[T]] = deriveEncoder
    println(solution.asJson)
  }

  def displayError(error: AppError) = {
    System.err.println("Error: " + error.display)
  }
}