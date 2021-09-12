package fi.tuni.sinipelto.weatherapp.models.gml

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
data class GmlPoint(

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Attribute(name = "id")
    @param:Attribute(name = "id")
    val id: String,

    @field:Attribute(name = "srsName")
    @param:Attribute(name = "srsName")
    val srsName: String,

    @field:Attribute(name = "srsDimension")
    @param:Attribute(name = "srsDimension")
    val srsDimension: String,

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Element(name = "name")
    @param:Element(name = "name")
    val name: String,

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Element(name = "pos")
    @param:Element(name = "pos")
    val pos: String

)
