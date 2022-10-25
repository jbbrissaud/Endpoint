package example

import zio.json._
import sttp.client3._
import sttp.model.{Method,Uri}

/////////////////////// lib specific

val UrlServer = "http://localhost:8090"

object SttpClient3:
  def setReq(method:String,path:List[String],params:Map[String, List[String]]
    ,headers:Map[String, List[String]],body:String) =
    val params1 = params.map((name,values)=>(name,values.head))
    val uri = uri"$UrlServer/$path?$params1"
    //println(s"uri=$uri")
    val headers1 = headers.map((name,values)=>(name,values.head))
    val req = basicRequest
      .method(Method(method),uri)
      .headers(headers1)
    val req1 = 
      if !(List(Method.GET) contains req.method) then  // à peaufiner
        req.body(body)
      else
        req
    req1

/////////////////////// main

private def getDatas[A :JsonEncoder](a:A): List[String] =
  val jsonBase = a.toJson
  if jsonBase.startsWith("[") then //seulement si Tuple
    val json = jsonBase.drop(1).dropRight(1)  
    var opened = 0
    var commas:List[Int] = List()
    for (c,i) <- json.zipWithIndex do
      if List('[','{','(') contains c then
        opened += 1
      else if List(']','}',')') contains c then
        opened -= 1
      else if c==',' && opened==0 then
        commas = commas :+ i
    //println(s"parens=$commas")
    val results_n = commas.foldLeft((List[String](),0)) {
      (list_i,j) => 
        val (list,i) = list_i
        (list:+json.substring(i,j),j+1)
    }
    val (results,n) = results_n
    results :+ json.substring(n,json.length)
  else
    List(jsonBase)

private def a2req(patterns:List[Pattern],datas1:List[String]): (String,List[String],Map[String, List[String]]
  ,Map[String, List[String]],String) =
    var datas = datas1
    var method: String = ""
    var path = List[String]()
    var params = Map[String,List[String]]()
    var headers = Map[String,List[String]]()
    var body: String = ""
    val bodyDict:collection.mutable.Map[String,List[String]] = collection.mutable.Map()
    for 
      pat <- patterns
    do
      pat match
        case CteMethod(m) => method = m
        case CtePath(str)  => 
          path = path :+ str
        case CteNamedPath(name,value)  => 
          path = path ++ List(name,value)
        case CteParam(name,value) => 
          if params contains name then
            params = params.updated(name,params(name) :+ value)
          else
            params = params.updated(name,List(value))
        case CteHeader(name,value) =>
          if (headers contains name) then
            headers = headers.updated(name,headers(name) :+ value)
          else
            headers = headers.updated(name,List(value))
        case CteBody(str) =>
          body = str
        case VarMethod() => 
          method = datas.head
          datas = datas.tail
        case VarPath() => 
          path = path :+ datas.head
          datas = datas.tail
        case VarNamedPath(name) => 
          path = path :+ name :+ datas.head
          datas = datas.tail
        case VarParam(name) => 
          if params contains name then
            params = params.updated(name,params(name) :+ datas.head)
          else 
            params = params.updated(name,List(datas.head))
          datas = datas.tail
        case VarHeader(name) => 
          if headers contains name then
            headers = headers.updated(name,headers(name) :+ datas.head)
          else 
            headers = headers.updated(name,List(datas.head))
          datas = datas.tail
        case VarBody() => 
          body = datas.head  // body is a Task
          datas = datas.tail
        case VarNamedBody(name) => 
          if bodyDict contains name then
            bodyDict(name) = bodyDict(name) :+ datas.head
          else
            bodyDict(name) = List(datas.head)
          datas = datas.tail
        case _ => ()
    if bodyDict.nonEmpty then
      body = bodyDict.toJson
    (method,path,params,headers,body)

/////////////////////////// public    

extension(endpoint:Endpoint[Unit])
  def apply(a:Unit): RequestT[Identity, Either[String, String], Any] =
    val datas = List()
    //println(s"json=${""}\ndatas=$datas")
    val (method,path,params,headers,body) = a2req(endpoint.patterns,datas)
    SttpClient3.setReq(method,path,params,headers,body)

extension[A :JsonEncoder](endpoint:Endpoint[A])
  def apply(a:A): RequestT[Identity, Either[String, String], Any] =
    val datas = getDatas(a)
    //println(s"json=${a.toJson}\ndatas=$datas")
    val (method,path,params,headers,body) = a2req(endpoint.patterns,datas)
    SttpClient3.setReq(method,path,params,headers,body)
