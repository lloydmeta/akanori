package org.beachape.server

import trendServer.gen._
import org.beachape.actors._
import trendServer.gen.TrendResult
import scala.concurrent.{ Future, Await }
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.ActorRef
import scala.concurrent.duration._
import collection.JavaConversions._
import trendServer.gen.TrendResult

class TrendServer(mainOrchestrator: ActorRef) extends TrendThriftServer.Iface {
  implicit val timeout = Timeout(600 seconds)

  implicit def toIntegerTrendResultsList(lst: List[TrendResult]) =
    seqAsJavaList(lst.map(i => i: TrendResult))

  override def time: Long = {
    val now = System.currentTimeMillis / 1000
    println("somebody just asked me what time it is: " + now)
    now
  }

  override def currentTrends = {
    val listOfReverseSortedTermsAndScoresFuture = ask(mainOrchestrator, 'getLatest)
    val listOfReverseSortedTermsAndScores = Await.result(listOfReverseSortedTermsAndScoresFuture, 600 seconds).asInstanceOf[List[(String, Double)]]
    val listOfTrendResults = for (result <- listOfReverseSortedTermsAndScores) yield {
      result match {
        case (term: String, score: Double) => new TrendResult(term, score)
        case _ => new TrendResult("fail", -55378008)
      }
    }
    listOfTrendResults
  }
}