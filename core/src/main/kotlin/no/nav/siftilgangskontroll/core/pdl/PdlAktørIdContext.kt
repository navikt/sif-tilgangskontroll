package no.nav.siftilgangskontroll.core.pdl

import kotlinx.coroutines.runBlocking
import no.nav.siftilgangskontroll.core.behandling.Behandling
import no.nav.siftilgangskontroll.pdl.generated.enums.IdentGruppe
import no.nav.siftilgangskontroll.pdl.generated.hentident.IdentInformasjon

data class PdlAktørIdContext(
    private val pdlService: PdlService,
    private val ident: String,
    private val identGruppe: IdentGruppe,
    private val callId: String,
    private val behandling: Behandling,
    val borgerToken: String,
) {
    val identer: List<IdentInformasjon> = runBlocking { pdlService.hentIdent(
        ident,
        borgerToken,
        callId,
        identGruppe,
        behandling
    ) }
}

fun List<IdentInformasjon>.tilAktørId(): AktørId = AktørId(first().ident)

data class AktørId(val value: String)
