package fi.tuni.sinipelto.weatherapp.models.om

import fi.tuni.sinipelto.weatherapp.models.gml.GmlTimePeriod
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
data class OmPhenomenonTime(

    @field:Element(name = "TimePeriod")
    @param:Element(name = "TimePeriod")
    val timePeriod: GmlTimePeriod

)
