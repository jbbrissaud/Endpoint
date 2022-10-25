package example
import zio.json._
import zhttp.http._

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


