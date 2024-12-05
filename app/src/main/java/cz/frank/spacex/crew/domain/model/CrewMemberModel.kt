package cz.frank.spacex.crew.domain.model

data class CrewMemberModel(val name: String, val status: Status, val image: String, val link: String) {
    enum class Status {
        ACTIVE, INACTIVE, RETIRED, UNKNOWN
    }
}
