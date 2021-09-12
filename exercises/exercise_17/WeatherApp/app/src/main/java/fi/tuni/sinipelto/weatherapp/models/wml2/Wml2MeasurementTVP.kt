package fi.tuni.sinipelto.weatherapp.models.wml2

import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Namespace(reference = "http://www.opengis.net/waterml/2.0", prefix = "wml2")
@Root(name = "MeasurementTVP", strict = false)
data class Wml2MeasurementTVP(

    @Namespace(reference = "http://www.opengis.net/waterml/2.0", prefix = "wml2")
    @field:Element(name = "time")
    @param:Element(name = "time")
    val time: String,

    @Namespace(reference = "http://www.opengis.net/waterml/2.0", prefix = "wml2")
    @field:Element(name = "value")
    @param:Element(name = "value")
    val value: String

)