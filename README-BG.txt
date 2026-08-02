ПРЕМИУМ ДИЗАЙН ЗА САМОТО ПРИЛОЖЕНИЕ

Замени:
- activity_main.xml
- MainActivity.kt

Добави:
- app_dashboard_background.xml
- ic_gust.xml

Увери се, че вече имаш:
- ic_windsock.xml
- ic_lighthouse.xml
- ic_wind.xml
- ic_direction_arrow.xml
- compass_circle.xml

В .github/workflows/main.yml добави:

cp app_dashboard_background.xml app/src/main/res/drawable/
cp ic_gust.xml app/src/main/res/drawable/

Редът за activity_main.xml трябва да остане:
cp activity_main.xml app/src/main/res/layout/

След Commit changes:
1. Изчакай зелен build.
2. Свали APK.
3. Инсталирай новата версия.
4. Отвори приложението и натисни бутона за обновяване.

Този пакет променя приложението, не widget-а.
