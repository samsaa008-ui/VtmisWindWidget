VTMIS WIDGET 5x2

Замени в главната папка:
- widget_wind.xml
- WindWidgetProvider.kt
- wind_widget_info.xml
- widget_background_premium.xml
- compass_circle.xml
- ic_direction_arrow.xml
- ic_wind.xml
- ic_gust.xml
- ic_windsock.xml
- ic_lighthouse.xml

В .github/workflows/main.yml трябва да има:

cp widget_wind.xml app/src/main/res/layout/
cp wind_widget_info.xml app/src/main/res/xml/
cp widget_background_premium.xml app/src/main/res/drawable/
cp compass_circle.xml app/src/main/res/drawable/
cp ic_direction_arrow.xml app/src/main/res/drawable/
cp ic_wind.xml app/src/main/res/drawable/
cp ic_gust.xml app/src/main/res/drawable/
cp ic_windsock.xml app/src/main/res/drawable/
cp ic_lighthouse.xml app/src/main/res/drawable/

След инсталацията:
1. Премахни стария widget.
2. Добави го отново.
3. Android ще го предложи като 5x2.
4. При различен launcher може да се наложи леко разтягане.

Обновяване:
- автоматично приблизително на 15 минути;
- ръчно чрез ↻;
- стрелката е обърната с 180°, за да показва накъде духа.
