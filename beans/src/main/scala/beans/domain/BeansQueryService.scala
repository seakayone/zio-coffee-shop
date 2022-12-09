package beans.domain

import coffeeshop.entity.BeanOrigin
import zio.{Ref, UIO, ZIO, ZLayer}

case class BeansQueryService(repo: BeansInventoryRepo) {
  def storedBeans: UIO[Map[BeanOrigin, Int]] = repo.storedBeans
}

object BeansQueryService {
  def storedBeans: ZIO[BeansQueryService, Nothing, Map[BeanOrigin, Int]] =
    ZIO.service[BeansQueryService].flatMap(_.storedBeans)

  val layer: ZLayer[BeansInventoryRepo, Nothing, BeansQueryService] =
    ZLayer.fromZIO(ZIO.service[BeansInventoryRepo].map(BeansQueryService(_)))
}
