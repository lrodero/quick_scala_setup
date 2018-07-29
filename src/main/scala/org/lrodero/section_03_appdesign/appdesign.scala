package org.lrodero.fpformortalsbook.section_03_appdesign

import scalaz.{NonEmptyList => NEL,  _}
import Scalaz._

import scala.concurrent.duration._

object Main extends App {

  final case class Epoch(millis: Long) extends AnyVal {
    def +(d: FiniteDuration): Epoch = Epoch(millis + d.toMillis)
    def diff(e: Epoch): FiniteDuration = (e.millis - millis).millis
  }

  trait Drone[F[_]] {
    def getBacklog: F[Int]
    def getAgents: F[Int]
  }

  final case class MachineNode(id: String)
  trait Machines[F[_]] {
    def getTime: F[Epoch]
    def getManaged: F[NEL[MachineNode]]
    def getAlive: F[Map[MachineNode, Epoch]]
    def start(node: MachineNode): F[MachineNode]
    def stop(node: MachineNode): F[MachineNode]
  }

  final case class WorldView(
    backlog: Int,
    agents: Int,
    managed: NEL[MachineNode],
    alive: Map[MachineNode, Epoch],
    pending: Map[MachineNode, Epoch],
    time: Epoch
  )

  final class DynAgents[F[_]](D: Drone[F], M: Machines[F])(implicit F: Monad[F]) {

    def initial: F[WorldView] = for {
      db <- D.getBacklog
      da <- D.getAgents
      mm <- M.getManaged
      ma <- M.getAlive
      mt <- M.getTime
    } yield WorldView(db, da, mm, ma, Map.empty, mt)

  }
}

