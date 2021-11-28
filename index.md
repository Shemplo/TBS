# Tinkoff bonds scanner

There are **50.000+** bonds available on [Tinkoff investments](https://www.tinkoff.ru/invest/). Which one to buy?

I have same question all the time and default bonds filter in Tinkoff doesn't help because it makes to do some manual work 
for understanding quality of each bond, so I made the application that helps to answer this question and makes search for bonds *easier*.

Prerequisites for using TBS are quite simple:
* You need to have installed Java 17 (or higher). You can download it from [OpenJDK 17](https://jdk.java.net/java-se-ri/17) (community build, free to use) or from [Oracle](https://www.oracle.com/java/technologies/downloads/) (official maintaner build, but have some license restrictions)
* Account(s) in Tinkoff investmens with API token that can be retrieved by [this](https://tinkoffcreditsystems.github.io/invest-openapi/auth/) instruction.

And that's all. Now we can use all capabilities of TBS. 

### Download

For now you can download TBS from the [releases](https://github.com/Shemplo/TBS/releases). 
You need to chose the version (the latest is recommended), download `.jar` file from the attachments to the release and double click on it.

Caused by JavaFX uses OpenGL for rendering it have some platform-specific dependencies. In releases `.jar` files are build for Windows,
so you can try to start them on Linux or MacOS but it's not guaranteed that it will run. For Linux and MacOS you can clone repository,
change JavaFX depencecies classifier to your platform and make a package via Maven.  

More detailed instruction how to use TBS is written in [repository](https://github.com/Shemplo/TBS).

### Scanned bonds

![image](https://user-images.githubusercontent.com/14365346/143763226-730f4b16-a914-467d-b643-755e86194d4c.png)

On the first tab you can find all bonds that matches your set up - price, days to next coupon, apriory percentage, and other (your set up is shown on top of the window). 
Also for your convenience they are sorted by `Score` value which is calculated by some formula and depends on your wishes (I tried to do my best with it) 
and some values are highlited with colors and font styles. Besides useful buttons at the begginning of each row will help you to open page in Tinkoff investmens, 
MOEX and open coupon details.

Going from the top to the bottom you can choose bonds that you like the most.

### Portfolio bonds

![image](https://user-images.githubusercontent.com/14365346/143763400-59758dad-6990-4046-b933-0ac618cab23d.png)

On the second tab you can find your portfolio. It has almost the same table without some columns. 
They are removed because of their inconsistency, for example `Price` because different lots can be bought at different time 
and different price, what value should be then in this column? Average? My thoughts sais that it's useless information.

### Planning tool

![image](https://user-images.githubusercontent.com/14365346/143763619-40c0af6c-5501-45f6-b6f5-80cc77cc4b99.png)

When you want to struct your future (and don't spend all money in the first day) you can use planner tool on the third tab. 
You can tick with a checkbox bonds that you want to buy. Then they will appear in the planner.

![image](https://user-images.githubusercontent.com/14365346/143763733-01c5b3b9-6736-49ff-9df7-d80d221ea6fb.png)
<div align="center">☟</div>

![image](https://user-images.githubusercontent.com/14365346/143763771-a9f5f81c-7392-40fe-a895-6383bca17b73.png)

After you chosen bonds for the next planning period you can start planning. 
For that you just need to select category to be distributed between selected bonds and quantity for that category. 
Currently TBS supports two categories - money (SUM) and amount of lots (LOTS). 
There is also one more parameter - diversifcation that can be edited with slider or directly in input field.
It defines distribution line angle, in simple words if it has value close to `0%` then all quantities will be passed to the mosly scored bond with number `1`, 
if it has value close to `100%` then all quantities will be distributed equally (mostly) between all bonds. 
Talking about diversification you can see to the chart bellow and watch how distribution is changing when you varying diversification value. 

|Diversification 31.88%|Diversification 78.26%|
|-|-|
| ![image](https://user-images.githubusercontent.com/14365346/143764052-4dd15a6b-c269-434f-b6c1-cd55e90c4eca.png) | ![image](https://user-images.githubusercontent.com/14365346/143764061-7434db17-9ea8-41bb-a8ac-1f9e273c8abe.png) |

If you chose satisfiying diversification value but whant to change something you still have opportunity to do it by passing direclty to bond
quantity that you want. You words are the last in planning, the machine will obey you.

During the planning do not forget to take a watch to a total values to be sure that you are in bounds (money or lots).

When planning is done you can also export it to excel file. You can don't worry about data lost after TBS is closed because it dump your input to a save file 
and on the next start up can restore your input (if you don't delete file `plan.bin`).

### Balance control

![image](https://user-images.githubusercontent.com/14365346/143764262-ba195b92-3e3e-4026-94d3-c891be00e4ed.png)

On the last tab you can see how balance of your account(s) will change in time. Bar chart shows coupons payments in corresponding dates, 
when area chart shows total income from the today till the end of chart. You are able to customize chart view by selecting date groupping (DAY, MONTH, YEAR), 
translation of chart to the future or past (offset parameter) and scaling amoung of displayed dates in chart (chart dates).

## Conclusion

I hope that TBS helps you to find your wish. If you have suggestions how to improve TBS then write me or open the issue in [repository](https://github.com/Shemplo/TBS), 
because I'm open to talk and interested in improving TBS.
