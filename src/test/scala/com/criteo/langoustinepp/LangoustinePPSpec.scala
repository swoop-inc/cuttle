package com.criteo.langoustinepp

import org.scalatest.FunSuite

case class TestDependencyDescriptor()

object TestDependencyDescriptor {
  implicit val defDepDescr = TestDependencyDescriptor()
}

case class TestScheduling(config: Unit = ()) extends Scheduling {
  type Context = Unit
  type DependencyDescriptor = TestDependencyDescriptor
  type Config = Unit
}

class LangoustinePPSpec extends FunSuite {
  test("Graph building") {
    val jobs =
      (0 to 3).map (i => Job(i.toString, TestScheduling()))
    val graph = (jobs(1) and jobs(2)) dependsOn jobs(0) dependsOn jobs(3)
    assert(graph.vertices.size == 4)
    assert(graph.edges.size == 3)
  }
}