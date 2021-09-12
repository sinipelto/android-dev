package fi.tuni.sinipelto.weatherapp.models.wfs

import fi.tuni.sinipelto.weatherapp.models.omso.OmsoPointTimeSeriesObservation
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root


@Namespace(reference = "http://www.opengis.net/wfs/2.0", prefix = "wfs")
@Root(name = "FeatureCollection")
data class WfsFeatureCollection(

    @field:Attribute(name = "timeStamp")
    @param:Attribute(name = "timeStamp")
    val timeStamp: String,

    @field:Attribute(name = "numberMatched")
    @param:Attribute(name = "numberMatched")
    val numberMatched: String,

    @field:Attribute(name = "numberReturned")
    @param:Attribute(name = "numberReturned")
    val numberReturned: String,

    @field:Attribute(name = "schemaLocation")
    @param:Attribute(name = "schemaLocation")
    val schemaLocation: String,

    @Namespace(reference = "http://www.opengis.net/wfs/2.0", prefix = "wfs")
    @field:ElementList(name = "member", inline = true)
    @param:ElementList(name = "member", inline = true)
    val members: List<OmsoPointTimeSeriesObservation>

)
