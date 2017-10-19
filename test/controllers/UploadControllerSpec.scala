package controllers

import akka.NotUsed

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Random
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.mvc.{ControllerComponents, PlayBodyParsers}
import play.api.mvc.MultipartFormData.FilePart
import play.api.test.Helpers.stubControllerComponents
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite


class UploadControllerSpec extends PlaySpec with GuiceOneServerPerSuite with Status {
  implicit val system: ActorSystem = ActorSystem("upload-file-test")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def controllerComponents: ControllerComponents = {
    val playBodyParsers = PlayBodyParsers(tfc = new InMemoryTemporaryFileCreator(100))
    stubControllerComponents(bodyParser = playBodyParsers.default, playBodyParsers = playBodyParsers)
  }

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[ControllerComponents].toInstance(controllerComponents))
      .build()

  val wsClient: WSClient = app.injector.instanceOf[WSClient]
  val uploadURL = s"http://localhost:$port/upload"

  def generateData(size: Int): Source[ByteString, NotUsed] = {
    val buffer = Array.ofDim[Byte](size)
    Random.nextBytes(buffer)
    Source.single(ByteString(buffer))
  }

  "Uploading a small file should not return an error" in {

    val body = Source(FilePart("hello", "hello.txt", Option("text/plain"), generateData(16)) :: Nil)
    val response = Await.result(wsClient.url(uploadURL)
      .post(body), 5.seconds)

    response.status mustBe OK
    response.body mustBe "hello.txt:16"
  }

  "Uploading a too large file should return an internal server error" in {

    val body = Source(FilePart("hello", "hello.txt", Option("text/plain"), generateData(4096)) :: Nil)
    val response = Await.result(wsClient.url(uploadURL)
      .post(body), 5.seconds)

    response.status mustBe INTERNAL_SERVER_ERROR
    //response.body mustNot equal ("hello.txt:4096")
  }
}