package controllers

import java.nio.file.Files
import javax.inject.{ Inject, Singleton }

import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }

@Singleton
class UploadController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def upload = Action { implicit request: Request[AnyContent] =>
    val uploadedFiles = for {
      multipart <- request.body.asMultipartFormData.toSeq
      filePart <- multipart.files
      fileSize = Files.size(filePart.ref.path)
    } yield s"${filePart.filename}:$fileSize"

    Ok(uploadedFiles.mkString)
  }
}
