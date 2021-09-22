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

    val `borgers tilgang til barn med strengt fortrolig adresse`: Policy<HentBarnContext> =
        policy {
            id = "FP.9"
            description = "NAV Bruker med spes.reg.kode 6 skal ikke kunne se sin geografiske adresse (alle typer)."
            evaluation = {
                val borgerHarStrengtFortroligAdresse = borger.harStrengtFortroligAdresse()
                val barnHarStrengtFortroligAdresse: Boolean = barn.any { it.person!!.harStrengtFortroligAdresse() }
                logger.info("borgerHarStrengtFortroligAdresse: {}", borgerHarStrengtFortroligAdresse)
                logger.info("barnHarStrengtFortroligAdresse: {}", barnHarStrengtFortroligAdresse)

                when {
                    !borgerHarStrengtFortroligAdresse && barnHarStrengtFortroligAdresse -> deny("Borger har ikke tilgang til skjermet barn")
                    else -> permit("Borger har tilgang til barn")
                }
            }
        }
}
