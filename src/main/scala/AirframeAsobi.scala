import wvlet.log.LogSupport
import wvlet.airframe.codec.MessageCodec
import wvlet.airframe.http.Endpoint
import wvlet.airframe.http.HttpMethod
import wvlet.airframe.http.Http
import wvlet.airframe.http.RxRouter
import wvlet.airframe.http.netty.Netty
import cats.effect.IO
import scala.concurrent.duration.{FiniteDuration, DurationInt}
import wvlet.airframe.http.RPCContext
import scala.concurrent.Future
import wvlet.airframe.http.HttpMessage.Response
import wvlet.airframe.http.HttpMessage.Request
import scala.collection.mutable.ListMap
import wvlet.airframe.http.netty.NettyServer
import wvlet.airframe.newDesign
import wvlet.airframe.Session

package airframeasobi {
  object MyApi {
    // Model classes
    case class ServerInfo(version: String, ua: Option[String])
    case class User(id: Int, name: String)
    case class NewUserRequest(name: String)
  }

// Web server definition
  @Endpoint(path = "/v1")
  class MyApi extends LogSupport {
    import MyApi._
    import scala.concurrent.ExecutionContext.Implicits.global

    @Endpoint(method = HttpMethod.GET, path = "/info")
    def serverInfo(using request: Request): Future[ServerInfo] = {
      info("serverInfo")
      Future(ServerInfo(version = "1.0", request.userAgent))
    }

    @Endpoint(method = HttpMethod.GET, path = "/user")
    def getUser(id: Int): User = User(id, "leo")

    @Endpoint(method = HttpMethod.POST, path = "/user")
    def createNewUser(req: NewUserRequest): User = User(1, req.name)

    @Endpoint(method = HttpMethod.GET, path = "/custom_response")
    def customResponse: Response = {
      val response = Http.response().withContent("hello airframe-http")
      response
    }
  }

  @Endpoint(path = "/v1")
  class MyAdminApi extends LogSupport {
    import MyApi._

    @Endpoint(method = HttpMethod.GET, path = "/info")
    def serverInfo(using request: Request): ServerInfo = {
      info("serverInfo")
      ServerInfo(version = "1.0", request.userAgent)
    }
  }

  class MyAppServer(server: NettyServer) {
    export server.awaitTermination
    export server.stop
  }

  class MyAdminServer(server: NettyServer) {
    export server.awaitTermination
    export server.stop
  }

  class MyService(myAppServer: MyAppServer, myAdminServer: MyAdminServer) {
    def awaitTermination(): Unit = {
      myAppServer.awaitTermination()
      myAdminServer.awaitTermination()
    }

    def stop: Unit = {
      myAppServer.stop()
      myAdminServer.stop()
    }
  }

  case class ServiceConfig(port: Int, adminPort: Int)
  def run(args: Seq[String]): Unit = {
    codecSample()

    val design = newDesign
      .bind[ServiceConfig]
      .toInstance(ServiceConfig(8080, 8081))
      .bind[MyAppServer]
      .toProvider { (config: ServiceConfig, session: Session) =>
        MyAppServer(
          Netty.server
            .withName("myapp")
            .withRouter(RxRouter.of[MyApi])
            .withPort(config.port)
            .newServer(session)
        )
      }
      .bind[MyAdminServer]
      .toProvider { (config: ServiceConfig, session: Session) =>
        MyAdminServer(
          Netty.server
            .withName("admin")
            .withRouter(RxRouter.of[MyAdminApi])
            .withPort(config.adminPort)
            .newServer(session)
        )
      }

    val startServer: IO[Unit] = IO {
      design.build[MyService] { service =>
        service.awaitTermination()
      }
    }

    val program = for {
      _ <- startServer.start
      _ <- IO.println("Server started")
      _ <- IO.sleep(1000.milliseconds)
      fb <- IO.blocking {
        // Accessing the server using an http client
        val client = Http.client.newSyncClient("http://localhost:8080")

        val v = client.readAs[MyApi.ServerInfo](Http.GET("/v1/info"))
        val u = client.call[MyApi.NewUserRequest, MyApi.User](
          Http.POST("/v1/user"),
          MyApi.NewUserRequest("Ann")
        )
        println(v)
        println(u)

        val adminClient = Http.client.newSyncClient("http://localhost:8081")
        val av = adminClient.readAs[MyApi.ServerInfo](Http.GET("/v1/info"))
        println(av)
      }.start
      _ <- fb.join
    } yield ()

    val runtime = cats.effect.unsafe.implicits.global
    program.unsafeRunSync()(runtime)
  }

  case class Person(name: String, age: Int)

  def codecSample(): Unit = {
    val codec = MessageCodec.of[Person]

    val a = Person("leo", 20)
    val json = codec.toJson(a)
    val msgpack = codec.toMsgPack(a)
    msgpack.foreach(printf("%02x ", _))
    println()

    println(codec.fromJson(json).name)
    printf("%d\n", codec.fromMsgPack(msgpack).age)
  }
}
