# PlanetCard

Гибкий и настраиваемый Jetpack Compose-компонент для отображения карточек планет в космическом
стиле.

## Возможности

`PlanetCard` подходит для:

- витринных карточек
- onboarding-экранов
- educational UI
- тематических экранов ко Дню космонавтики
- карточек миссий, спутников и планет

Компонент поддерживает:

- кастомные градиенты карточки
- отдельную настройку внешнего вида планеты
- кольца, кратеры, полосы
- glow-эффекты
- фоновые звёзды
- анимации парения, вращения и мерцания
- слоты для `header`, `content`, `footer`
- кастомные размеры и позиционирование планеты

---

## 🎬 Demo

![PlanetCard Demo](/specification/components/img/planetCard.gif)


---

## Основные сущности

### `PlanetCard`

Главный composable-компонент карточки.

### `PlanetCardStyle`

Отвечает за внешний вид самой карточки:

- форма
- фон
- border
- glow
- отступы
- минимальная высота
- количество звёзд
- включение/отключение декоративных эффектов

### `PlanetVisualStyle`

Отвечает за визуал самой планеты:

- основные цвета
- кольца
- атмосфера
- тени
- кратеры
- полосы

### `PlanetAnimationSpec`

Настройка анимаций:

- floating
- rotation
- twinkling stars
- длительности
- амплитуда движения

### `PlanetCardBadge`

Небольшой бейдж над карточкой, например:

- `Избранное`
- `Миссия`
- `Доступно`

---

## Базовый пример

```kotlin
PlanetCard(
    title = "Марс",
    subtitle = "Красная планета",
    description = "Гибкая карточка с кастомным визуалом и анимацией.",
    badge = PlanetCardBadge("Избранное"),
    style = PlanetCardDefaults.cardStyle(
        colors = PlanetCardDefaults.marsCardColors()
    ),
    planetStyle = PlanetCardDefaults.planetStyle(
        colors = listOf(
            Color(0xFFF6B26B),
            Color(0xFFD96C3E),
            Color(0xFFA63D2F),
        ),
        showCraters = true,
    ),
    footer = {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricPill(label = "Расстояние", value = "225 млн км")
            MetricPill(label = "Сутки", value = "24.6 ч")
        }
    }
)
```

---

## Параметры `PlanetCard`

### Контент

- `title` — заголовок карточки
- `subtitle` — подзаголовок
- `description` — описание
- `badge` — бейдж
- `header` — дополнительный верхний слот
- `content` — кастомный контент внутри карточки
- `footer` — нижний слот, например с метриками

### Поведение

- `onClick` — обработчик нажатия
- `enabled` — доступность карточки

### Внешний вид карточки

- `style` — объект `PlanetCardStyle`

### Внешний вид планеты

- `planetStyle` — объект `PlanetVisualStyle`
- `planetSize` — размер планеты
- `planetAlignment` — выравнивание внутри карточки
- `planetPadding` — внутренний отступ планеты

### Анимации

- `animationSpec` — объект `PlanetAnimationSpec`

---

## Настройка карточки

### 1. Цвета карточки

```kotlin
val customCardColors = PlanetCardColors(
    containerGradient = listOf(
        Color(0xFF071A31),
        Color(0xFF10304F),
        Color(0xFF18496B),
    ),
    contentColor = Color(0xFFF2FAFF),
    glowColor = Color(0xFF4FC3F7).copy(alpha = 0.22f),
)
```

### 2. Стиль карточки

```kotlin
val customCardStyle = PlanetCardStyle(
    colors = customCardColors,
    minHeight = 200.dp,
    contentPadding = 20.dp,
    starCount = 24,
    showStars = true,
    showGlow = true,
)
```

### 3. Визуал планеты

```kotlin
val customPlanetStyle = PlanetVisualStyle(
    colors = listOf(
        Color(0xFF7CFF8A),
        Color(0xFF2ACB65),
        Color(0xFF0E7A32),
    ),
    showRings = false,
    showAtmosphere = true,
    showShadow = true,
    showCraters = false,
    showStripes = true,
)
```

### 4. Анимация

```kotlin
val customAnimation = PlanetAnimationSpec(
    enableFloating = true,
    floatingAmplitude = 8.dp,
    floatingDurationMillis = 3200,
    enableRotation = true,
    rotationDurationMillis = 15000,
    enableTwinklingStars = true,
)
```

---

## Пример полностью кастомной карточки

