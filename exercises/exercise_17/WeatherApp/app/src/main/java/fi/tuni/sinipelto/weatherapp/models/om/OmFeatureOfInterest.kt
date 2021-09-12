package fi.tuni.sinipelto.weatherapp.models.om

import fi.tuni.sinipelto.weatherapp.models.sams.SamsSfSpatialSamplingFeature
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
data class OmFeatureOfInterest(

    @field:Element(name = "SF_SpatialSamplingFeature")
    @param:Element(name = "SF_SpatialSamplingFeature")
    val sfSpatialSamplingFeature: SamsSfSpatialSamplingFeature

)