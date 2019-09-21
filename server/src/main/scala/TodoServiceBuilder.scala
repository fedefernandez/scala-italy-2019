package fortysevendeg

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import fs2.Stream
import fortysevendeg.service._

import scala.util.control.NoStackTrace

object TodoServiceBuilder {

  case object TodoItemNotFoundError extends RuntimeException("Item not found") with NoStackTrace
  case object ItemIdNotAvailableError
      extends RuntimeException("Can't generate a new id")
      with NoStackTrace

  def apply[F[_]: Sync]: F[TodoService[F]] =
    Ref.of(Map.empty[Int, TodoItem]).map(new TodoServiceImpl[F](_))

  val random = new scala.util.Random(System.currentTimeMillis())

  val maxAttempts: Int = 1000

  def genId[F[_]: Sync](currentIds: Set[Int]): F[Int] = {

    @scala.annotation.tailrec
    def returnIfValid(id: Int, step: Int): F[Int] =
      if (!currentIds.contains(id)) {
        id.pure[F]
      } else if (step > maxAttempts) {
        ItemIdNotAvailableError.raiseError[F, Int]
      } else {
        returnIfValid(random.nextInt(Int.MaxValue), step + 1)
      }
    returnIfValid(random.nextInt(Int.MaxValue), 0)
  }

  class TodoServiceImpl[F[_]: Sync](refMap: Ref[F, Map[Int, TodoItem]]) extends TodoService[F] {

    def createTodoItem(request: CreateTodoItemRequest): F[TodoItem] =
      for {
        map   <- refMap.get
        newId <- genId(map.keySet)
        item = TodoItem(newId, request.text, request.completed, request.tags)
        _ <- refMap.update(_ + (newId -> item))
      } yield item

    def getTodoItem(request: GetTodoItemRequest): F[TodoItem] =
      refMap.get
        .map(_.get(request.id).toRight[Throwable](TodoItemNotFoundError))
        .rethrow

    def getTodoItemList(req: GetTodoItemsRequest): Stream[F, TodoItem] =
      Stream.eval(refMap.get.map(_.values.toList)).flatMap(Stream.emits)
  }

}
