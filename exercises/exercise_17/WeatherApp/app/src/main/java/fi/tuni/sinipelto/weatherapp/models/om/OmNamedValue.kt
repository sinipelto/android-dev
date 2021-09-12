package fi.tuni.sinipelto.weatherapp.models.om

import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
data class OmNamedValue(

    @Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
    @field:Element(name = "name")
    @param:Element(name = "name")
    val name: OmName,

    @Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
    @field:Element(name = "value")
    @param:Element(name = "value")
    val value: String

)