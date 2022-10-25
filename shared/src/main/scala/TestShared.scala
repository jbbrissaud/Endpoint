package example

import zio.json._

case class Name(first:String,last:String)
case class User(name:Name,age:Int)

val userEndpoint0 = GET

val userEndpoint1 = GET / ?[Int]("userid")

val userEndpoint2 = GET / ??[Double]("double") / ?[Int]("int")

val userEndpoint3 =
  GET / ?[User] ("user1") / ??[User]("user2") / !![User]("user3")

val userEndpoint4 =
  GET / "userid" / ?[String] / ?[Int]("age") / ??[String]("name") / 
    ??("oiseau","titi") / !![Double]("poids") / !!("header1","val1")

