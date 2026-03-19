package com.rudra.defineeasy.feature_dictionary.data.local

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.reflect.TypeToken
import com.rudra.defineeasy.feature_dictionary.data.util.JsonParser
import com.rudra.defineeasy.feature_dictionary.domain.model.Meaning


@ProvidedTypeConverter
class Converters(
    private val jsonParser: JsonParser
) {
    @TypeConverter
    fun fromMeaningsJson(json: String): List<Meaning> {
        val type = TypeToken.getParameterized(ArrayList::class.java, Meaning::class.java).type
        return jsonParser.fromJson(
            json,
            type
        ) ?: emptyList()
    }

    @TypeConverter
    fun toMeaningsJson(meanings: List<Meaning>): String {
        return jsonParser.toJson(
            meanings,
            TypeToken.getParameterized(ArrayList::class.java, Meaning::class.java).type
        ) ?: "[]"
    }
}
