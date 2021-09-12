package fi.tuni.sinipelto.weatherapp.models.om

import fi.tuni.sinipelto.weatherapp.models.wml2.Wml2MeasurementTimeseries
import fi.tuni.sinipelto.weatherapp.models.wml2.Wml2Point
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root


@Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
@Root(name = "Result", strict = false)
data class OmResult(

    @Namespace(reference = "http://www.opengis.net/waterml/2.0", prefix = "wml2")
    @field:ElementList(name = "MeasurementTimeseries")
    @param:ElementList(name = "MeasurementTimeseries")
    val measurementTimeseries: List<Wml2Point>
)
