package eventjournal.api

import eventjournal.store.EventJournal
import zhttp.http.*

object EventJournalApi {
  def apply =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "eventjournal" => EventJournal.listAll.map(list => Response.text(list.mkString("\n")))
    }
}
