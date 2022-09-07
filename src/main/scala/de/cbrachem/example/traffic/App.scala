package de.cbrachem.example.traffic

import cats.implicits._
import com.monovore.decline.{CommandApp, Opts}

import java.nio.file.Path
import Cli.{displayError, displayResults, intersectionArgument}
import io.circe._, io.circe.generic.semiauto._



object App extends CommandApp(
  name = "traffic-data-excercise",
  header =
    """Gives you the fastest route between two intersections.
      |Starting and ending intersections must be given in the form A1, B5 etc.""".stripMargin,
  main = {
    val dataFileOpt = Opts.argument[Path](metavar = "data file")
    val startOpt = Opts.argument[Intersection](metavar = "start")
    val endOpt = Opts.argument[Intersection](metavar = "end")

    implicit val intersectionEncoder: Encoder[Intersection] = deriveEncoder

    (dataFileOpt, startOpt, endOpt).mapN { (dataFile, start, end) =>
      val result = for {
        data <- DataFileHandler.loadAndValidateData(dataFile)
        graph = Graph.build(data.average)
        shortestPathInfo <- Graph.findShortestPath(graph, start, end)
      } yield (shortestPathInfo)

      result match {
        case Right(solution) => displayResults(solution)
        case Left(error) => displayError(error)
      }
    }
  }
)
