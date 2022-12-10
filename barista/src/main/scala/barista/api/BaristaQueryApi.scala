package barista.api

import barista.domain.BaristaRepo
import zhttp.http.*
import zio.*
import zio.json.*
import zio.json.internal.RetractReader

object BaristaQueryApi {

  def apply(): Http[BaristaRepo, Nothing, Any, Response] =
    Http.collectZIO { case Method.GET -> !! / "barista" =>
      BaristaRepo.findAll.map(_.map((id, brew) => (id.toString, brew)).toJson).map(Response.json)
    }

}
