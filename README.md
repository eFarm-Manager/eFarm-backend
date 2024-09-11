# eFarm-backend

## Dokumentacja Backend

## Dokumentacja Testow

### Unit Tests

Zeby przetestowac unit testy mozna to zrobic za pomoca komendy:
```sh
mvn clean test -Punit-test
```

#### Controller

##### AuthController

- testowanie czy user moze sie poprawnie zalogowac
- jezeli haslo jest zle przy logowaniu to BadCredentialsException
- poprawnie wyczyszcza token przy signout

#### Model/User/ERole

- testowanie czy tylko 2 role sa w enumie

#### Payload/response/UserInfoResponse

- testowanie setRoles(null)
- testowanie roles.add()
- testowanie setRoles(roles)

#### Repository

##### farm/ActivationCodeRepository

- findByCode zwraca istniejacy ActivationCode
- ActivationCode jest Optional of null dla nieistniejacego kodu

##### farm/FarmRepository

- zwraca true jak farma istnieje ByName
- zwraca false jak farma nie instnieje ByName

##### farm/AddressRepository

-

##### user/RoleRepository
- poprawnie zwraca manager
- poprawnie zwraca operator
- IllegalArgumentException dla nieistniejacej roli
- dla nulla zwraca optional of null

##### user/UserRepository
- poprawnie zwraca uzytkownika po username
- zwraca optional of null dla nieistniejacego uzytkownika
- zwraca true jak farma istnieje ByName
- zwraca false jak farma nie instnieje ByName

#### Security

##### jwt/AuthEntryPointJwt
- wysyla SC_UNAUTHORIZED na bledzie autoryzacji

##### jwt/AuthTokenFilterSpec
- autentykuje usera jezeli valid jwt
- nie autentykuje uzera jezeli invalid jwt

##### jwt/JwtUtils
- zwraca jwt jezeli cookie istnieje - getJwtFromCookie
- zwraca null jezeli cookie nie istnieje - getJwtFromCookie
- jezeli request jest null to wtedy IllegalArgumentException - getJwtFromCookie
- sprawdza poprawnosc tokenu wytworzonego - generateJwtCookie
- jezeli brak user details to NullPointerException - generateJwtCookie
- cookies roznia sie - generateJwtCookie
- zwraca czysty token - getCleanJwtCookie
- zwraca uzytkownika z valid token - getUserNameFromJwtToken
- jezeli token expired to ExpiredJwtException - getUserNameFromJwtToken
- jezeli token malformed to MalformedJwtException - getUserNameFromJwtToken
- jezeli token empty to IllegalArgumentException - getUserNameFromJwtToken
- poprawnie waliduje poprawny token - validateJwtToken
- zwraca false dla expired token - validateJwtToken
- zwraca false dla malformed token - validateJwtToken
- zwraca false dla unsupported token - validateJwtToken
- zwraca false dla empty token - validateJwtToken

##### services/UserDetailsImpl
- poprawnie tworzy user details przez konstruktor
- poprawnie tworzy user details przez build
- zwraca true jezeli porownanie wystapi pomiedzy tym samym user details
- zwraca false jezeli porownanie wystapi pomiedzy 2 roznymi user details 
- zwraca false jezeli porownanie wystapi pomiedzy user details a nullem
- zwraca false jezeli porownanie wystapi pomiedzy user details a Object z polem id

##### services/UserDetailsServiceImpl
- poprawnie znajduje uzytkownika po username
- UsernameNotFoundException jezeli nie istnieje

#### Service

##### AuthServiceSpec
- testowany signup robiony przez managera(rozne role dla nowego uzytkownika, czy login dla nowego uzytkownika jest zajety, czy manager ma przypisane Gospodarstwo, poprawny signup)
- jezeli manager robiacy konto nie istnieje RuntimeException
- testowany signup dla nowego gospodarstwa(czy login zajety, czy nazwa farmy zajeta, poprawny signupFarm)
- testowanie roznych przypadkow z kodem aktywacyjnym(kod nie istnieje, expired, uzyty, poprawny signupFarm)