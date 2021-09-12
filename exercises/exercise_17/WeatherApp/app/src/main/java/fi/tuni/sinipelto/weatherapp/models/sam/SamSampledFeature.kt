package fi.tuni.sinipelto.weatherapp.models.sam

import fi.tuni.sinipelto.weatherapp.models.target.TargetLocationCollection
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://www.opengis.net/sampling/2.0", prefix = "sam")
data class SamSampledFeature(

    @Namespace(reference = "http://xml.fmi.fi/namespace/om/atmosphericfeatures/1.0", prefix = "target")
    @field:Element(name = "LocationCollection")
    @param:Element(name = "LocationCollection")
    val locationCollection: TargetLocationCollection
)
