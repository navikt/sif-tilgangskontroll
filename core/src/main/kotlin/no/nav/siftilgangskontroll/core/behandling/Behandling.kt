package no.nav.siftilgangskontroll.core.behandling

/**
 * Enum som representerer forskjellige behandlingstyper for å administrere ytelseskrav.
 * Denne klassen brukes for å håndtere retten til ulike ytelser ved ulike hendelser som bortfall av
 * arbeidsinntekt for yrkesaktive relatert til barns eller barnepassers sykdom, barns funksjonshemning,
 * og pleie av nærstående i livets sluttfase.
 */
enum class Behandling(val behandlingsnummer: String) {

    /**
     * Behandlingstype for saksbehandling av søknader og refusjonskrav om pleiepenger for syke barn
     * etter folketrygdloven kap. 9.
     *
     * @see <a href="https://behandlingskatalog.intern.nav.no/process/purpose/PLEIE_OMSORGS_OG_OPPLAERINGSPENGER/daa46292-de92-4924-9f60-2490b29d0c30">NAV Behandlingskatalog</a>
     */
    PLEIEPENGER_SYKT_BARN("B249"),

    /**
     * Behandlingstype for saksbehandling av søknader og refusjonskrav om pleiepenger i livets sluttfase
     * etter folketrygdloven kap. 9.
     *
     * @see <a href="https://behandlingskatalog.intern.nav.no/process/purpose/PLEIE_OMSORGS_OG_OPPLAERINGSPENGER/e2630d73-2013-4654-bc6e-e08281b1167e">NAV Behandlingskatalog</a>
     */
    PLEIEPENGER_I_LIVETS_SLUTTFASE("B566"),

    /**
     * Behandlingstype for saksbehandling av personopplysninger relatert til omsorgspenger fra bruker.
     *
     * @see <a href="https://behandlingskatalog.intern.nav.no/process/purpose/PLEIE_OMSORGS_OG_OPPLAERINGSPENGER/d0f205b9-01cc-437d-9528-9aaf65931ba0">NAV Behandlingskatalog</a>
     */
    OMSORGSPENGERUTBETALING("B135"),

    /**
     * Behandlingstype for saksbehandling av søknader om flere omsorgsdager og meldinger om deling av omsorgsdager.
     *
     * @see <a href="https://behandlingskatalog.intern.nav.no/process/purpose/PLEIE_OMSORGS_OG_OPPLAERINGSPENGER/4a1c9324-9c5e-4ddb-ac7f-c55d1dcd9736">NAV Behandlingskatalog</a>
     */
    OMSORGSPENGER_RAMMEMELDING("B142"),

    /**
     * Behandlingstype for saksbehandling av søknader om opplæringspenger.
     *
     * @see <a href="https://behandlingskatalog.ansatt.nav.no/process/purpose/PLEIE_OMSORGS_OG_OPPLAERINGSPENGER/5c802209-fb14-4bb1-806f-869c1656de03">NAV Behandlingskatalog</a>
     */
    OPPLÆRINGSPENGER("B915"),

    /**
     * Behandlingstype for å kunne motta og behandle søknad og revurdere ungdomsprogramytelsen.
     *
     * @see <a href="https://behandlingskatalog.intern.nav.no/process/purpose/ARBEIDSMARKEDSTILTAK/754a8897-8108-4cfc-8201-52d81dfa5169">NAV Behandlingskatalog</a>
     */
    UNGDOMSYTELSEN("B950"),
}
