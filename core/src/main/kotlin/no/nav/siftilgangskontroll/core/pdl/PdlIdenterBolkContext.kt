package no.nav.siftilgangskontroll.core.pdl

import kotlinx.coroutines.runBlocking
import no.nav.siftilgangskontroll.pdl.generated.enums.IdentGruppe
import no.nav.siftilgangskontroll.pdl.generated.hentidenterbolk.HentIdenterBolkResult

data class PdlIdenterBolkContext(
    private val pdlService: PdlService,
    private val identer: List<String>,
    private val identGrupper: List<IdentGruppe>,
    private val callId: String,
    val systemToken: String
) {
    val identerBolkResults: List<HentIdenterBolkResult> = runBlocking { pdlService.hentIdenter(
        identer = identer,
        identGrupper = identGrupper,
        systemToken = systemToken,
        callId = callId
    ) }
}
