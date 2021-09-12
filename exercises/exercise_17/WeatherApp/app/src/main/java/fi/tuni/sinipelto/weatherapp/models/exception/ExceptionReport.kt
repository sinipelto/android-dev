package fi.tuni.sinipelto.weatherapp.models.exception

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "ExceptionReport")
data class ExceptionReport(

    @field:Attribute(name = "version")
    @param:Attribute(name = "version")
    val version: String,

    @field:ElementList(name = "Exception")
    @param:ElementList(name = "Exception")
    val exception: List<Exception>

)
