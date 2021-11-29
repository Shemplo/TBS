# Tinkoff bonds scanner

This tools allows to scan tinkoff investment for the bonds using set of various parameters. 
It can be useful when you need to decide what to buy next because it provides also score for 
each bond and sorts appropriate variants according to it. **Be careful (disclaimer)**:
_score value is not a ground truth but we try to make it as more objective as possible_

### Used data sources
* [Tinkoff investment Java SDK](https://github.com/TinkoffCreditSystems/invest-openapi-java-sdk) - to retrieve list of available bonds in Tinkoff investment
* [MOEX](https://iss.moex.com/) - to retrieve bond details (name, dates, coupons, price, ...)

### Requirements

* Installed JRE 17+
* Stable Internet connection

### How to run
1. Get token for Tinkoff investment API how it's described in official documentation
2. Create file `{FILE}` and write in it generated token on step 1.
3. Run application and wait until JavaFX window is open (it can take some time)
    * For packed version from [releases](https://github.com/Shemplo/TBS/releases) you can write and then run script from this template:
    ```
    java -jar tinkoff-bond-scanner-{version}.jar {profile name|path to custom profile}
    
    Examples: `java -jar tinkoff-bond-scanner-1.0.jar DEFAULT_RUB` 
           or `java -jar tinkoff-bond-scanner-1.0.jar custom.xml`
    ```
    
    * You can also manually package application to `JAR` file by calling
    ```
    mvn package
    ```
    
    * Custom profiles can be defined with XML using following format:
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <profile>
      <name>Custom profile name</name>
      <token filename="token.txt" responsible="1" />
      <general mr="30" inflation="0.065" />
      <params mte="24" cpy="4" mdtc="30" nv="1000.0" minp="6.0" maxpr="1000" />
      <currencies>RUB</currencies>
      <cmodes>FIXED, NOT_FIXED</cmodes>
      <bannede>-1L</bannede>
    </profile>
    ```
    Tag `params` is required but each attribute of it can absent. In such case corresponding parameter will not be used in bonds filter.
    All other tags and attributes are required and can't be missed. **Path to token** file should be absolute or relative to the location 
    when application is started. Element `bannede` defines list of identifies of emitter that definetely should be filtered out 
    
    To apply custom profile pass path to this file as the first argument for application

### How to use
* Link `🌐` allows to open bonds page in Tinkoff investment (T) and MOEX (M)
* Link `🔍` allows to inspect known coupons of corresponding bond
* In column `📎` you can select bond that should be used for planning.
Then all selected bonds will be shown in `Planning tool` tab, where you
can define category and amount of value to distribute. Also you can use slider
to change diversification percentage (**0%** - distribute all to the first bond, 
**100%** - try to distribute equally between all selected bonds). After parameters
are set distribution will be calculated and shown in corresponding columns and chart.
* Column `👝` shows number of lots in your portfolio (sum by all your accounts)
* Symbol `➥` marks next coupon, symbol `⭿` mark coupon after offer is committed
* You can use double click to copy cell content to clipboard
* Credit values are calculated with consideration to the inflation with the equation: 
let `S` some sum, let `D` number of a days and `I` is inflation in percents then
`s` value equals to `S / (1 + I)^(D / 365)` is sum that corresponding to `S` in `D` days
* In case of coupon value is unknown then latest known value (or 0.0) will be used with
discount of 0.9 modifier due to this value is not reliable and it's some penalty to final score
* There is no guarantee that prices that are mentioned in table are still the same when
table is shown and used for some time. This value is retrieved once when bond data is requested
from MOEX (before table is shown).
