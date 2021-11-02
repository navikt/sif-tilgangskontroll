package no.nav.siftilgangskontroll.core.pdl

import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.core.utils.personIdent
import no.nav.siftilgangskontroll.pdl.generated.hentident.IdentInformasjon

data class PdlAktørIdContext(
    private val pdlService: PdlService,
    private val ident: String,
    private val callId: String,
    val borgerToken: String
) {
    val identer: List<IdentInformasjon> = runBlocking { pdlService.aktørId(ident, borgerToken, callId) }
}

fun List<IdentInformasjon>.tilAktørId(): AktørId = AktørId(first().ident)

data class AktørId(val value: String)
