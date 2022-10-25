package example

import sttp.client3.{SimpleHttpClient,Response} //{Request, Response, SimpleHttpClient, UriContext, asStringAlways, basicRequest}
import concurrent.ExecutionContext.Implicits.global
import scala.util.Success
import zio.json._


given JsonEncoder[Name] = DeriveJsonEncoder.gen[Name]
given JsonEncoder[User] = DeriveJsonEncoder.gen[User]

object Test:
  def f =
    val client = SimpleHttpClient()
    val req0 = userEndpoint0(())
    val req1 = userEndpoint1(7)
    val req2 = userEndpoint2(3.14,2)
    val req3 = userEndpoint3(User(Name("inPath","doe"),41),User(Name("inParam","ji"),82),User(Name("inHeader","ok"),100))
    val req4 = userEndpoint4("id1",100,"nom1",40.2)
    for req <- List(req0,req1,req2,req3,req4) do
      println(s"req=$req")
      val response = client.send(req)
      response.onComplete {value => value match
        case Success(Response(Right(v),_,_,_,_,_)) =>
          println(s"response: ${v}")
        case _ =>
          println(s"bad response: ${value}")
      }