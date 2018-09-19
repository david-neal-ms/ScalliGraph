package org.thp.scalligraph

import org.thp.scalligraph.controllers.Field
import play.api.libs.json.{JsString, Json, OWrites, Writes}

case class AttributeCheckingError(errors: Seq[AttributeError] = Nil) extends Exception(errors.mkString("[", "][", "]")) {
  override def toString: String = errors.mkString("[", "][", "]")
}
object AttributeCheckingError {
  implicit val invalidFormatAttributeErrorWrites: OWrites[InvalidFormatAttributeError] =
    Json.writes[InvalidFormatAttributeError]
  implicit val unknownAttributeErrorWrites: OWrites[UnknownAttributeError] = Json.writes[UnknownAttributeError]
  implicit val updateReadOnlyAttributeErrorWrites: OWrites[UpdateReadOnlyAttributeError] =
    Json.writes[UpdateReadOnlyAttributeError]
  implicit val missingAttributeErrorWrites: OWrites[MissingAttributeError] = Json.writes[MissingAttributeError]
  implicit val unsupportedAttributeErrorWrites: OWrites[UnsupportedAttributeError] =
    Json.writes[UnsupportedAttributeError]

  implicit val attributeErrorWrites: Writes[AttributeError] = Writes[AttributeError] {
    case ifae: InvalidFormatAttributeError ⇒
      invalidFormatAttributeErrorWrites.writes(ifae) + ("type" → JsString("InvalidFormatAttributeError"))
    case uae: UnknownAttributeError ⇒
      unknownAttributeErrorWrites.writes(uae) + ("type" → JsString("UnknownAttributeError"))
    case uroae: UpdateReadOnlyAttributeError ⇒
      updateReadOnlyAttributeErrorWrites.writes(uroae) + ("type" → JsString("UpdateReadOnlyAttributeError"))
    case mae: MissingAttributeError ⇒
      missingAttributeErrorWrites.writes(mae) + ("type" → JsString("MissingAttributeError"))
    case uae: UnsupportedAttributeError ⇒
      unsupportedAttributeErrorWrites.writes(uae) + ("type" → JsString("UnsupportedAttributeError"))
  }

  implicit val attributeCheckingErrorWrites: OWrites[AttributeCheckingError] =
    Json.writes[AttributeCheckingError]
}

sealed trait AttributeError extends Throwable {
  val name: String
  def withName(name: String): AttributeError
  def withModel(model: String): AttributeError
}

case class InvalidFormatAttributeError(name: String, format: String, acceptedInput: Seq[String], field: Field) extends AttributeError {
  override def toString = s"Invalid format for $name: $field, expected $format"
  override def withName(newName: String): InvalidFormatAttributeError =
    copy(name = newName)
  override def withModel(model: String): InvalidFormatAttributeError =
    copy(name = s"$model.$name")
}
case class UnknownAttributeError(name: String, field: Field) extends AttributeError {
  override def toString = s"Unknown attribute $name: $field"
  override def withName(newName: String): UnknownAttributeError =
    copy(name = newName)
  override def withModel(model: String): UnknownAttributeError =
    copy(name = s"$model.$name")
}
case class UpdateReadOnlyAttributeError(name: String) extends AttributeError {
  override def toString = s"Attribute $name is read-only"
  override def withName(newName: String): UpdateReadOnlyAttributeError =
    copy(name = newName)
  override def withModel(model: String): UpdateReadOnlyAttributeError =
    copy(name = s"$model.$name")
}
case class MissingAttributeError(name: String) extends AttributeError {
  override def toString = s"Attribute $name is missing"
  override def withName(newName: String): MissingAttributeError =
    copy(name = newName)
  override def withModel(model: String): MissingAttributeError =
    copy(name = s"$model.$name")
}
case class UnsupportedAttributeError(name: String) extends AttributeError {
  override def toString = s"Attribute $name is not supported"
  override def withName(newName: String): UnsupportedAttributeError =
    copy(name = newName)
  override def withModel(model: String): UnsupportedAttributeError =
    copy(name = s"$model.$name")
}
