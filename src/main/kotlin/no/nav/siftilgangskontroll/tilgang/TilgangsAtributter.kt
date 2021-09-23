package no.nav.siftilgangskontroll.tilgang

import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.pdl.PdlService
import no.nav.siftilgangskontroll.pdl.generated.ID
import no.nav.siftilgangskontroll.pdl.generated.enums.AdressebeskyttelseGradering
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Adressebeskyttelse
import no.nav.siftilgangskontroll.pdl.generated.hentperson.ForelderBarnRelasjon
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person
import no.nav.siftilgangskontroll.spesification.Policy
import no.nav.siftilgangskontroll.spesification.PolicyDecision
import no.nav.siftilgangskontroll.spesification.equalTo
import no.nav.siftilgangskontroll.util.personIdent
import org.slf4j.Logger
import org.springframework.stereotype.Component
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Adressebeskyttelse as AdressebeskyttelseBarn
import no.nav.siftilgangskontroll.pdl.generated.hentbarn.Person as PersonBarn

typealias PersonIdent = String
typealias BarnIdent = String
typealias BearerToken = String

@Component
data class TilgangsAttributter(
    val pdlService: PdlService
)

data class HentBarnContext(
    val barnTilgangForespørsel: BarnTilgangForespørsel,
    private val bearerToken: JwtToken,
    private val tilgangsAttributter: TilgangsAttributter
) {
    val borger = Borger(
        personIdent = bearerToken.personIdent(),
        tilgangsAttributter = tilgangsAttributter
    )

    val barn = Barn(
        barnIdent = listOf(barnTilgangForespørsel.barnIdent),
        tilgangsAttributter = tilgangsAttributter
    )
}

data class HentPersonContext(
    private val bearerToken: JwtToken,
    private val tilgangsAttributter: TilgangsAttributter
) {
    val borger = Borger(
        personIdent = bearerToken.personIdent(),
        tilgangsAttributter = tilgangsAttributter
    )
}

data class Borger(
    val personIdent: PersonIdent,
    val tilgangsAttributter: TilgangsAttributter
) {
    val person = runBlocking { tilgangsAttributter.pdlService.person(personIdent) }

    fun harStrengtFortroligAdresse(): Boolean = person.harStrengtFortroligAdresse()
    fun erDød(): Boolean = person.erDød()
    fun relasjoner(): List<ForelderBarnRelasjon> = person.forelderBarnRelasjon
}

data class Barn(
    val barnIdent: List<ID>,
    val tilgangsAttributter: TilgangsAttributter
) {
    val barn = runBlocking { tilgangsAttributter.pdlService.barn(barnIdent) }

    fun harStrengtFortroligAdresse(): Boolean = barn.any { it.person!!.harStrengtFortroligAdresse() }
    fun erDød() = barn.any { it.person!!.erDød() }
}

fun hentPersonContext(
    bearerToken: JwtToken,
    tilgangsAttributter: TilgangsAttributter
) = HentPersonContext(
    bearerToken,
    tilgangsAttributter
)

fun hentBarnContext(
    bearerToken: JwtToken,
    barnTilgangForespørsel: BarnTilgangForespørsel,
    tilgangsAttributter: TilgangsAttributter
) =
    HentBarnContext(
        barnTilgangForespørsel,
        bearerToken,
        tilgangsAttributter
    )

fun <T> List<T>.filterBy(policy: Policy<T>, decision: PolicyDecision, logger: Logger): List<T> = filter {
    val evaluation = policy.evaluate(it)
    when {
        evaluation equalTo decision -> true
        else -> {
            logger.debug("filtered entry, reason: ${evaluation.reason}")
            false
        }
    }
}

fun Person.harStrengtFortroligAdresse(): Boolean = adressebeskyttelse
    .contains(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG))

fun Person.erDød(): Boolean = doedsfall.isNotEmpty()

fun PersonBarn.harStrengtFortroligAdresse(): Boolean = adressebeskyttelse
    .contains(AdressebeskyttelseBarn(AdressebeskyttelseGradering.STRENGT_FORTROLIG))

fun PersonBarn.erDød(): Boolean = doedsfall.isNotEmpty()

internal fun String.path(path: String) = "${this.removeSuffix("/")}/${path.removePrefix("/")}"
