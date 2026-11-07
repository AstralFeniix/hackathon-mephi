# Лицензии шрифтов

Все шрифты вкомпилированы в APK и работают офлайн. Кириллица проверена для
каждой гарнитуры — иначе русский интерфейс молча падал бы на системный шрифт.

| Файл | Гарнитура | Роль | Источник | Лицензия |
|---|---|---|---|---|
| `onest.ttf` | Onest | Bloom: головы и текст | [Google Fonts](https://fonts.google.com/specimen/Onest) | SIL OFL 1.1 — `licenses/OFL-Onest.txt` |
| `lora.ttf` | Lora | Bloom: один serif-момент («Тихо») | [Google Fonts](https://fonts.google.com/specimen/Lora) | SIL OFL 1.1 — `licenses/OFL-Lora.txt` |
| `playfair.ttf` | Playfair Display | Lumen: головы строчными | [Google Fonts](https://fonts.google.com/specimen/Playfair+Display) | SIL OFL 1.1 — `licenses/OFL-Playfair.txt` |
| `geist.ttf` | Geist | Lumen: текст | [Google Fonts](https://fonts.google.com/specimen/Geist) | SIL OFL 1.1 — `licenses/OFL-Geist.txt` |
| `geist_mono.ttf` | Geist Mono | Lumen: таймкоды и отсчёты | [Google Fonts](https://fonts.google.com/specimen/Geist+Mono) | SIL OFL 1.1 — `licenses/OFL-GeistMono.txt` |

## Почему не Ranade и Instrument Serif

В темах Hallmark Bloom и Lumen указаны Ranade, Gambarino, Instrument Serif,
но у этих файлов нет кириллических глифов — русские заголовки отрендерились бы
системным шрифтом. Поэтому взяты ближайшие по голосу гарнитуры с кириллицей:

- Ranade → **Onest** (тёплый геометрический гротеск);
- Gambarino → **Lora** (римский serif для одного слова);
- Instrument Serif → **Playfair Display** (высококонтрастный display-serif).

Палитра, сетка, радиусы и логика поверхностей остаются из Bloom и Lumen.
