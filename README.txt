ВЕРСИЯ 2 — АДАПТИВЕН WIDGET

Качи/замени в главната папка на GitHub:
1. WindWidgetProvider.kt
2. widget_wind.xml
3. wind_widget_info.xml
4. widget_wind_small.xml (нов файл)

После в .github/workflows/main.yml, в стъпката
"Create Android project structure", добави реда:

cp widget_wind_small.xml app/src/main/res/layout/

Commit changes и изчакай зелена отметка в Actions.

След инсталиране:
- добави widget-а два пъти;
- единия остави малък (2x1);
- втория разтегли на 4x2 или по-голям;
- дизайнът се сменя автоматично според ширината.

Цветове:
- под 5 m/s: зелено
- от 5 до под 10 m/s: жълто
- 10+ m/s: червено
