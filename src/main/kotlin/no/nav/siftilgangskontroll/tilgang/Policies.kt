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

    val `borger har tilgang til barn med strengt fortrolig adresse`: Policy<HentBarnContext> =
        policy {
            id = "FP.9"
            description = "NAV Bruker med spes.reg.kode 6 skal ikke kunne se sin geografiske adresse (alle typer)."
            evaluation = {
                val borgerHarStrengtFortroligAdresse = borger.harStrengtFortroligAdresse()
                val barnHarStrengtFortroligAdresse: Boolean = barn.harStrengtFortroligAdresse()
                logger.info("borgerHarStrengtFortroligAdresse: {}", borgerHarStrengtFortroligAdresse)
                logger.info("barnHarStrengtFortroligAdresse: {}", barnHarStrengtFortroligAdresse)

                when {
                    !borgerHarStrengtFortroligAdresse && barnHarStrengtFortroligAdresse -> deny("Borger har ikke tilgang til skjermet barn")
                    else -> permit("Borger har tilgang til barn")
                }
            }
        }

    val `NAV-bruker i live`: Policy<HentPersonContext> =
        policy {
            id = "FP.10"
            description = "Tilgang til selvbetjening skal nektes til NAV-brukere som ikke er i live."
            evaluation = {
                when(borger.erDød()) {
                    true -> deny("Borger er ikke lenger i live")
                    else -> permit("Borger er i live")
                }
            }
        }

    val `Barn er i live`: Policy<HentBarnContext> =
        policy {
            id = "FP.10"
            description = "Tilgang skal nektes til barn som ikke er i live."
            evaluation = {
                when(barn.erDød()) {
                    true -> deny("Barn er ikke lenger i live")
                    else -> permit("Barn er i live")
                }
            }
        }
}
