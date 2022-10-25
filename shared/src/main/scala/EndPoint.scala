package example

import zio.json._
import tupler._

//////////////////////////

////////////////// Endpoint
////// an Endpoint is a value used by the client with EndpointClient and by the server with EndpointServer.

////// syntax:
////// (method|method("GET")|GET|POST|...) ( / other)*
////// where other = 
//////   String | ?[A] | ?[A](name) | ?(name,value)                     // path
//////   ??[A](name) | ??(name,value)                                   // param
//////   !![A](name) | !!(name,value)                                   // header
//////   body(value) | body[A] | $$[A](name) | $$(name,value)           // body

trait Pattern
trait CtePattern extends Pattern
trait VarPattern extends Pattern
case class CteMethod(method:String) extends CtePattern
case class CtePath(name:String) extends CtePattern
case class CteNamedPath(name:String,value:String) extends CtePattern
case class CteParam(name:String,value:String) extends CtePattern
case class CteHeader(name:String,value:String) extends CtePattern
case class CteBody(name:String) extends CtePattern
case class CteNamedBody(name:String,value:String) extends CtePattern
case class VarMethod() extends VarPattern
case class VarPath() extends VarPattern
case class VarNamedPath(name: String) extends VarPattern
case class VarParam(name:String) extends VarPattern
case class VarHeader(name:String) extends VarPattern
case class VarBody() extends VarPattern
case class VarNamedBody(name: String) extends VarPattern

class Typed[A](val pat: VarPattern)

/////////////// Syntax

// String: CtePath

def ?(name:String,value:String) =
  new CteNamedPath(name:String,value:String)

def ??(name:String,value:String) =
  new CteParam(name:String,value:String)

def !!(name:String,value:String) =
  new CteHeader(name:String,value:String)

def body(value:String) = 
  new CteBody(value)

def $$(name:String,value:String) =
  new CteNamedBody(name:String,value:String)

def ?[A] = 
  new Typed[A](VarPath())

def ?[A](name:String) = 
  new Typed[A](VarNamedPath(name))

def ??[A](name:String) =
  new Typed[A](VarParam(name))
  
def !![A](name:String) =
  new Typed[A](VarHeader(name))
  
def body[A] =
  new Typed[A](VarBody())

def $$[A](name:String) =
  new Typed[A](VarNamedBody(name))
  
/////////////////////////////////////// trait Endpoint[A]

class Endpoint[A](val patterns: List[Pattern]):
  self =>
  // syntax
  infix def /(s:String): Endpoint[A] =
    new Endpoint[A](self.patterns :+ CtePath(s))
    
  infix def /(pat:CtePattern): Endpoint[A] =
    new Endpoint[A](self.patterns :+ pat)
    
  infix def /[B,C](t:Typed[B])(using tupler:Tupler.Aux[A,B,C]): Endpoint[C] =
    new Endpoint[C](self.patterns :+ t.pat)

/////////////// Endpoint creation

def method: Endpoint[Tuple1[String]] = new Endpoint[Tuple1[String]](List(VarMethod()))

def method(m:String): Endpoint[Unit] = new Endpoint[Unit](List(CteMethod(m)))

def GET: Endpoint[Unit] = method("GET")

