package fi.tuni.sinipelto.weatherapp.models.wml2

import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Namespace(reference = "http://www.opengis.net/waterml/2.0", prefix = "wml2")
@Root(name = "point", strict = false)
data class Wml2Point(

    @Namespace(reference = "http://www.opengis.net/waterml/2.0", prefix = "wml2")
    @field:Element(name = "MeasurementTVP")
    @param:Element(name = "MeasurementTVP")
    val measurementTvp: Wml2MeasurementTVP

)