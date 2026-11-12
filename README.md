# SIF Tilgangskontroll

![CI / CD](https://github.com/navikt/sif-tilgangskontroll/workflows/Build/badge.svg)

# Innholdsoversikt
* [1. Kontekst](#1-kontekst)
* [2. Funksjonelle Krav](#2-funksjonelle-krav)
* [3. Begrensninger](#3-begrensninger)
* [4. Programvarearkitektur](#4-programvarearkitektur)
* [5. Kode](#5-kode)
* [6. Data](#6-data)
* [7. Infrastrukturarkitektur](#7-infrastrukturarkitektur)
* [8. Publisering](#8-publisering)
* [9. Utviklingsmiljø](#9-utviklingsmiljø)
* [10. Henvendelser](#10-henvendelser)

# 1. Kontekst
Utføre tilgangskontroll av sluttbrukere og saksbehandlere for tjenester i sykdom-i-familien (SIF).

# 2. Funksjonelle Krav
Denne tjenesten understøtter behovet for utføring av tilgangskontroll av sluttbrukere og saksbehandlere for team sif.
Tjenesten utfører tilgangskontroll basert på visse regelsett avhengig om bruker er borger eller saksbehandler.
Tilgangskontrollen publiseres som bibliotekene `core` og `spesification`. Andre tjenester bruker dem til å utføre tilgangskontroll på vegne av bruker. Repoet har ingen API-endepunkter.

# 3. Begrensninger
N/A

# 4. Programvarearkitektur
N/A

# 5. Kode
N/A

# 6. Data
N/A

# 7. Infrastrukturarkitektur
N/A

# 8. Publisering
Workflowen [Publish release](.github/workflows/publish.yml) publiserer bibliotekene `core` og `spesification` til GitHub Packages når noen publiserer en release i GitHub. Taggen på releasen blir versjonsnummeret.

Konsumentene legger til `https://maven.pkg.github.com/navikt/sif-tilgangskontroll` som Maven-repository og henter bibliotekene slik:

```kotlin
implementation("no.nav.sif.tilgangskontroll:core:<versjon>")
implementation("no.nav.sif.tilgangskontroll:spesification:<versjon>")
```

Workflowen [Build](.github/workflows/build-and-deploy.yml) kjører testene og CodeQL ved hver push. Repoet bygger ikke image og deployer ingenting.

# 9. Utviklingsmiljø
## Forutsetninger
* Java 25

## Bygge Prosjekt
For å bygge kode og kjøre testene, kjør:

```shell script
./gradlew clean build
```

# 10. Henvendelser
Still spørsmål om koden eller prosjektet til team brukerdialog på Slack:
* [\#sif-brukerdialog](https://nav-it.slack.com/archives/CQ7QKSHJR)
* [\#sif-innsynsplattform](https://nav-it.slack.com/archives/C013ZJTKUNB)
