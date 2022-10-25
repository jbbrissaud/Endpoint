package example

import zhttp.http._
import zhttp.service.Server
import zio._
import zio.json._

import java.io.File
import zhttp.http.Middleware.cors
import zhttp.http.middleware.Cors.CorsConfig

object WebServer extends ZIOAppDefault:

  def getFile(filename:String) =
    println(s"filename=$filename")
    val file = new File(filename)
    Http.fromFile(file).orDie
  
  private val appBase = 
    Http.collectHttp[Request] { 
      case Method.GET -> !! / "index.html" =>
        getFile("frontend/static/index.html")
      case Method.GET -> !! / "favicon.ico" =>
        getFile("frontend/static/plante.ico")
      case Method.GET -> !! / "static" / "main.js" =>
        // "frontend/target/scala-3.2.0/frontend-fastopt/main.js"
        getFile("frontend/target/esbuild/bundle.js")
      case Method.GET -> !! / "static" / "bundle.js.map" =>
        getFile("frontend/target/esbuild/bundle.js.map")
      case Method.GET -> !! / "static" / name =>
        getFile(s"frontend/static/$name")
    }

  /////////////////////////////////////

  given JsonDecoder[Name] = DeriveJsonDecoder.gen[Name]
  given JsonDecoder[User] = DeriveJsonDecoder.gen[User]

  private val appTest: Http[Any, Nothing, Request, Response] =
    Http.collect[Request] {
      case userEndpoint4(user,age,name,poids)  => 
        Response.text(s"${(user,age,name,poids)}")
      case userEndpoint3(user1,user2,user3) => 
        Response.text(s"user1=$user1  user2=$user2  user3=$user3")
      case userEndpoint2(d,n) => Response.text(s"d=$d  n=$n")
      case userEndpoint1(n) => Response.text(s"n=$n")
      case userEndpoint0(_) => Response.text(s"rien=0")  // unreachable
    }

  /////////////////////////////////////

  val config: CorsConfig =
      CorsConfig(allowedOrigins = _ => true, allowedMethods = Some(Set(Method.GET, Method.DELETE)))

  // execute toujours les patterns de appTest. bug en cours de correction zhttp      
  private val app = appBase ++ appTest
  def run =
    Server.start(8090, app @@cors(config))

end WebServer

