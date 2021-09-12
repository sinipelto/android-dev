package fi.tuni.sinipelto.weatherapp.models.target

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://xml.fmi.fi/namespace/om/atmosphericfeatures/1.0", prefix = "target")
data class TargetRepresentativePoint(

    @Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
    @field:Attribute(name = "href")
    @param:Attribute(name = "href")
    val href: String

)