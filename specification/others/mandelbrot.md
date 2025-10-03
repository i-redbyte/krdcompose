# Множество Мандельброта(Jetpack Compose + AGSL, Android 13+)

Анимируемый
фон [множества Мандельброта](https://ru.wikipedia.org/wiki/%D0%9C%D0%BD%D0%BE%D0%B6%D0%B5%D1%81%D1%82%D0%B2%D0%BE_%D0%9C%D0%B0%D0%BD%D0%B4%D0%B5%D0%BB%D1%8C%D0%B1%D1%80%D0%BE%D1%82%D0%B0)
для Jetpack Compose. Рендер происходит на **GPU** через *
*AGSL** (`RuntimeShader`) поверх Skia (под капотом это может быть OpenGL ES или Vulkan — см. FAQ).
Поверх фона можно располагать любой Compose‑UI.

---

## Содержание

- [Возможности](#возможности)
- [Требования](#требования)
- [Демо](#демо)
- [AGSL шейдер (`res/raw/mandelbrot.agsl`)](#agsl-шейдер-resrawmandelbrotagsl)
- [Экран с контролами и жестами](#экран-с-контролами-и-жестами)
- [Производительность и анимация](#производительность-и-анимация)
- [Известные ограничения AGSL](#известные-ограничения-agsl)
- [FAQ: Как это работает?](#faq-это-через-opengl)

---

## Возможности

- ⚡ Рендер на **GPU** через `RuntimeShader` (AGSL).
- 🎛️ Параметры: `maxIterations`, `centerX`, `centerY`, `scale` (зум), `animationSpeed`, `hueShift`.
- 🧭 Жесты: панорамирование и пинч‑зум (в примере `MandelbrotBackgroundScreen`).
- 🧱 Отдельный слой `View` внутри `AndroidView` → минимальное влияние на UI поверх.

---

## Требования

- **Android 13+ (API 33+)** — `RuntimeShader` доступен с Android 13.
- `compileSdk` ≥ 33.
- Jetpack Compose.

---

## Демо

| ![](/specification/others/img/mandelbrot1.gif) | ![](/specification/others/img/mandelbrot2.gif) |
|------------------------------------------------|------------------------------------------------|

---

## AGSL шейдер (`res/raw/mandelbrot.agsl`)

> ⚠️ Сохраните файл как **UTF‑8 без BOM** и расширением **`.agsl`**.

```glsl
// res/raw/mandelbrot.agsl

uniform float2 iResolution; // px
uniform float2 uCenter;     // центр в плоскости
uniform float  uScale;      // видимая высота/ширина по короткой стороне
uniform float  uTime;       // секунды (течёт всегда)
uniform float  uHueShift;   // [0..1)
uniform float  uMaxIter;    // итерации (float из Kotlin)

// HSV -> RGB
float3 hsv2rgb(float3 c) {
    float4 K = float4(1.0, 2.0/3.0, 1.0/3.0, 3.0);
    float3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

half4 main(float2 fragCoord) {
    float2 res = iResolution;
    float2 uv = (fragCoord - 0.5*res) / res.y; // нормализация по короткой стороне

    float2 c = float2(uCenter.x + uv.x * uScale,
                      uCenter.y + uv.y * uScale);
    float2 z = float2(0.0, 0.0);

    // лимит итераций (через float->int: min для float‑типов)
    const int MAX_CAP = 1024;
    float maxIterF = min(uMaxIter, float(MAX_CAP));
    int   maxIter  = int(maxIterF);

    // цикл Мандельброта — AGSL требует for с декларацией
    int ii = 0;
    for (int i = 0; i < MAX_CAP; ++i) {
        ii = i;
        if (i >= maxIter) break;

        // z = z^2 + c
        float x = (z.x*z.x - z.y*z.y) + c.x;
        float y = (2.0*z.x*z.y) + c.y;
        z = float2(x, y);

        if (dot(z, z) > 4.0) break; // вышли за радиус 2
    }

    float iter = float(ii);

    // smooth coloring, если вышли раньше лимита
    if (ii < maxIter) {
        float zn = max(dot(z, z), 1e-8); // защита от log(0)
        float nu = log2(0.5 * log(zn));
        iter = max(0.0, iter - nu + 4.0);
    }

    // Небольшая цветовая анимация по времени
    float h = fract(uHueShift + 0.015*iter + 0.05*sin(uTime*0.5));
    float s = 0.9;
    float v = (ii >= maxIter) ? 0.0 : 1.0; // внутри множества — почти чёрный
    float3 rgb = hsv2rgb(float3(h, s, v));

    return half4(rgb, 1.0);
}
```

---

## Экран с контролами и жестами

- Панорамирование и пинч‑зум через `detectTransformGestures`.
- Контролы: `maxIterations`, `scale`, `animationSpeed`, `hueShift`.
- Пэддинги под статус/навигационные бары.

```kotlin
@Composable
fun MandelbrotBackgroundScreen() {
    var maxIterations by rememberSaveable { mutableIntStateOf(300) }
    var centerX by rememberSaveable { mutableFloatStateOf(-0.5f) }
    var centerY by rememberSaveable { mutableFloatStateOf(0f) }
    var scale by rememberSaveable { mutableFloatStateOf(2.8f) }
    var animationSpeed by rememberSaveable { mutableFloatStateOf(0.25f) }
    var hueShift by rememberSaveable { mutableFloatStateOf(0.1f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .onSizeChanged { containerSize = it }
            .pointerInput(containerSize) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val h = containerSize.height.coerceAtLeast(1)
                    centerX -= (pan.x / h) * scale
                    centerY -= (pan.y / h) * scale
                    scale = (scale / zoom).coerceIn(1e-4f, 10f)
                }
            }
    ) {
        MandelbrotBackground(
            modifier = Modifier.fillMaxSize(),
            maxIterations = maxIterations,
            centerX = centerX,
            centerY = centerY,
            scale = scale,
            animationSpeed = animationSpeed,
            hueShift = hueShift
        )

        // ControlPanel(...) — слайдеры итераций/зума/скорости/палитры
    }
}
```

---

## Производительность и анимация

- Для слабых устройств снижайте `maxIterations` (например, 150–220).
- `scale` влияет на «глубину» — чем меньше значение, тем больше деталей (но может потребоваться
  больше итераций для красивого градиента).
- Для плавной непрерывной анимации палитры добавьте `postInvalidateOnAnimation()` в `onDraw`.
- Если нужна статичная картинка — в `onDraw`: удалить postInvalidateOnAnimation()

---

## Известные ограничения AGSL

- Циклы: **только `for` с декларацией** (`for (int i = 0; ... )`); `while` не поддерживается.
- Функции `min`/`max` — типобезопасные: используйте `float` перегрузку с приведением типов.
- В `for` нельзя оставлять пустой init (`for (; ... )`) и нельзя инициализировать уже объявленную
  переменную (будет ошибка).
- Файл `.agsl` сохраняйте **без BOM** — иначе возможны ошибки парсера.

---

## FAQ: Как это работает?

**Не напрямую.** `RuntimeShader` исполняется внутри Skia/HWUI; на разных устройствах бэкендом может
быть **OpenGL ES** или **Vulkan**. Вы не управляете GL‑контекстом вручную — Android транслирует AGSL
в подходящий для устройства промежуточный формат (GLSL/SPIR‑V).

---

