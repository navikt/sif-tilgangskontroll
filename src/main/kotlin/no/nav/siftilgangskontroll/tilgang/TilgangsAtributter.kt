package no.nav.siftilgangskontroll.tilgang

import kotlinx.coroutines.runBlocking
import no.nav.siftilgangskontroll.pdl.PdlService
import no.nav.siftilgangskontroll.pdl.generated.enums.AdressebeskyttelseGradering
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Adressebeskyttelse
import no.nav.siftilgangskontroll.pdl.generated.hentperson.Person
import no.nav.siftilgangskontroll.spesification.Policy
import no.nav.siftilgangskontroll.spesification.PolicyDecision
import no.nav.siftilgangskontroll.spesification.equalTo
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
    private val bearerToken: BearerToken,
    private val tilgangsAttributter: TilgangsAttributter
) {
    val borger = Borger(
        barnTilgangForespørsel.personIdent,
        tilgangsAttributter
    )

    val barn = runBlocking { tilgangsAttributter.pdlService.barn(listOf(barnTilgangForespørsel.barnIdent)) }
}

data class Borger(
    val personIdent: PersonIdent,
    val tilgangsAttributter: TilgangsAttributter
) {
    fun harStrengtFortroligAdresse(): Boolean = runBlocking {
        tilgangsAttributter.pdlService.person(personIdent).harStrengtFortroligAdresse()
    }
}

fun hentBarnContext(
    bearerToken: BearerToken,
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

fun PersonBarn.harStrengtFortroligAdresse(): Boolean = adressebeskyttelse
    .contains(AdressebeskyttelseBarn(AdressebeskyttelseGradering.STRENGT_FORTROLIG))

internal fun String.path(path: String) = "${this.removeSuffix("/")}/${path.removePrefix("/")}"
