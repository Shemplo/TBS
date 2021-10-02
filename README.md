# Tinkoff bonds scanner
This tools allows to scan tinkoff investment for the bonds using set of various parameters. 
It can be useful when you need to decide what to buy next because it provides also score for 
each bond and sorts appropriate variants according to it. **Be careful (disclaimer)**:
_score value is not a ground truth but we try to make it more objective as possible_

### Used data sources
* [Tinkoff investment Java SDK](https://github.com/TinkoffCreditSystems/invest-openapi-java-sdk) - to retrieve list of available bonds in Tinkoff investment
* [MOEX](https://iss.moex.com/) - to retrieve bond details (name, dates, coupons, price, ...)

### How to run
1. Get token for Tinkoff investment API how it's described in official documentation
2. Pull this repository
3. Create file `{FILE}` and write in it generated token on step 1.
4. Modify enumeration constant in `ru.shemplo.tbs.TBSProfile` for your needs 
(token filename; responsibility that defines which environment in Tinkoff will be used 
sandbox/production; commented parameters, some of them can have NULL value)
5. Choose profile in main class `ru.shemplo.tbs.RunTinkoffBondsScanner`
6. Run application and wait until JavaFX window is open (it can take some time)

### How to use
* Link `🌐` allows to open bonds page in Tinkoff investment
* Link `🔍` allows to inspect known coupons of corresponding bond
* Credit values are calculated with consideration to the inflation with the equation: 
let `S` some sum, let `D` number of a days and `I` is inflation in percents than
`s` value equals to `S / (1 + I)^(D / 365)` is sum that corresponding to `S` in `D` days
* In case of coupon value is unknown then latest known value (or 0.0) will be used with
discount of 0.9 modifier due to this value is not reliable and it's some penalty to final score
* There is no guarantee that prices that are mentioned in table are still the same when
table is shown and used for some time. This value is retrieved one when bond data is requested
from MOEX (before table is shown).