package no.nav.siftilgangskontroll.tilgang

import no.nav.siftilgangskontroll.spesification.Policy
import no.nav.siftilgangskontroll.spesification.Policy.Companion.policy
import no.nav.siftilgangskontroll.spesification.PolicyEvaluation.Companion.deny
import no.nav.siftilgangskontroll.spesification.PolicyEvaluation.Companion.permit
import org.slf4j.LoggerFactory

/**
 * @see <a href="https://confluence.adeo.no/pages/viewpage.action?spaceKey=SIK&title=Forretningspolicyer">Forretningspolicyer for tilgangskontroll</a>
 */
object Policies {

    private val logger = LoggerFactory.getLogger(Policies::class.java)

    fun `NAV-bruker uten adressebeskyttelse skal ikke ha tilgang til barn med adressebeskyttelse`(barnIdent: BarnIdent): Policy<HentBarnContext> =
        policy {
            id = "SIF.1"
            description = "NAV-Bruker uten adressebeskyttelse skal ikke ha tilgang til barn med adressebeskyttelse"
            evaluation = {
                val borgerHarStrengtFortroligAdresse = borger.harStrengtFortroligAdresse()
                val barnHarStrengtFortroligAdresse: Boolean = barn.harStrengtFortroligAdresse(barnIdent)

                when {
                    !borgerHarStrengtFortroligAdresse && barnHarStrengtFortroligAdresse -> deny("NAV-bruker har ikke tilgang til barn med adressebeskyttelse")
                    else -> permit("NAV-bruker har tilgang til barn")
                }
            }
        }


    fun `Barn er i live`(ident: BarnIdent): Policy<HentBarnContext> =
        policy {
            id = "SIF.2"
            description = "Tilgang skal nektes til barn som ikke er i live."
            evaluation = {
                when (barn.erDød(ident)) {
                    true -> deny("Barn er ikke lenger i live")
                    else -> permit("Barn er i live")
                }
            }
        }

    // TODO: 23/09/2021 Mulig med fullmakt kanskje?
    fun `NAV-bruker skal ikke ha tilgang til ukjent relasjon`(barnIdent: BarnIdent): Policy<HentBarnContext> =
        policy {
            id = "SIF.3"
            description = "NAV-bruker skal ikke ha tilgang til ukjent relasjon"
            evaluation = {

                val erKjentRelasjon = borger.relasjoner().map { it.relatertPersonsIdent }.contains(barnIdent)

                when {
                    erKjentRelasjon -> permit("Relasjon er kjent")
                    else -> deny("NAV-bruker har ikke tilgang til ukjent relasjon")
                }
            }
        }

    fun `NAV-bruker er i live`(): Policy<HentPersonContext> =
        policy {
            id = "FP.10"
            description = "Tilgang til selvbetjening skal nektes til NAV-brukere som ikke er i live."
            evaluation = {
                when (borger.erDød()) {
                    true -> deny("NAV-bruker er ikke lenger i live")
                    else -> permit("NAV-bruker er i live")
                }
            }
        }

    fun `NAV-bruker under myndighetsalder`(): Policy<HentPersonContext> =
        policy {
            id = "FP.11"
            description = "Tilgang til selvbetjening skal nektes til NAV-brukere som er mindreårig (under $MYNDIG_ALDER år)."
            evaluation = {
                when (borger.erMyndig()) {
                    true ->  permit("NAV-bruker er myndig")
                    else -> deny("NAV-bruker er ikke myndig")
                }
            }
        }
}
