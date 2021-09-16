package no.nav.siftilgangskontroll.tilgang

import no.nav.siftilgangskontroll.spesification.Policy
import no.nav.siftilgangskontroll.spesification.Policy.Companion.policy
import no.nav.siftilgangskontroll.spesification.PolicyEvaluation.Companion.deny
import no.nav.siftilgangskontroll.spesification.PolicyEvaluation.Companion.permit

/**
 * @see <a href="https://confluence.adeo.no/pages/viewpage.action?spaceKey=SIK&title=Forretningspolicyer">Forretningspolicyer for tilgangskontroll</a>
 */
object Policies {

    val `borgers tilgang til barn med strengt fortrolig adresse`: Policy<HentBarnContext> =
        policy {
            id = "FP.9"
            description = "NAV Bruker med spes.reg.kode 6 skal ikke kunne se sin geografiske adresse (alle typer)."
            evaluation = {
                when {
                    borger.harStrengtFortroligAdresse() -> permit("Borger har tilgang til barn med strengt fortrolig adresse.")
                    else -> {
                        when (val harBarnMedStrengtFortroligAdresse: Boolean =
                            this.barn.any { !it.person!!.harStrengtFortroligAdresse() }) {
                            harBarnMedStrengtFortroligAdresse -> {
                                deny("Borger har ikke tilgang til barn med strengt fortrolig adresse.")
                            }
                            else -> {
                                permit("Borger har tilgang til barn med strengt fortrolig adresse.")
                            }
                        }
                    }
                }
            }
        }
}
