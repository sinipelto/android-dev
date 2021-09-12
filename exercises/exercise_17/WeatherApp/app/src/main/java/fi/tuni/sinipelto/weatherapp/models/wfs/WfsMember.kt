package fi.tuni.sinipelto.weatherapp.models.wfs

import fi.tuni.sinipelto.weatherapp.models.omso.OmsoPointTimeSeriesObservation
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Namespace(reference = "http://www.opengis.net/wfs/2.0", prefix = "wfs")
@Root(name = "member")
data class WfsMember(

    @Namespace(reference = "http://inspire.ec.europa.eu/schemas/omso/3.0", prefix = "omso")
    @field:Element(name = "PointTimeSeriesObservation")
    @param:Element(name = "PointTimeSeriesObservation")
    val pointTimeSeriesObservation: OmsoPointTimeSeriesObservation

)
