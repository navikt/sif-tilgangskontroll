package no.nav.siftilgangskontroll.core.pdl.utils

import no.nav.siftilgangskontroll.pdl.generated.hentbarn.HentPersonBolkResult
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person

enum class PdlOperasjon(val navn: String) {
    HENT_PERSON("hentPerson"),
    HENT_PERSON_BOLK("hentPersonBolk")
}

fun pdlHentPersonResponse(
    person: Person
): String =
    //language=json
    """
    {
        "data": {
            "hentPerson": ${person.somJsonObject()}
        }
    }
    """.trimIndent()

fun pdlHentPersonBolkResponse(
    personBolk: List<HentPersonBolkResult>,
): String =
    //language=json
    """
    {
        "data": {
            "hentPersonBolk": ${personBolk.somJsonArray()}
        }
    }
    """.trimIndent()
