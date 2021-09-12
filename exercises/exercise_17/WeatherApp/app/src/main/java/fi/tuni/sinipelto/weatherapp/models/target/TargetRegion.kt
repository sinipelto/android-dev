package fi.tuni.sinipelto.weatherapp.models.target

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Namespace


@Namespace(reference = "http://xml.fmi.fi/namespace/om/atmosphericfeatures/1.0", prefix = "target")
data class TargetRegion(

    @field:Attribute(name = "codeSpace")
    @param:Attribute(name = "codeSpace")
    val codeSpace: String

)