# SIF Tilgangskontroll

![CI / CD](https://github.com/navikt/sif-tilgangskontroll/workflows/CI%20/%20CD/badge.svg)
![Alerts](https://github.com/navikt/sif-tilgangskontroll/workflows/Alerts/badge.svg)
![Vulnerabilities scanning of dependencies](https://github.com/navikt/sif-tilgangskontroll/workflows/Vulnerabilities%20scanning%20of%20dependencies/badge.svg)

# Innholdsoversikt
* [1. Kontekst](#1-kontekst)
* [2. Funksjonelle Krav](#2-funksjonelle-krav)
* [3. Begrensninger](#3-begrensninger)
* [4. Programvarearkitektur](#5-programvarearkitektur)
* [5. Kode](#6-kode)
* [6. Data](#7-data)
* [7. Infrastrukturarkitektur](#8-infrastrukturarkitektur)
* [8. Distribusjon av tjenesten (deployment)](#9-distribusjon-av-tjenesten-deployment)
* [9. Utviklingsmiljø](#10-utviklingsmilj)
* [10. Drift og støtte](#11-drift-og-sttte)

# 1. Kontekst
Utføre tilgangskontroll av sluttbrukere og saksbehandlere for tjenester i sykdom-i-familien (SIF).

# 2. Funksjonelle Krav
Denne tjenesten understøtter behovet for utføring av tilgangskontroll av sluttbrukere og saksbehandlere for team sif.
Tjenesten utfører tilgangskontroll basert på visse regelsett avhengig om bruker er borger eller saksbehandler.
Tjenesten eksponerer api for at andre tjenester kan utføre tilgangskontroll på vegne av bruker.

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

# 8. Distribusjon av tjenesten (deployment)
Distribusjon av tjenesten er gjort med bruk av Github Actions.
[SIF Tilgangskontroll API CI / CD](https://github.com/navikt/sif-tilgangskontroll/actions)

Push/merge til master branche vil teste, bygge og deploye til produksjonsmiljø og testmiljø.

# 9. Utviklingsmiljø
## Forutsetninger
* docker
* Java 11
* Kubectl

## Bygge Prosjekt
For å bygge kode, kjør:

```shell script
./gradlew clean build
```

## Kjøre Prosjekt
For å kjøre kode, kjør:

```shell script
./gradlew bootRun
```

### Henting av data via api-endepunktene
TODO: Forklar henting av gyldig token for bruk av apiet.

# 10. Drift og støtte
## Logging
Loggene til tjenesten kan leses på to måter:

### Kibana
TODO: Legg til lenker for logg i kibana.

### Kubectl
For dev-gcp:
```shell script
kubectl config use-context dev-gcp
kubectl get pods -n dusseldorf | grep sif-tilgangskontroll
kubectl logs -f sif-tilgangskontroll-<POD-ID> --namespace dusseldorf -c sif-tilgangskontroll
```

For prod-gcp:
```shell script
kubectl config use-context prod-gcp
kubectl get pods -n dusseldorf | grep sif-tilgangskontroll
kubectl logs -f sif-tilgangskontroll-<POD-ID> --namespace dusseldorf -c sif-tilgangskontroll
```

## Alarmer
Vi bruker [nais-alerts](https://doc.nais.io/observability/alerts) for å sette opp alarmer. Disse finner man konfigurert i [nais/alerterator-prod.yml](nais/alerterator-prod.yml).

## Metrics
N/A

## Henvendelser
Spørsmål koden eller prosjekttet kan rettes til team brukerdialog på:
* [\#sif-brukerdialog](https://nav-it.slack.com/archives/CQ7QKSHJR)
* [\#sif-innsynsplattform](https://nav-it.slack.com/archives/C013ZJTKUNB)


