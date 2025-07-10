# 🐍 Игра "Змейка" на Jetpack Compose

Простая, но гибко настраиваемая реализация классической игры "Змейка" с поддержкой emoji,
изображений и классического отображения.

## 🚀 Возможности

- Поддержка трёх режимов визуализации:
    - 🖼️ Изображения
    - 🐍 Emoji
    - 🎨 Классический (цветные прямоугольники)
- Управление свайпами (все 4 направления)
- Случайная генерация еды
- Возможность проходить через стены
- Отображение счёта и количества жизней
- Поддержка победы (если занято всё поле)
- Адаптивная верстка на Jetpack Compose

## ⚙️ Параметры компонента

```kotlin
SnakeGame(
    isWrapWalls = true,
    livesCount = 3,
    mode = RenderMode.EMOJI,
    headImageRes = R.drawable.ic_snake_head,
    foodImageRes = R.drawable.ic_food,
    tailImageRes = R.drawable.ic_snake_tail,
    emojiHead = "🐍",
    emojiFood = "🍎",
    emojiTail = "🍑"
)
```

## 🕹 Управление

Проведите пальцем по экрану, чтобы изменить направление движения змейки.

## Демонстрация

![Classic](/specification/games/img/snake_classic.webp)

![Emoji](/specification/games/img/snake_emoji.webp)

![Image](/specification/games/img/snake_game.gif)
