package fi.tuni.sinipelto.weatherapp.models.target

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://xml.fmi.fi/namespace/om/atmosphericfeatures/1.0", prefix = "target")
data class TargetLocationCollection(

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Attribute(name = "id")
    @param:Attribute(name = "id")
    val id: String,

    @Namespace(
        reference = "http://xml.fmi.fi/namespace/om/atmosphericfeatures/1.0",
        prefix = "target"
    )
    @field:Element(name = "member")
    @param:Element(name = "member")
    val member: TargetMember

)
