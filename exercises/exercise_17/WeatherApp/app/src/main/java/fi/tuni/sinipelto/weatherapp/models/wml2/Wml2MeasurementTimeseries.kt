package fi.tuni.sinipelto.weatherapp.models.wml2

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Namespace(reference = "http://www.opengis.net/waterml/2.0", prefix = "wml2")
@Root(name = "MeasurementTimeseries", strict = false)
data class Wml2MeasurementTimeseries(

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Attribute(name = "id")
    @param:Attribute(name = "id")
    val id: String,

    @Namespace(reference = "http://www.opengis.net/waterml/2.0", prefix = "wml2")
    @field:ElementList(name = "point")
    @param:ElementList(name = "point")
    val measurementList: List<Wml2Point>

)