package fi.tuni.sinipelto.weatherapp.models.omso

import fi.tuni.sinipelto.weatherapp.models.om.*
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Namespace(reference = "http://inspire.ec.europa.eu/schemas/omso/3.0", prefix = "omso")
@Root(name = "PointTimeSeriesObservation")
data class OmsoPointTimeSeriesObservation(

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Attribute(name = "id")
    @param:Attribute(name = "id")
    val id: String,

    @Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
    @field:Element(name = "phenomenonTime")
    @param:Element(name = "phenomenonTime")
    val phenomenonTime: OmPhenomenonTime,

    @Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
    @field:Element(name = "resultTime")
    @param:Element(name = "resultTime")
    val resultTime: OmResultTime,

    @Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
    @field:Element(name = "procedure")
    @param:Element(name = "procedure")
    val procedure: OmProcedure,

    @Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
    @field:Element(name = "parameter")
    @param:Element(name = "parameter")
    val parameter: OmParameter,

    @Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
    @field:Element(name = "observedProperty")
    @param:Element(name = "observedProperty")
    val observedProperty: OmObservedProperty,

    @Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
    @field:Element(name = "featureOfInterest")
    @param:Element(name = "featureOfInterest")
    val featureOfInterest: OmFeatureOfInterest,

    @Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
    @field:Element(name = "result")
    @param:Element(name = "result")
    val result: OmResult

)
