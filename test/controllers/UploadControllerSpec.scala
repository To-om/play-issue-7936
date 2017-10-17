package controllers

import scala.concurrent.Await
import scala.concurrent.duration._

import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.api.test.Helpers.stubControllerComponents

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite


class UploadTest extends PlaySpec with GuiceOneServerPerSuite with Status {
  implicit val system: ActorSystem = ActorSystem("raw-body-parser-spec")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def controllerComponents: ControllerComponents = {
    val playBodyParsers = PlayBodyParsers(tfc = new InMemoryTemporaryFileCreator(100))
    stubControllerComponents(bodyParser = playBodyParsers.default, playBodyParsers = playBodyParsers)
  }

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[ControllerComponents].toInstance(controllerComponents))
      .build()

  "Uploading a small file should not return an error" in {
    val wsClient = app.injector.instanceOf[WSClient]
    val uploadURL = s"http://localhost:$port/upload"

    val fakeData = Source.repeat(ByteString("ABCD")).take(4)
    val response = Await.result(wsClient.url(uploadURL)
      .post(Source(FilePart("hello", "hello.txt", Option("text/plain"), fakeData) :: Nil)), 5.seconds)

    response.status mustBe OK
    response.body mustBe "hello.txt:16"
  }

  "Uploading a too large file should return an internal server error" in {
    val wsClient = app.injector.instanceOf[WSClient]
    val uploadURL = s"http://localhost:$port/upload"

    val fakeData = Source.repeat(ByteString("ABCD")).take(1024)
    val response = Await.result(wsClient.url(uploadURL)
      .post(Source(FilePart("hello", "hello.txt", Option("text/plain"), fakeData) :: Nil)), 5.seconds)

    response.status mustBe INTERNAL_SERVER_ERROR
    //response.body mustBe "hello.txt:4096"
  }
}