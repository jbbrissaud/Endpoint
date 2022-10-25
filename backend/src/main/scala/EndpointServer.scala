
package example

import zhttp.http.{Request,Header}
import zio.json._
import java.net.URLDecoder

////////////////////////////// lib specific

object ZhttpHttp:
  def getReq(req:Request) = 
      val method = req.method
      val path = req.path.toList.map(
        URLDecoder.decode( _, "UTF-8" )
      )
      val params = req.url.queryParams
      val headers: Map[String,List[String]] = 
        val map:collection.mutable.Map[String,List[String]] = collection.mutable.Map()
        for Header(nameCs,valueCs) <- req.headers.toChunk
        do
          val name = nameCs.toString()
          val value = valueCs.toString()
          if map contains name then
            map(name) = map(name) :+ value
          else 
            map(name) = List(value)
        map.toMap
      val body = req.bodyAsString  //Task
      (method.toString(),path,params,headers,"bodyToDo")

/////////////////////////////// main      

private def req2optJson(patterns: List[Pattern]
  ,method:String,path:List[String],params:Map[String,List[String]]
  ,headers:Map[String,List[String]],body:String): Option[String] = 
  class treatPatternException extends Exception

  def treatPattern(pat:Pattern,path:List[String],result:List[String]): (List[String],List[String]) =
    println(s"pat=${pat.toString()}")
    pat match
      case CteMethod(m) =>
        if m==method.toString() then 
          (path,result)
        else
          throw treatPatternException()
      case CtePath(str) =>
        if !path.isEmpty && path.head==str then
          (path.tail,result)
        else
          throw treatPatternException()
      case CteNamedPath(name,value) =>
        if path.length>=2 && path.head==name && path.tail.head==value then
          (path.tail.tail,result)
        else
          throw treatPatternException()
      case CteParam(name,value) =>
        if (params contains name) && (params(name) contains value) then
          (path,result)
        else
          throw treatPatternException()
      case CteHeader(name,value) =>
        if (headers contains name) && (headers(name) contains value) then
          (path,result)
        else
          throw treatPatternException()
      case CteBody(str) =>  // body is a Task
        ???
      case VarMethod() => 
        (path,result :+ method.toString())
      case VarPath() => 
        if !path.isEmpty then
          (path.tail,result :+ path.head)
        else
          throw treatPatternException()
      case VarNamedPath(name) =>
        if !path.isEmpty && path.head==name then
          path match
            case name1::value1::path1 if name1==name =>
              (path1,result :+ value1)
            case _ =>
              throw treatPatternException()
        else
          throw treatPatternException()
      case VarParam(name) => 
        if params contains name then
          (path,result :+ params(name).head)
        else 
          ("" +: path,result)
      case VarHeader(name) => 
        if headers contains name then
          (path,result :+ headers(name).head)
        else
          throw treatPatternException()
      case VarBody() => 
        (path,result :+ ???)  // body is a Task
      case VarNamedBody(name) => 
        (path,result :+ ???)  // body is a Task
      case _ => ???

  def loop(patterns:List[Pattern],path:List[String],result:List[String]):(List[String],List[String]) =
    patterns match
      case pat::patterns1 =>
        val (path1,result1) = treatPattern(pat,path,result)
        loop(patterns1,path1,result1)
      case _ =>
        (path,result)

  val (path1,result) = 
    try
      loop(patterns,path,List())
    catch
      case _:treatPatternException =>
        (List(""),List())
  if path1.isEmpty then
    val json = result.length match
      case 0 => ""
      case 1 => result.head
      case _ =>
        result.mkString("[",",","]")
    Some(json)
  else
    None

extension(endpoint:Endpoint[Unit])
  def unapply(req:Request): Option[Unit] =
    val (method,path,params,headers,body) = ZhttpHttp.getReq(req)
    //println(s"req=$req\npath=${path}\nparams=$params\nheaders=$headers")
    println(s"path=${path}\nparams=$params\nheaders=$headers")

    req2optJson(endpoint.patterns,method,path,params,headers,"body").match
      case None => None
      case Some(json) => Some(())

extension[A :JsonDecoder](endpoint:Endpoint[A])
  def unapply(req:Request): Option[A] =
    val (method,path,params,headers,body) = ZhttpHttp.getReq(req)
    //println(s"req=$req\npath=${path}\nparams=$params\nheaders=$headers")
    println(s"path=${path}\nparams=$params\nheaders=$headers")

    req2optJson(endpoint.patterns,method,path,params,headers,"body").match
      case None => None
      case Some(json) => json.fromJson[A].toOption
    
