package kr.co.houmuch.batch.config.convert

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException

class StringTrimDeserializer : JsonDeserializer<String>() {
    @Throws(IOException::class, JacksonException::class)
    override fun deserialize(p: JsonParser, ext: DeserializationContext?): String {
        return p.valueAsString.trim();
    }
}
