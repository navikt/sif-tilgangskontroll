package no.nav.siftilgangskontroll.core.pdl

import kotlinx.coroutines.runBlocking
import no.nav.siftilgangskontroll.core.behandling.Behandling

data class PdlBarnOppslagContext(
    private val pdlService: PdlService,
    private val behandling: Behandling,
    val barnIdenter: List<String>,
    val systemToken: String,
    val callId: String,
) {
    val barn = runBlocking { pdlService.barn(barnIdenter, systemToken, callId, behandling) }
}
