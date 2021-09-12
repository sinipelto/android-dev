package fi.tuni.sinipelto.weatherapp.models.sams

import fi.tuni.sinipelto.weatherapp.models.sam.SamSampledFeature
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://www.opengis.net/samplingSpatial/2.0", prefix = "sams")
@Element(name = "SF_SpatialSamplingFeature")
data class SamsSfSpatialSamplingFeature(

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Attribute(name = "id")
    @param:Attribute(name = "id")
    val id: String,

    @field:Element(name = "sampledFeature")
    @param:Element(name = "sampledFeature")
    val sampledFeature: SamSampledFeature,

    @Namespace(reference = "http://www.opengis.net/samplingSpatial/2.0", prefix = "sams")
    @field:Element(name = "shape")
    @param:Element(name = "shape")
    val shape: SamsShape

)
