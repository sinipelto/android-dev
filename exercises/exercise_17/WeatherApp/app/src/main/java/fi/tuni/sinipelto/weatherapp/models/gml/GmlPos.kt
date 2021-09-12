package fi.tuni.sinipelto.weatherapp.models.gml

import org.simpleframework.xml.Namespace

@Namespace(reference = "http://www.opengis.net/gml/3.2", prefix = "gml")
data class GmlPos(

    val value: String

)