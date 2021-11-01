package no.nav.siftilgangskontroll.core.pdl

import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.siftilgangskontroll.core.utils.personIdent
import no.nav.siftilgangskontroll.pdl.generated.hentident.IdentInformasjon

data class PdlAktørIdContext(
    private val pdlService: PdlService,
    val borgerToken: String
) {
    val identer: List<IdentInformasjon> = runBlocking { pdlService.aktørId(JwtToken(borgerToken).personIdent(), borgerToken) }
    val pdlPersonContext = PdlPersonContext(pdlService, borgerToken)
}

fun List<IdentInformasjon>.tilAktørId(): AktørId = AktørId(first().ident)

data class AktørId(internal val value: String)
