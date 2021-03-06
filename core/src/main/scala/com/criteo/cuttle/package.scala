package com.criteo

import scala.concurrent._

import cats.effect.IO
import cats.free._
import doobie._

package cuttle {

  /**
    * Used a return type for the [[Job]] side effect instead of `Unit`.
    *
    * We use `Future[Completed]` instead of `Future[Unit]` to avoid mistakes in user code because of
    * value discarding.
    */
  sealed trait Completed

  /**
    * The object to use to successfully complete a job side effect.
    *
    * {{{
    *   Future.successful(Completed)
    * }}}
    */
  case object Completed extends Completed
}

/**
  * Core cuttle concepts are defined here.
  *
  *  - A [[CuttleProject]] is basically a [[Workflow]] to execute.
  *  - [[Workflow]]s are directed acyclic graphs of [[Job]]s.
  *  - [[Scheduler]] is defined for a given [[Scheduling]] mechanism.
  *  - [[Execution]]s are created by a [[Scheduler]] for a given [[Job]] and [[SchedulingContext]].
  *  - [[Executor]] handles the [[SideEffect]]s execution.
  *  - [[SideEffect]]s are plain asynchronous Scala functions and can use [[com.criteo.cuttle.ExecutionPlatform]]s to
  *    access underlying resources.
  */
package object cuttle {

  /** Doobie transactor. See https://github.com/tpolecat/doobie. */
  type XA = Transactor[IO]
  private[cuttle] val NoUpdate: ConnectionIO[Int] = Free.pure(0)

  /** The side effect function represents the real job execution. It returns a `Future[Completed]` to
    * indicate the execution result (we use [[Completed]] here instead of `Unit` to avoid automatic value
    * discarding, but [[Completed]] do not maintain additional state).
    *
    * The cuttle [[Executor]] ensures that a scheduled side effect for a given [[SchedulingContext]] will be run
    * a least once, but cannot guarantee that it will be run exactly once. That's why the side effect function must
    * be idempotent, meaning that if executed for the same [[SchedulingContext]] it must produce the same result.
    *
    * A failed future means a failed execution.
    */
  type SideEffect[S <: Scheduling] = (Execution[S]) => Future[Completed]

  /**
    * Automatically provide a scala `scala.concurrent.ExecutionContext` for a given [[Execution]].
    * The threadpool will be chosen carefully by the [[Executor]].
    */
  implicit def scopedExecutionContext(implicit execution: Execution[_]): ExecutionContext = execution.executionContext

}
