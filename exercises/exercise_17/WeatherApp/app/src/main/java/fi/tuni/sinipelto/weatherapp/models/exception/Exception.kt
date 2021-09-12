package fi.tuni.sinipelto.weatherapp.models.exception

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList

data class Exception(

    @field:Attribute(name = "exceptionCode")
    @param:Attribute(name = "exceptionCode")
    val exceptionCode: String,

    @field:ElementList(name = "ExceptionText")
    @param:ElementList(name = "ExceptionText")
    val exceptionTextList: List<String>

)