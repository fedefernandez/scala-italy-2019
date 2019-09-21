package fortysevendeg

import cats.effect.{ExitCode, IO, IOApp}
import fortysevendeg.service._
import higherkindness.mu.rpc.prometheus.PrometheusMetrics
import higherkindness.mu.rpc.server.{AddService, GrpcServer}
import higherkindness.mu.rpc.server.interceptors.implicits._
import higherkindness.mu.rpc.server.metrics.MetricsServerInterceptor
import io.prometheus.client.CollectorRegistry
import io.grpc.ServerServiceDefinition

object TodoServerApp extends IOApp {

  lazy val cr: CollectorRegistry = new CollectorRegistry()

  override def run(args: List[String]): IO[ExitCode] = {

    val serviceImpl: IO[TodoService[IO]] = TodoServiceBuilder[IO]

    def serviceDefinition(implicit HC: TodoService[IO]): IO[ServerServiceDefinition] =
      TodoService.bindService[IO]

    for {
      service    <- serviceImpl
      metricsOps <- PrometheusMetrics.build[IO](cr, "server")
      serviceDef <- serviceDefinition(service)
        .map(_.interceptWith(MetricsServerInterceptor(metricsOps)))
      server <- GrpcServer.default[IO](50051, List(AddService(serviceDef)))
      _      <- GrpcServer.server[IO](server)
    } yield ExitCode.Success
  }
}
