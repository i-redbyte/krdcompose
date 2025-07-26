# FortuneWheel — Jetpack Compose «Колесо фортуны»

Вращающееся колесо с секторами для Jetpack Compose. По нажатию колесо начинает вращаться по часовой
стрелке, плавно замедляется по выбранной **easing**‑кривой и останавливается так, чтобы центр
сектора оказался под **красной фиксированной стрелкой** (смотрит вниз). По завершении возвращает
выпавший элемент.

> Полностью на стандартных средствах Compose/Kotlin, без сторонних библиотек. Сохраняет состояние
> при повороте экрана. Поддерживает принудительный выбор победителя.

---

## Содержание

- [Демонстрация](#демонстрация)
- [Установка](#установка)
- [Быстрый старт](#быстрый-старт)
- [API](#api)
    - [`WheelItem`](#wheelitem)
    - [`FortuneWheel`](#fortunewheel)
- [Кривые easing](#кривые-easing)
- [Как выбирается победитель](#как-выбирается-победитель)
- [Адаптация под ландшафт](#адаптация-под-ландшафт)
- [Диагностика](#диагностика)

---

## Демонстрация

```kotlin
val items = listOf(
    WheelItem("OCaml"), WheelItem("C"), WheelItem("Kotlin"),
    WheelItem("Python"), WheelItem("Java"), WheelItem("C++"),
    WheelItem("Haskell"), WheelItem("PHP"), WheelItem("C#"), WheelItem("Perl")
)

var selected by remember { mutableStateOf<WheelItem?>(null) }
var easingName by remember { mutableStateOf("Sine‑out") }
val easing = easingMap[easingName] ?: { t: Float -> t }

Column(horizontalAlignment = Alignment.CenterHorizontally) {
    FortuneWheel(
        items = items,
        modifier = Modifier.fillMaxWidth(0.9f),
        evenSectorColor = Color(0xFF1B5E20),
        oddSectorColor = Color(0xFFFFEB3B),
        evenTextColor = Color(0xFFFFEB3B),
        oddTextColor = Color(0xFF1B5E20),
        easing = easing,
        onItemSelected = { selected = it }
    )

    Spacer(Modifier.height(24.dp))
    Text("Выпало: ${selected?.text ?: "—"}")
}
```

![sapper](/specification/games/img/fortuneWheel.gif)

---

## Установка

1. Скопируйте файл с компонентом `FortuneWheel` и  `easingMap` в ваш модуль.
2. Измените `package` под ваш проект при необходимости.

---

## Быстрый старт

```kotlin
val easing = easingMap["Sine‑out"] ?: { t: Float -> t }

FortuneWheel(
    items = listOf(WheelItem("50"), WheelItem("100"), WheelItem("200")),
    evenSectorColor = Color.Black,
    oddSectorColor = Color.White,
    easing = easing,
    onItemSelected = { item -> /* обработать результат */ }
)
```

---

## API

### `WheelItem`

| Свойство | Тип    | Описание                       |
|----------|--------|--------------------------------|
| `text`   | String | Текст, отображаемый в секторе. |

### `FortuneWheel`

```kotlin
@Composable
fun FortuneWheel(
    items: List<WheelItem>,
    modifier: Modifier = Modifier,
    evenSectorColor: Color = Color.Black,
    oddSectorColor: Color = Color.White,
    evenTextColor: Color? = null,
    oddTextColor: Color? = null,
    easing: (Float) -> Float,
    forcedWinnerIndex: Int? = null,
    onItemSelected: (WheelItem) -> Unit
)
```

| Параметр            | Тип                   | По умолчанию  | Описание                                                                                       |
|---------------------|-----------------------|---------------|------------------------------------------------------------------------------------------------|
| `items`             | `List<WheelItem>`     | —             | Элементы колеса (минимум 2).                                                                   |
| `modifier`          | `Modifier`            | `Modifier`    | Внешний модификатор.                                                                           |
| `evenSectorColor`   | `Color`               | `Color.Black` | Цвет чётных секторов (индексы 0,2,4…).                                                         |
| `oddSectorColor`    | `Color`               | `Color.White` | Цвет нечётных секторов (1,3,5…).                                                               |
| `evenTextColor`     | `Color?`              | `null`        | Цвет текста на чётных секторах. Если `null`, выбирается автоматически по контрасту.            |
| `oddTextColor`      | `Color?`              | `null`        | Цвет текста на нечётных секторах. Если `null`, выбирается автоматически по контрасту.          |
| `easing`            | `(Float) -> Float`    | —             | Функция прогресса анимации `t∈[0;1] → [0;1]`.                                                  |
| `forcedWinnerIndex` | `Int?`                | `null`        | Если задан и входит в диапазон `items.indices`, колесо **всегда** остановится на этом секторе. |
| `onItemSelected`    | `(WheelItem) -> Unit` | —             | Колбэк с элементом, оказавшимся под стрелкой.                                                  |

---

## Кривые easing

В комплекте идёт `easingMap: LinkedHashMap<String, (Float) -> Float>` с ~20 преднастроенными
кривыми:

- `Sine‑out`, `Sine‑inOut`
- `Linear`
- `Quad‑out`, `Quad‑inOut`
- `Cubic‑out`
- `Quart‑in`, `Quart‑out`
- `Quint‑out`, `Quint‑inOut`
- `Expo‑out`, `Expo‑inOut`
- `Circ‑out`, `Circ‑inOut`
- `Back‑out`, `Back‑inOut`
- `Elastic‑out`, `Elastic‑inOut`
- `Bounce‑out`, `Bounce‑inOut`

Можно добавить свою: отдельно передайте функцию `easing: (Float) -> Float` в `FortuneWheel`.

---

## Как выбирается победитель

- Стрелка указывает вниз, то есть «прицел» — **90°**.
- Центр сектора с индексом `i` равен `90° + i * (360° / N)`, где `N` — количество элементов.
- Целевой угол = текущий угол + `k` полных оборотов + дельта до центра выбранного сектора.
- По завершении угол нормализуется, индекс победителя вычисляется по фактическому конечному углу —
  это гарантирует корректность даже при накоплении ошибок округления.

Формулы:

```
seg = 360 / N
center(i) = 90 + i * seg
now = currentAngle mod 360
delta = (center(i) - now) mod 360
```

---

## Адаптация под ландшафт

Компонент использует `Modifier.aspectRatio(1f)` и занимает доступную ширину. Если в ландшафте текст
«Выпало: …» не попадает на экран, используйте один из вариантов:

1. **Скролл контейнера**
   ```kotlin
   Column(Modifier.verticalScroll(rememberScrollState())) { /* wheel + text */ }
   ```

2. **Взвешивание по высоте**
   ```kotlin
   FortuneWheel(
       items = items,
       modifier = Modifier
           .fillMaxWidth()
           .weight(1f),
       easing = easing,
       onItemSelected = { /* ... */ }
   )
   ```

3. **Жёсткий лимит по минимальной стороне**
   Используйте `BoxWithConstraints` и рисуйте квадрат со стороной`min(maxWidth, maxHeight * 0.85f)`.

---

## Диагностика

- **Серый прямоугольник при клике** — это ripple. В компоненте он отключён: `indication = null`.
- **Не видно текста «Выпало: …»** — уменьшите колесо (см. раздел «Адаптация под ландшафт») или
  добавьте скролл.
- **Нужно всегда выдавать один и тот же сектор** — задайте `forcedWinnerIndex = нужный_индекс`.

---

