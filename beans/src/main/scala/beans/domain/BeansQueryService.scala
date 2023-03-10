package beans.domain

import eventjournal.entity.BeanOrigin
import zio.{Ref, UIO, ZIO, ZLayer}

case class BeansQueryService(repo: BeansRepo) {
  def storedBeans: UIO[Map[BeanOrigin, Int]] = repo.storedBeans
}

object BeansQueryService {
  def storedBeans: ZIO[BeansQueryService, Nothing, Map[BeanOrigin, Int]] =
    ZIO.service[BeansQueryService].flatMap(_.storedBeans)

  val layer: ZLayer[BeansRepo, Nothing, BeansQueryService] =
    ZLayer.fromZIO(ZIO.service[BeansRepo].map(BeansQueryService(_)))
}
