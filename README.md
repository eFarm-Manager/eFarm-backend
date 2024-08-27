# eFarm-backend

## Dokumentacja Backend

## Dokumentacja CI

### Linting, Vulnerability Scanning and Building

CI bedzie sie triggerowac gdy bedzie pull request(merge request) do `dev` z branch `feature/*` lub `bugfix/` 

Jest tam Spotbugs, owasp dependency checker, codeQL oraz secret-scanning.
Po wejsciu w actions i wybraniu konkretnego runu mozna pobrac artefakty:

- owasp-dependency-check-report
- spotbugs-report

Wiec jak beda jakies problemy to tam powinno byc co niepasowalo.

Jest mozliwosc pojawienia sie **false positives**  to wtedy pisac to sie ogarnie.
Jezeli ktore kolwiek z tych narzedzi bedzie bardziej przeszkadzalo niz pomagalo bedzie mozna probowac zmieniac lub wyrzucic ale powinno byc ok.
