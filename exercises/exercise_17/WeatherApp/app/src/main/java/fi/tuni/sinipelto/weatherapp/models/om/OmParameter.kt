package fi.tuni.sinipelto.weatherapp.models.om

import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace

@Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
data class OmParameter(

    @Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
    @field:Element(name = "NamedValue")
    @param:Element(name = "NamedValue")
    val namedValue: OmNamedValue

)