## I. Start the app

### 1. Start backend

#### - in a console (in the main directory, the one with the build.sbt file)

```shell
sbt "~backend / run"
```

### 2. Start frontend

#### - in another console (also in the main directory)

```shell
sbt
(in sbt)  ~ frontend / fastLinkJS / esBuild
```

### 3. Test the app

Then you can access the app at http://localhost:8090/index.html

## II. How to use the lib
This is yet another lib to have HTTP endpoints. An Endpoint can be seen as a function to create a request, on the client side, and as a pattern to match a request, on the server side.
One can match on the path, the parameters, the headers and the body of the request. For instance:
```scala
val myEndpoint = GET / "userid" / ?[String] / ??[Int]("age") / !!("role","admin")
```
describes a request with a GET method, a path starting with "userid" followed by a String, a "age" parameter which is an Int,
and a header "role" with the value "admin". Its type is Endpoint[(String,Int)].
You can use it on the client side to generate a request:
```scala
val myRequest = myEndpoint("id243",32)  // method: GET , uri: /userid/id243?age=32 , header: role=admin
```
and you can use it on the server side to match a request:
```scala
myRequest match
  case myEndpoint(thisId,thisAge) => ???
```
The endpoint is shared by the js client and the JVM server, and can use any type in its description (not only String and Int). You just need to provide for the new types a zio-json JsonEncoder if you use the js client,  and a zio-json JsonDecoder if you use the JVM server.

The order of the patterns between the "/" determines the order of parameters. You don't have to start with the path and, for convenience, "?[A](name)" describes a path "/<name>/<json encoding of a A>". For instance:
```scala
GET / ??[Int]("age") / ?[String]("userid") / ?[String]("nickname")
```
is an Endpoint\[(Int,String,String)\] which matches a request with path /userid/id243/nickname/joe?age=32 .

## caveats
The type of Request to match or generate depends on the framework used.
Currently, only the zhttp.HTTP.Request is matched on the server, and only the sttp.client3.Request is generated on the client. However, the code to modify this is only getReq and setReq, given the following notion of request:
a method: String , like "GET" or "POST"
a path: List[String] (without the parameters)
the parameters: Map[String,List[String]]
the headers: Map[String,List[String]]
a body: String



