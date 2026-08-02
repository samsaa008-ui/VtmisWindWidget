ТОВА Е КОРЕКЦИЯТА ЗА РАЗМЕРА ОТ СНИМКАТА

Резултат:
- РК и БЦ се виждат едновременно;
- скорост и порив са с еднакъв размер;
- m/s винаги е на същия ред;
- икона на ветропоказател за РК;
- икона на фар за БЦ;
- стрелка за посоката;
- по-четливи шрифтове без разцепване на редове.

Качи/замени в главната папка:
1. WindWidgetProvider.kt
2. widget_wind_medium.xml
3. ic_wind.xml

Увери се, че вече имаш:
- widget_background_premium.xml
- ic_windsock.xml
- ic_lighthouse.xml
- ic_direction_arrow.xml
- widget_wind.xml
- widget_wind_small.xml

В .github/workflows/main.yml добави:
cp widget_wind_medium.xml app/src/main/res/layout/
cp ic_wind.xml app/src/main/res/drawable/

После Commit changes.

След инсталацията:
1. Премахни стария widget от началния екран.
2. Добави го наново.
3. Остави го на приблизително същия размер като на снимката.
4. Ако пак зареди стария изглед, разтегли леко настрани и върни размера.
