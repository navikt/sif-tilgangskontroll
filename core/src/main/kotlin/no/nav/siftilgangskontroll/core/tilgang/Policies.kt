package no.nav.siftilgangskontroll.core.tilgang

import no.nav.siftilgangskontroll.core.pdl.*
import no.nav.siftilgangskontroll.core.pdl.BarnContext
import no.nav.siftilgangskontroll.policy.spesification.Policy
import no.nav.siftilgangskontroll.policy.spesification.Policy.Companion.policy
import no.nav.siftilgangskontroll.policy.spesification.PolicyEvaluation.Companion.deny
import no.nav.siftilgangskontroll.policy.spesification.PolicyEvaluation.Companion.permit
import org.slf4j.LoggerFactory

/**
 * @see <a href="https://confluence.adeo.no/pages/viewpage.action?spaceKey=SIK&title=Forretningspolicyer">Forretningspolicyer for tilgangskontroll</a>
 */
object Policies {

    private val logger = LoggerFactory.getLogger(Policies::class.java)

    internal fun `Barn er ikke adressebeskyttet`(barnIdent: BarnIdent): Policy<BarnContext> =
        policy {
            id = "SIF.1"
            description = "NAV-Bruker skal ikke ha tilgang til barn med adressebeskyttelse"
            evaluation = {
                val harAdresseSkjerming: Boolean = pdlBarn.harAdresseSkjerming(barnIdent)

                when {
                   harAdresseSkjerming -> deny("NAV-bruker har ikke tilgang til barn med adressebeskyttelse")
                    else -> permit("NAV-bruker har tilgang til barn")
                }
            }
        }


    internal fun `Barn er i live`(ident: BarnIdent): Policy<BarnContext> =
        policy {
            id = "SIF.2"
            description = "Tilgang skal nektes til barn som ikke er i live."
            evaluation = {
                when (pdlBarn.erDød(ident)) {
                    true -> deny("Barn er ikke lenger i live")
                    else -> permit("Barn er i live")
                }
            }
        }

    // TODO: 23/09/2021 Mulig med fullmakt kanskje?
    internal fun `NAV-bruker har tilgang barn`(barnIdent: BarnIdent): Policy<BarnContext> =
        policy {
            id = "SIF.3"
            description = "NAV-bruker skal ikke ha tilgang til ukjent relasjon"
            evaluation = {

                val erKjentRelasjon = pdlPersonContext.relasjoner().map { it.relatertPersonsIdent }.contains(barnIdent)

                when {
                    erKjentRelasjon -> permit("Relasjon er kjent")
                    else -> deny("NAV-bruker har ikke tilgang til ukjent relasjon")
                }
            }
        }

    internal fun `Barn er under myndighetsalder`(ident: BarnIdent): Policy<BarnContext> =
        policy {
            id = "SIF.4"
            description = "Tilgang skal nektes til barn som er over 18 år."
            evaluation = {
                when (pdlBarn.erMyndig(ident)) {
                    true -> deny("Barn er over myndighetsalder")
                    else -> permit("Barn er under myndighetsalder ")
                }
            }
        }

    internal fun `NAV-bruker er i live`(): Policy<PdlPersonContext> =
        policy {
            id = "FP.10"
            description = "Tilgang til selvbetjening skal nektes til NAV-brukere som ikke er i live."
            evaluation = {
                when (erDød()) {
                    true -> deny("NAV-bruker er ikke lenger i live")
                    else -> permit("NAV-bruker er i live")
                }
            }
        }

    internal fun `NAV-bruker er myndig`(): Policy<PdlPersonContext> =
        policy {
            id = "FP.11"
            description = "Tilgang til selvbetjening skal nektes til NAV-brukere som er mindreårig (under $MYNDIG_ALDER år)."
            evaluation = {
                when (erMyndig()) {
                    true ->  permit("NAV-bruker er myndig")
                    else -> deny("NAV-bruker er ikke myndig")
                }
            }
        }
}
