package com.example.myapplication.data
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Root(
    @SerializedName("_embedded")
    val embedded: Embedded,
) : Serializable

data class Embedded(
    val events: List<Event>,
) : Serializable

data class Event(
    val name: String,
    val id: String,
    val sales: Sales,
    val dates: Dates,
    val priceRanges: List<PriceRange>,
    val seatmap: Seatmap,
    @SerializedName("_embedded")
    val embedded: Embedded2,
): Serializable

data class Sales(
    val public: Public,
): Serializable

data class Public(
    val startDateTime: String,
    val endDateTime: String,
): Serializable

data class Dates(
    val start: Start,
    val timezone: String,
    val status: Status,
    val spanMultipleDays: Boolean,
): Serializable

data class Start(
    val localDate: String,
    val localTime: String,
    val dateTime: String,
): Serializable

data class Status(
    val code: String,
): Serializable

data class PriceRange(
    val type: String,
    val currency: String,
    val min: String,
    val max: String,
): Serializable

data class Seatmap(
    val staticUrl: String,
): Serializable

data class Embedded2(
    val venues: List<Venue>,
): Serializable

data class Venue(
    val id: String,
    val url: String,
    val locale: String,
    val postalCode: String,
    val timezone: String,
    val city: City,
    val country: Country,
    val address: Address,
    val location: Location,
    val upcomingEvents: UpcomingEvents,
): Serializable

data class City(
    val name: String,
): Serializable

data class Country(
    val name: String,
    val countryCode: String,
): Serializable

data class Address(
    val line1: String,
): Serializable

data class Location(
    val longitude: String,
    val latitude: String,
): Serializable

data class UpcomingEvents(
    @SerializedName("mfx-se")
    val mfxSe: Long,
    @SerializedName("_total")
    val total: Long,
    @SerializedName("_filtered")
    val filtered: Long,
): Serializable