# 🎨 ColorPickerDialog (Jetpack Compose)

**ColorPickerDialog** — это кастомный UI-компонент, позволяющий пользователю выбрать цвет в формате
RGBA (Red, Green, Blue, Alpha) через слайдеры.

---

## 📌 Возможности

- Просмотр выбранного цвета в превью (круг).
- Настройка каналов **R**, **G**, **B** и **A** (прозрачность) отдельными слайдерами.
- Возврат выбранного значения через callback.
- Кнопки подтверждения («ОК») и отмены («Отмена»).
- Запоминание текущего состояния (`rememberSaveable`).

---

## 🚀 Использование

### Импорт

Убедитесь, что вы используете **Material3** и Jetpack Compose:

### Вызов диалога

```kotlin
var showDialog by remember { mutableStateOf(false) }
var selectedColor by remember { mutableStateOf(Color.Red) }

if (showDialog) {
    ColorPickerDialog(
        initial = selectedColor,
        onPick = { color ->
            selectedColor = color
            showDialog = false
        },
        onClose = { showDialog = false }
    )
}
```

В этом примере при выборе цвета `selectedColor` обновляется, а диалог закрывается.

---

## ⚙️ API

### `@Composable fun ColorPickerDialog(...)`

| Параметр  | Тип               | Описание                                                    |
|-----------|-------------------|-------------------------------------------------------------|
| `initial` | `Color`           | Начальный цвет, отображаемый в диалоге.                     |
| `onPick`  | `(Color) -> Unit` | Callback, вызывается при подтверждении выбора.              |
| `onClose` | `() -> Unit`      | Callback, вызывается при закрытии (крестик, бэк, «Отмена»). |

---

### `@Composable fun ChannelSlider(...)`

| Параметр  | Тип             | Описание                           |
|-----------|-----------------|------------------------------------|
| `name`    | `String`        | Метка канала (`R`, `G`, `B`, `A`). |
| `value`   | `Int`           | Текущее значение канала (0..255).  |
| `onValue` | `(Int) -> Unit` | Callback при изменении значения.   |

---

## 📷 Визуализация

> *(добавьте скриншот в папку `art/` и вставьте сюда)*

![Color Picker](/specification/components/img/colorPickerDialog.gif)

---

## 🛠 Детали реализации

- Для хранения значений каналов используется `rememberSaveable { mutableIntStateOf(...) }`, что
  позволяет сохранять состояние при пересоздании UI.
- Слайдеры `Slider` настроены с диапазоном `0f..255f` и `steps = 254` для дискретных значений.
- Цвет в превью создаётся как `Color(r, g, b, a)`.

---

## 🔮 Идеи для расширения

- Поддержка **HEX-ввода** (#RRGGBB).
- Палитра готовых цветов.
- Выбор оттенка/насыщенности через 2D-градиент.
- Поддержка HSL/HSV помимо RGBA.
