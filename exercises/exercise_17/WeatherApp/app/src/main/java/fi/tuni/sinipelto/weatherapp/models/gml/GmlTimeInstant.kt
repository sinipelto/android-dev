package fi.tuni.sinipelto.weatherapp.models.gml

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
data class GmlTimeInstant(

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Attribute(name = "id")
    @param:Attribute(name = "id")
    val id: String,

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Element(name = "timePosition")
    @param:Element(name = "timePosition")
    val timePosition: String

)