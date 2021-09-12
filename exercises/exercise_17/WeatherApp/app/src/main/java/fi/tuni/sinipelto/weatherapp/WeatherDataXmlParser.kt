package fi.tuni.sinipelto.weatherapp

import android.util.Xml
import fi.tuni.sinipelto.weatherapp.models.MeasurementTVP
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

// XML Parser implementation for processing FMI Weather API measurements
// Source: https://developer.android.com/training/basics/network-ops/xml#kotlin
class WeatherDataXmlParser {

    // Attribute gml:id value for temperature
    private val temperatureId = "obs-obs-1-1-t2m"

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): WeatherData {
        inputStream.use {
            // Setup the parser
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, Xml.Encoding.UTF_8.name)
            parser.nextTag()

            // Collect the required data from the parser
            val data = readWeatherData(parser)

            // The data is required to be available at this point
            return WeatherData(data.station!!, data.region!!, data.measurements!!)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readWeatherData(
        parser: XmlPullParser,
        data: MutableWeatherData = MutableWeatherData(null, null, mutableListOf()),
    ): MutableWeatherData {
        while (parser.next() != XmlPullParser.END_DOCUMENT) {

            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "target:Location" -> {
                    if (data.region == null) data.region =
                        readElementsFromTag(parser,
                            XmlTag(null, "target:Location"),
                            listOf("target:region")).first().second
                }

                "gml:Point" ->
                    if (data.station == null) {
                        data.station =
                            readElementsFromTag(parser,
                                XmlTag(null, "gml:Point"),
                                listOf("gml:name")).first().second
                    }

                "wml2:MeasurementTimeseries" -> {
                    if (parser.getAttributeValue(null, "gml:id") == temperatureId) {

                        while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == "wml2:MeasurementTimeseries")) {
                            val measure = readElementsFromTag(parser,
                                XmlTag(null, "wml2:MeasurementTVP"),
                                listOf("wml2:time", "wml2:value"))

                            // While getting out correct data, keep fetching
                            // Otherwise stop
                            if (measure.size == 2) {
                                data.measurements!!.add(
                                    MeasurementTVP(measure[0].second, measure[1].second)
                                )
                            } else {
                                break
                            }
                        }
                    }
                }

                // If any of the selected not found, go further
                else -> {
                    if (data.validate()) {
                        return data
                    } else {
                        readWeatherData(parser, data)
                    }
                }
            }
        }

        // Finally, return the collected data if XML gone through
        return data
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readElementsFromTag(
        parser: XmlPullParser,
        tag: XmlTag,
        elements: List<String>,
        data: MutableList<Pair<String, String>> = mutableListOf(),
    ): List<Pair<String, String>> { // Format: Key = element tag, Value = element tag value
        while (parser.next() != XmlPullParser.END_DOCUMENT) {

            if (parser.eventType == XmlPullParser.END_TAG) {
                if (parser.name == tag.name) return data
            }

            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (elements.contains(parser.name)) {
                data.add(Pair(parser.name,
                    readTagValue(parser, XmlTag(parser.namespace, parser.name))))
                return data
            } else {
                // If did not match, try to go deeper if possible
                readElementsFromTag(parser, XmlTag(parser.namespace, parser.name), elements, data)
            }
        }

        return data
    }

    // Processes tags with text content
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTagValue(parser: XmlPullParser, tag: XmlTag): String {
        parser.require(XmlPullParser.START_TAG, tag.ns, tag.name)
        val value = readText(parser)
        parser.require(XmlPullParser.END_TAG, tag.ns, tag.name)
        return value
    }

    // Processes tag attributes in the xml (read attribute value based on the attribute name).
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTagAttribute(
        parser: XmlPullParser,
        tag: XmlTag,
        attribute: XmlAttribute,
    ): String {
        parser.require(XmlPullParser.START_TAG, tag.ns, tag.name)
        val attr = parser.getAttributeValue(tag.ns, attribute.name)
        parser.require(XmlPullParser.END_TAG, tag.ns, tag.name)
        return attr
    }

    // For the tags, extracts their text values
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}

// Attribute for parsing attributes and their values in the XML stream
data class XmlAttribute(
    val ns: String?, // Possibly no namespace
    val name: String, // Always has a name
    val value: String?, // Null for attribute queries
)

// Represents an XML tag. Used for processing tags in the XML stream.
data class XmlTag(
    val ns: String?,
    val name: String,
)

// Element type to specify for an element (e.g. tag or attribute)
enum class ElementType {
    Tag,
    Attribute,
}