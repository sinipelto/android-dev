package fi.tuni.sinipelto.weatherapp.models.gml

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
data class GmlName(

    @field:Attribute(name = "codeSpace")
    @param:Attribute(name = "codeSpace")
    val codeSpace: String

)