package fi.tuni.sinipelto.weatherapp.models.om

import fi.tuni.sinipelto.weatherapp.models.gml.GmlTimeInstant
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
data class OmResultTime(

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Element(name = "TimeInstant")
    @param:Element(name = "TimeInstant")
    val timeInstant: GmlTimeInstant

)
