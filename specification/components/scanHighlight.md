# ScanHighlight

Composable-компонент для создания **эффекта сканирования (scan-line)**.

Используется для визуального выделения элемента списка: по карточке проходит светящаяся линия,
имитируя сканирование.

---

## ✨ Особенности

- Управление через `scanTrigger` — анимация запускается по событию
- Поддержка режимов:
    - `Overlay` — поверх контента
    - `Clipped` — внутри карточки
- Настраиваемые цвета и анимации через `HackerListStyle`

---

## 🎬 Demo

![ScanHighlight Demo](/specification/components/img/scanHighlight.gif)


---

## 🚀 Использование

```kotlin
ScanHighlight(
    scanTrigger = scanTrigger,
    selected = selected,
    style = style
) {
    YourContent()
}
```

---

## 🧩 Параметры

| Параметр      | Тип                      | Описание                           |
|---------------|--------------------------|------------------------------------|
| `scanTrigger` | `Int`                    | При изменении запускает анимацию   |
| `selected`    | `Boolean`                | Влияет на border/glow              |
| `style`       | `HackerListStyle`        | Палитра, размеры, режимы, скорости |
| `content`     | `@Composable () -> Unit` | Контент карточки                   |

---

## ⚙️ Как запускать сканирование

```kotlin
val scanTriggers = remember { mutableStateMapOf<Int, Int>() }

onClick = {
    scanTriggers[id] = (scanTriggers[id] ?: 0) + 1
}
```

---

## 🎨 Настройка стиля

```kotlin
val style = HackerListStyles.MatrixGreen.copy(
    animations = HackerAnimationSpec(
        scanDurationMillis = 1400
    )
)
```

---

## 🧠 Как это работает

- Используется `Animatable` для позиции scan-line
- `drawWithContent` рисует glow и core-линию
- После завершения анимации видимость сбрасывается
- В режиме `Clipped` линия не выходит за форму карточки

---

## ⚠️ Важно

- Не завязывай сканирование только на `selected`
- Лучше использовать отдельный `scanTrigger`
- Для аккуратного UI чаще всего подходит `ScanLineMode.Clipped`


