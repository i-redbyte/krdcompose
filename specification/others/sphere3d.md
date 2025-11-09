# Сфера-3D

Компонент `Sphere3D` рисует псевдо-3D-сферу с освещением, поддержкой текстуры (картинка + текст) и
управлением вращением из Jetpack Compose.  
Дополнительно можно включать автоповорот и «радужную» анимацию цвета.

---

## Демо

![](/specification/others/img/sphere3d.gif)


---

## Возможности

- Отрисовка сферы с объёмным освещением (ламберт + небольшой ambient).
- Вращение сферы по вертикали и горизонтали:
    - от жеста пользователя (drag);
    - из анимации (внешнее управление углами).
- Наложение текстуры:
    - иконка/картинка;
    - текст, отрисованный на `Bitmap`, который изгибается по сфере.
- Шейдер вынесен в отдельный `.agsl` файл в `res/raw`.

---

## Требования

- **Android 13+ (API 33+)** — используется `RuntimeShader` + AGSL.
- Jetpack Compose.
- Material 3

---

## API компонента `Sphere3D`

```kotlin
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Sphere3D(
    modifier: Modifier = Modifier,

    sphereColor: Color = Color(0xFFFFC107),
    lightDirection: Float3 = Float3(0.3f, 0.6f, 1f),

    useImage: Boolean = false,
    image: ImageBitmap? = null,

    useText: Boolean = false,
    text: String = "",
    textColor: Color = Color.White,
    textSizeSp: Float = 16f,

    // внешнее управление вращением (радианы)
    rotationX: Float,
    rotationY: Float,

    // вход от жеста drag; вызывается только при свайпе
    onRotateDelta: (dx: Float, dy: Float) -> Unit,
)
```

### Параметры

| Параметр         | Описание                                                                               |
|------------------|----------------------------------------------------------------------------------------|
| `modifier`       | Размер/позиция сферы. Компонент не навязывает аспект-ratio, шар вписывается в область. |
| `sphereColor`    | Базовый цвет сферы, до смешивания с освещением и текстурой.                            |
| `lightDirection` | Направление света (`Float3`), нормализуется в шейдере.                                 |
| `useImage`       | Включить фон-картинку в текстуре.                                                      |
| `image`          | `ImageBitmap` иконки/картинки.                                                         |
| `useText`        | Включить рисование текста на текстуре.                                                 |
| `text`           | Строка, отображаемая по центру сферы.                                                  |
| `textColor`      | Цвет текста.                                                                           |
| `textSizeSp`     | Размер текста в sp.                                                                    |
| `rotationX/Y`    | Текущие углы поворота сферы (радианы), передаются в AGSL.                              |
| `onRotateDelta`  | Колбэк драг-жеста; сюда прилетает дельта `dx/dy` в пикселях для обновления углов.      |

---

## Простой пример использования

```kotlin
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SphereDemoStatic() {
    var rotationX by remember { mutableFloatStateOf(0f) }
    var rotationY by remember { mutableFloatStateOf(0f) }

    Sphere3D(
        modifier = Modifier.fillMaxSize(),
        sphereColor = Color(0xFFE53935),
        lightDirection = Float3(0.2f, 0.7f, 1f),
        useImage = false,
        useText = true,
        text = "C++ Rocks!",
        textColor = Color.White,
        textSizeSp = 18f,
        rotationX = rotationX,
        rotationY = rotationY,
        onRotateDelta = { dx, dy ->
            val sensitivity = 0.01f
            rotationY += dx * sensitivity
            rotationX += dy * sensitivity
        }
    )
}
```

---

## Идеи для развития

- Добавить specular-блик (металлическая/пластиковая сфера).
- Поддержка нескольких источников света.
- Смена текстур/текста «на лету» (например, через список тем/скинов).
- Вынос `SphereState` (углы, цвет, флаги) в отдельный класс для более чистого API.
