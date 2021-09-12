package fi.tuni.sinipelto.weatherapp.models.om

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root

@Namespace(reference = "http://www.opengis.net/om/2.0", prefix = "om")
@Root(name = "procedure")
data class OmProcedure(

    @Namespace(reference = "http://www.w3.org/1999/xlink", prefix = "xlink")
    @field:Attribute(name = "href")
    @param:Attribute(name = "href")
    val href: String

)
