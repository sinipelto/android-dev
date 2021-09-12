package fi.tuni.sinipelto.weatherapp.models.sams

import fi.tuni.sinipelto.weatherapp.models.gml.GmlPoint
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://www.opengis.net/samplingSpatial/2.0", prefix = "sams")
data class SamsShape(

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Element(name = "Point")
    @param:Element(name = "Point")
    val point: GmlPoint

)
