package fi.tuni.sinipelto.weatherapp.models.target

import fi.tuni.sinipelto.weatherapp.models.gml.GmlName
import org.simpleframework.xml.*

@Namespace(reference = "http://xml.fmi.fi/namespace/om/atmosphericfeatures/1.0", prefix = "target")
@Root(strict = false)
data class TargetLocation(

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Attribute(name = "id")
    @param:Attribute(name = "id")
    val id: String,

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Element(name = "identifier")
    @param:Element(name = "identifier")
    val identifier: String,

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Path(value = "name")
    @param:Path(value = "name")
    @field:Element(name = "name", required = false)
    @param:Element(name = "name", required = false)
    val name: GmlName?,

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Path(value = "geoid")
    @param:Path(value = "geoid")
    @field:Element(name = "name", required = false)
    @param:Element(name = "name", required = false)
    val geoid: GmlName?,

    @Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
    @field:Path(value = "wmo")
    @param:Path(value = "wmo")
    @field:Element(name = "name", required = false)
    @param:Element(name = "name", required = false)
    val wmo: GmlName?,

    @Namespace(
        reference = "http://xml.fmi.fi/namespace/om/atmosphericfeatures/1.0",
        prefix = "target"
    )
    @field:Element(name = "representativePoint")
    @param:Element(name = "representativePoint")
    val representativePoint: TargetRepresentativePoint,

    @Namespace(
        reference = "http://xml.fmi.fi/namespace/om/atmosphericfeatures/1.0",
        prefix = "target"
    )
    @field:Element(name = "region")
    @param:Element(name = "region")
    val region: TargetRegion

)