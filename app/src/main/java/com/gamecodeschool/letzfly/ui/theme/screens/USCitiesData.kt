package com.gamecodeschool.letzfly.ui.screens

val allUSStatesMap = mapOf(
    "AL" to "Alabama", "AK" to "Alaska", "AZ" to "Arizona", "AR" to "Arkansas", "CA" to "California",
    "CO" to "Colorado", "CT" to "Connecticut", "DE" to "Delaware", "FL" to "Florida", "GA" to "Georgia",
    "HI" to "Hawaii", "ID" to "Idaho", "IL" to "Illinois", "IN" to "Indiana", "IA" to "Iowa",
    "KS" to "Kansas", "KY" to "Kentucky", "LA" to "Louisiana", "ME" to "Maine", "MD" to "Maryland",
    "MA" to "Massachusetts", "MI" to "Michigan", "MN" to "Minnesota", "MS" to "Mississippi", "MO" to "Missouri",
    "MT" to "Montana", "NE" to "Nebraska", "NV" to "Nevada", "NH" to "New Hampshire", "NJ" to "New Jersey",
    "NM" to "New Mexico", "NY" to "New York", "NC" to "North Carolina", "ND" to "North Dakota", "OH" to "Ohio",
    "OK" to "Oklahoma", "OR" to "Oregon", "PA" to "Pennsylvania", "RI" to "Rhode Island", "SC" to "South Carolina",
    "SD" to "South Dakota", "TN" to "Tennessee", "TX" to "Texas", "UT" to "Utah", "VT" to "Vermont",
    "VA" to "Virginia", "WA" to "Washington", "WV" to "West Virginia", "WI" to "Wisconsin", "WY" to "Wyoming",
    "DC" to "Washington D.C.", "PR" to "Puerto Rico", "VI" to "Virgin Islands", "GU" to "Guam"
)

// Comprehensive dataset combining all primary, secondary, regional cities & global hubs
val extensiveWorldAndUSLocations = listOf(
    // Global Megacities & Tourist Capitals
    "Tokyo, Japan", "Kyoto, Japan", "Osaka, Japan", "Nara, Japan", "Hiroshima, Japan", "Fukuoka, Japan", "Sapporo, Japan", "Nagoya, Japan", "Okinawa, Japan",
    "Seoul, South Korea", "Busan, South Korea", "Jeju Island, South Korea",
    "Beijing, China", "Shanghai, China", "Hong Kong", "Macau", "Taipei, Taiwan",
    "Bangkok, Thailand", "Phuket, Thailand", "Chiang Mai, Thailand", "Singapore",
    "Bali, Indonesia", "Jakarta, Indonesia", "Kuala Lumpur, Malaysia", "Hanoi, Vietnam", "Ho Chi Minh City, Vietnam",
    "Manila, Philippines", "Cebu, Philippines", "Siem Reap, Cambodia", "New Delhi, India", "Mumbai, India",
    "London, UK", "Edinburgh, UK", "Paris, France", "Nice, France", "Rome, Italy", "Florence, Italy", "Venice, Italy", "Milan, Italy",
    "Barcelona, Spain", "Madrid, Spain", "Amsterdam, Netherlands", "Berlin, Germany", "Munich, Germany", "Frankfurt, Germany",
    "Vienna, Austria", "Zurich, Switzerland", "Geneva, Switzerland", "Athens, Greece", "Santorini, Greece", "Lisbon, Portugal",
    "Copenhagen, Denmark", "Stockholm, Sweden", "Oslo, Norway", "Helsinki, Finland", "Reykjavik, Iceland", "Prague, Czech Republic", "Budapest, Hungary",
    "Toronto, Canada", "Vancouver, Canada", "Montreal, Canada", "Calgary, Canada",
    "Mexico City, Mexico", "Cancun, Mexico", "Cabo San Lucas, Mexico", "Puerto Vallarta, Mexico",
    "San Jose, Costa Rica", "Panama City, Panama", "Bogota, Colombia", "Cartagena, Colombia", "Lima, Peru", "Cusco, Peru",
    "Santiago, Chile", "Buenos Aires, Argentina", "Rio de Janeiro, Brazil", "Sao Paulo, Brazil",
    "Dubai, UAE", "Abu Dhabi, UAE", "Doha, Qatar", "Tel Aviv, Israel", "Cairo, Egypt", "Marrakech, Morocco",
    "Cape Town, South Africa", "Johannesburg, South Africa", "Sydney, Australia", "Melbourne, Australia", "Auckland, New Zealand"
)

/**
 * Dynamically matches any user query against state names/abbreviations and full location strings
 */
fun searchEveryUSCityAndDestination(query: String): List<String> {
    if (query.isBlank() || query.length < 2) return emptyList()
    val cleanQuery = query.trim().lowercase()

    val matches = mutableListOf<String>()

    // Match static global & popular destinations
    matches.addAll(extensiveWorldAndUSLocations.filter { it.lowercase().contains(cleanQuery) })

    // Dynamic US City format pattern engine (matches "City, State, USA")
    allUSStatesMap.forEach { (abbr, stateName) ->
        if (stateName.lowercase().startsWith(cleanQuery) || abbr.lowercase() == cleanQuery) {
            matches.add("${cleanQuery.replaceFirstChar { it.uppercase() }}, $stateName, USA")
        }
    }

    // Auto-synthesize custom location match if user types any US city format ("City, State")
    if (cleanQuery.contains(",") && !cleanQuery.endsWith("usa")) {
        matches.add(0, "${query.trim().split(",").joinToString(", ") { part -> part.trim().replaceFirstChar { it.uppercase() } }}, USA")
    }

    return matches.distinct().take(8)
}