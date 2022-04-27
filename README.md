# Tinkoff bonds scanner

[![Creating Artifacts](https://github.com/Shemplo/TBS/actions/workflows/github-actions.yml/badge.svg?branch=master&event=push)](https://github.com/Shemplo/TBS/actions/workflows/github-actions.yml)

Приложение для поиска, фильтрации и ранжирования облигаций, доступных в [Тинькофф инвестициях](https://www.tinkoff.ru/invest/).

Фильтрация поддерживает несколько параметров (цена, номинал, количество дней до следующего купона, ...), на основании которых
в дальнейшем считается собираетельная характеристика облигации `Score`, по которой и производится ранжирование.

**Предупреждение:** характеристика `Score` не является гарантией правильного выбора облигации, не является инвестиционной идеей, 
советом, рекомендацией, предложением купить или продать ценные бумаги и другие финансовые инструменты. Это просто подсказка,
на что стоит обратить внимание в первую очередь. Мы стараемся сделать как можно объективнее и точнее.

### Использованные ресурсы

* [API Тинькофф Инвестиций](https://github.com/Tinkoff/invest-api-java-sdk) - получение данных из Тинькофф инвестиций согласно [документации](https://tinkoff.github.io/investAPI).
* [ISS MOEX](https://iss.moex.com/) - получение детальной информации об облигации (названия, даты, купоны, последняя цена, ...).

### Требования перед запуском приложения

* Установленная виртуальная Java машина (JVM) версии 17 и выше.
  Скачать: [OpenJDK 17](https://jdk.java.net/java-se-ri/17) или [Oracle](https://www.oracle.com/java/technologies/downloads/).
* Аккаунт в Тинькофф инвестициях и токен для API запросов. 
  * Официальная [инструкция](https://tinkoff.github.io/investAPI/token/) по получению токена.
  * Прямая [ссылка](https://www.tinkoff.ru/invest/settings/api/) для тех, кто ценит своё время.
* Стабильное Интернет-соединение.

### Запуск приложения

1. Скачайте последнюю версию приложения в отдельную директорию. Быстрое скачивание **Windows**: [EXE](https://github.com/Shemplo/TBS/releases/latest/download/TBS.exe).
2. Если вы пользователь **Windows**, то запустите приложение (EXE файл) двойным кликом по нему. 

* Для всех платформ запустите файл через командную строку `java -jar TBS-<платформа>.jar`.

При запуске откроется окно launcher'а, которое позволит выбрать, что вы хотете сделать - 
открыть существующий набор облигаций или просканировать заново (перед этим выбрав профиль из списка).

### Куда смотреть, что нажимать?

Приложение имеет довольно простой пользовательский интерфейс, и удобство использования должно прийти с опытом, но про некоторые моменты всё-таки стоит рассказать:

* Для того, чтобы открыть страницу облигаций на сайте Тинькофф инвестиций или Московской биржи (MOEX) можно нажать соответствующие кнопки
  `🌐` в начале каждой строки в таблице.
* Чтобы просмотреть купоны по какой-то облигации необходимо нажать на кнопку `🔍`.
    * Первая колонка может содержать символ следующего купона (➥), либо символ оферты (⭿),
      после которой величина купона может измениться как в большую, так и в меньшую сторону.
    * Далее идут колонки, которые содержат дату купона; величину купона; информацию о том, известна ли достоверна величина купона
      (если нет, то берётся величина предыдущего купона), а также доходность купона с учётом инфляции.
    * Если величина купона не известна достоверно (не опубликована ещё эмитентом), то берётся значение предыдущего купона, а при расчёте доходности применяется
      коэффициент `0.9` как штраф за недостоверную информацию.
* В колонке `📎` можно отметить облигации, которые будут использоваться в планировании (подробнее об этом можно прочитать на сайте приложения).
* Далее идут колонки название и тикер облигации; валюта; количество лотов данного тикера во всех Ваших счетах (`👝`); суммарный доход с учётом инфляции;
  доход по купонам с учётом инфляции; последняя цена сделки; номинал; количество купонов в год; дата следующего купона; фиксированные или нефиксированные купоны;
  количество лет и месяцев до выхода облигации из обращения и априорный процент доходности с Московской биржи.
    * Учёт инфляции выполнен следующим образом, пусть `S` - некоторая денежная сумма на сегодняшний день, пусть `D` - это количество дней, а `I` - уровень инфляции
      в процентах, тогда значение `s` равное `S / (1 + I)^(D / 365)` - будет называться соответствующей денежной суммой `S` через `D` дней с уровнем инфляции `I`.
* В планировщике есть неочевидный параметр диверсификации. Формально, он определяет угол кривой распределения значения, простыми словами это значит,
  что при значении 0% всё значение величины будет назначено первой облигации в таблице, при 100% – распределяемая величина будет равномерно (почти)
  назначена всем облигациям.
* Цены в таблице не обновляются в режиме реального времени, поэтому фактическая цена на сайте Тинькофф инвестиций может отличаться
  от приведённой в таблице после запуска приложения.
* Любое значение в таблицах можно скопировать в буфер обмена двойным щелчком по ячейке таблицы.

### В дальнейших планах

* Обновление базы эмитентов из репозитория (без ручного прокликивания каждого эмитента)
