package fortysevendeg

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import fortysevendeg.service._
import higherkindness.mu.rpc.ChannelForAddress
import higherkindness.mu.rpc.channel.{AddInterceptor, UsePlaintext}
import higherkindness.mu.rpc.channel.metrics.MetricsChannelInterceptor
import higherkindness.mu.rpc.prometheus.PrometheusMetrics
import io.prometheus.client.CollectorRegistry

object TodoClientApp extends IOApp {

  lazy val cr: CollectorRegistry = new CollectorRegistry()

  val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger("client")

  val clientResource: Resource[IO, TodoService[IO]] =
    for {
      metricsOps <- Resource.liftF(PrometheusMetrics.build[IO](cr, "client"))
      client <- TodoService.client[IO](
        ChannelForAddress("localhost", 50051),
        List(UsePlaintext(), AddInterceptor(MetricsChannelInterceptor(metricsOps))))
    } yield client

  override def run(args: List[String]): IO[ExitCode] = clientResource.use { client =>
    for {
      r1 <- client
        .createTodoItem(CreateTodoItemRequest("Item 1", completed = false, List("tag1", "tag2")))
      _  <- IO(logger.info(s"Create item response: $r1"))
      r2 <- client.getTodoItem(GetTodoItemRequest(r1.id))
      _  <- IO(logger.info(s"Get item response: $r2"))
      r3 <- client
        .createTodoItem(CreateTodoItemRequest("Item 2", completed = false, List("tag2", "tag3")))
      _  <- IO(logger.info(s"Create item response: $r3"))
      r4 <- client.getTodoItem(GetTodoItemRequest(r3.id))
      _  <- IO(logger.info(s"Get item response: $r4"))
      r5 <- client.getTodoItemList(GetTodoItemsRequest()).compile.toList
      _  <- IO(logger.info(s"Get item list response: $r5"))
    } yield ExitCode.Success
  }

}
