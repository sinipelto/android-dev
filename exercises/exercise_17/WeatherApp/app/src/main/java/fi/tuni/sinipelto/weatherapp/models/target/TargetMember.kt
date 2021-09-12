package fi.tuni.sinipelto.weatherapp.models.target

import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://xml.fmi.fi/namespace/om/atmosphericfeatures/1.0", prefix = "target")
data class TargetMember(

    @Namespace(
        reference = "http://xml.fmi.fi/namespace/om/atmosphericfeatures/1.0",
        prefix = "target"
    )
    @field:Element(name = "Location")
    @param:Element(name = "Location")
    val location: TargetLocation

)
