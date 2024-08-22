# eFarm-backend

## Dokumentacja Backend

## Dokumentacja CI

### Linting, Vulnerability Scanning and Building

CI bedzie sie triggerowac gdy bedzie pull request(merge request) do 'dev' z branch 'feature/*'

Jest tam Spotbugs(szuka bugow) oraz owasp dependency checker(sprawdza dependencies).
Po wejsciu w actions i wybraniu konkretnego runu mozna pobrac artefakty:

- owasp-dependency-check-report
- spotbugs-report

Wiec jak beda jakies problemy to tam powinno byc co niepasowalo.
Jest mozliwosc pojawienia sie **false positives** i dla spotbugs i owasp ale wtedy mozna po prostu dodac suppresion file ale to na spokojnie.
Jezeli ktore kolwiek z tych narzedzi bedzie bardziej przeszkadzalo niz pomagalo bedzie mozna probowac zmieniac ale powinny byc ok.
