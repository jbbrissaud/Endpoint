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
    //println(s"filename=$filename")
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

  val config: CorsConfig =
      CorsConfig(allowedOrigins = _ => true, allowedMethods = Some(Set(Method.GET, Method.DELETE)))

  // execute toujours les patterns de appTest. bug en cours de correction zhttp      
  private val app = appBase ++ appTest
  def run =
    Server.start(8090, app @@cors(config))

end WebServer