```kotlin
PlanetCard(
    title = "Андроид-Краснодар",
    subtitle = "Зелёная планета технологий",
    description = "Посвящается самому лучшему IT-сообществу города!",
    badge = PlanetCardBadge("Краснодар"),
    style = PlanetCardStyle(
        colors = PlanetCardColors(
            containerGradient = listOf(
                Color(0xFF07140B),
                Color(0xFF0D2514),
                Color(0xFF173A1F),
            ),
            contentColor = Color(0xFFEFFFF1),
            glowColor = Color(0xFF8BFF98).copy(alpha = 0.22f),
            badgeContainerColor = Color(0xFF7CFF8A).copy(alpha = 0.14f),
            badgeContentColor = Color(0xFFDFFFE3),
        ),
        minHeight = 196.dp,
        contentPadding = 20.dp,
        starCount = 20,
        showStars = true,
        showGlow = true,
    ),
    planetStyle = PlanetVisualStyle(
        colors = listOf(
            Color(0xFFA8FF78),
            Color(0xFF58D668),
            Color(0xFF1E8E3E),
        ),
        glowColor = Color(0xFF90FF9A).copy(alpha = 0.24f),
        showRings = false,
        showAtmosphere = true,
        atmosphereColor = Color(0xFFBFFFC7).copy(alpha = 0.18f),
        showShadow = true,
        showCraters = false,
        showStripes = true,
        stripeColor = Color.White.copy(alpha = 0.10f),
    ),
    animationSpec = PlanetAnimationSpec(
        enableFloating = true,
        enableRotation = true,
        enableTwinklingStars = true,
        floatingAmplitude = 7.dp,
    ),
    footer = {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricPill(label = "Тема", value = "Android")
            MetricPill(label = "Город", value = "Краснодар")
        }
    }
)
```

---

## Шаблон пресета: Андроид-Краснодар

Ниже — вариант, который удобно вынести в отдельный файл и переиспользовать.

```kotlin
object AndroidKrasnodarPlanetDefaults {

    fun cardColors(): PlanetCardColors = PlanetCardColors(
        containerGradient = listOf(
            Color(0xFF07140B),
            Color(0xFF0D2514),
            Color(0xFF173A1F),
        ),
        contentColor = Color(0xFFEFFFF1),
        glowColor = Color(0xFF8BFF98).copy(alpha = 0.22f),
        badgeContainerColor = Color(0xFF7CFF8A).copy(alpha = 0.14f),
        badgeContentColor = Color(0xFFDFFFE3),
    )

    fun cardStyle(): PlanetCardStyle = PlanetCardStyle(
        colors = cardColors(),
        minHeight = 196.dp,
        contentPadding = 20.dp,
        starCount = 20,
        showStars = true,
        showGlow = true,
    )

    fun planetStyle(): PlanetVisualStyle = PlanetVisualStyle(
        colors = listOf(
            Color(0xFFA8FF78),
            Color(0xFF58D668),
            Color(0xFF1E8E3E),
        ),
        glowColor = Color(0xFF90FF9A).copy(alpha = 0.24f),
        showRings = false,
        showAtmosphere = true,
        atmosphereColor = Color(0xFFBFFFC7).copy(alpha = 0.18f),
        showShadow = true,
        showCraters = false,
        showStripes = true,
        stripeColor = Color.White.copy(alpha = 0.10f),
    )

    fun animationSpec(): PlanetAnimationSpec = PlanetAnimationSpec(
        enableFloating = true,
        enableRotation = true,
        enableTwinklingStars = true,
        floatingAmplitude = 7.dp,
    )
}
```

### Использование пресета

```kotlin
PlanetCard(
    title = "Андроид-Краснодар",
    subtitle = "Зелёная планета технологий",
    description = "Посвящается самому лучшему IT-сообществу города!",
    badge = PlanetCardBadge("KRD"),
    style = AndroidKrasnodarPlanetDefaults.cardStyle(),
    planetStyle = AndroidKrasnodarPlanetDefaults.planetStyle(),
    animationSpec = AndroidKrasnodarPlanetDefaults.animationSpec(),
    footer = {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricPill(label = "Стиль", value = "Android")
            MetricPill(label = "Цвет", value = "Зелёный")
        }
    }
)
```

---

## Рекомендации по использованию

### Когда использовать `footer`

Когда нужно показать короткие метрики:

- спутники
- диаметр
- расстояние
- продолжительность суток
- статус миссии

### Когда использовать `content`

Когда внутри карточки нужен кастомный UI:

- дополнительные бейджи
- кнопки
- статистика
- произвольные composable-элементы

### Когда отключать анимации

Если карточка используется в длинном списке, имеет смысл отключить часть анимаций:

```kotlin
PlanetAnimationSpec(
    enableFloating = false,
    enableRotation = false,
    enableTwinklingStars = true,
)
```
