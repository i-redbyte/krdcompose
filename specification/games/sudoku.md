# Sudoku

## Обзор

Это простая, быстрая и настраиваемая реализация Судоку на Jetpack Compose. В основе — компонент
`SudokuBoard`, экран игры `SudokuGameScreen`, экран выбора сложности `DifficultyScreen` и
`SudokuViewModel`, управляющий состоянием без сохранения на диск (но с переживанием поворотов
экрана).

- Генератор `SudokuGenerator` создаёт валидную головоломку с **единственным решением** и разными
  уровнями сложности.
- Доступны **заметки (pencil marks)** с мгновенным обновлением и аккуратным центрированием цифр.
- Есть режим **таймера** с поражением по истечении времени.
- Компоненты спроектированы для **минимума лишних рекомпозиций**.

---

## 👁 Демонстрация

![Sudoku](/specification/games/img/sudoku.gif)

---

## Состав проекта

### Основные типы

```kotlin
enum class Difficulty { EASY, NORMAL, HARD }

data class SudokuPuzzle(
    val givens: IntArray,
    val solution: IntArray,
    val difficulty: Difficulty
) {
    fun clueCount(): Int = givens.count { it != 0 }
}
```

### Генератор

```kotlin
class SudokuGenerator(private val rng: Random = Random.Default) {
    fun generate(difficulty: Difficulty): SudokuPuzzle
    fun generateMany(difficulty: Difficulty, count: Int): List<SudokuPuzzle>
}
```

- Диапазоны подсказок (givens): EASY 38–47, NORMAL 30–37, HARD 24–29.
- Каждая сгенерированная доска проверяется на **единственность решения**.

### ViewModel

```kotlin
class SudokuViewModel : ViewModel() {
    val screenState: State<ScreenState>           // Menu | Game
    val generating: State<Boolean>                // Идёт генерация?
    val isTimedGame: State<Boolean>               // Режим таймера?
    val timeRemaining: State<Int>                 // Остаток времени (сек)
    val board: SnapshotStateList<Int>             // Текущее поле (0 — пусто)
    val notesState: State<List<Set<Int>>>         // Заметки для каждой клетки (1..9)
    val isCellEditableState: State<List<Boolean>> // Разрешена ли правка ячейки
    val selectedCell: State<Int>                  // Индекс выбранной ячейки (0..80 / -1)
    val noteMode: State<Boolean>                  // Режим заметок
    val gameStatus: State<GameStatus>             // Playing | Won | Lost
    val currentDifficulty: Difficulty

    fun startGame(difficulty: Difficulty, timed: Boolean)
    fun selectCell(index: Int)
    fun inputNumber(number: Int)
    fun erase()
    fun useHint()
    fun toggleNoteMode()
    fun exitToMenu()
}
```

Особенности:

- Заметки хранятся как `Set<Int>`. При любом изменении присваивается **новое множество** (
  `_notes[i] = old + x` или `old - x`), что гарантирует мгновенную рекомпозицию.
- При установке числа автоматически чистятся соответствующие заметки в строке, столбце и блоке 3×3.

---

## Компоненты UI

### Экран выбора сложности

```kotlin
@Composable
fun DifficultyScreen(onStartGame: (Difficulty, Boolean) -> Unit)
```

- Позволяет выбрать уровень сложности и включить/выключить таймер.
- Рекомендуется выравнивать элементы по ширине `widthIn(max = 420.dp)` и использовать
  `Card + Divider` для аккуратного списка.

### Экран игры

```kotlin
@Composable
fun SudokuGameScreen(viewModel: SudokuViewModel)
```

Состав:

- Таймер (если режим включён).
- `SudokuBoard`.
- Панель управления `SudokuControls` (цифры 1–9, «Заметки», «Стереть», «Подсказка»).
- Диалог победы/поражения с действиями «Новая игра» и «Выйти».

### Игровая доска

```kotlin
@Composable
fun SudokuBoard(
    board: List<Int>,                // 81 значение; 0 — пусто
    notes: List<Set<Int>>,           // 81 множество заметок (1..9)
    selectedIndex: Int,              // индекс выбранной клетки (-1, если нет)
    isCellEditable: List<Boolean>,   // 81 флаг доступности редактирования
    numberColors: List<Color>,       // 9 цветов для цифр 1..9 (индекс = digit-1)
    backgroundColor: Color,          // цвет фона поля
    gridLineColor: Color,            // цвет линий сетки
    onCellClick: (Int) -> Unit       // обработчик выбора клетки
)
```

Поведение:

- Тап по редактируемой клетке выбирает/снимает выделение.
- Рисование сетки и жирных линий блоков 3×3 — через `Canvas`.
- Контент ячейки анимируется через `AnimatedContent` с тремя состояниями: пусто, число, заметки.
- Заметки рендерятся строго по центру мини-клеток 3×3 с помощью `Canvas` и метрик шрифта: каждая
  мини-цифра — в точном центре своей области, без смещений.

Пример подключения:

```kotlin
SudokuBoard(
    board = viewModel.board,
    notes = viewModel.notesState.value,
    selectedIndex = viewModel.selectedCell.value,
    isCellEditable = viewModel.isCellEditableState.value,
    numberColors = listOf(
        Color(0xFF1976D2), Color(0xFF388E3C), Color(0xFFF57C00),
        Color(0xFF7B1FA2), Color(0xFFFBC02D), Color(0xFFE64A19),
        Color(0xFF0097A7), Color(0xFFD32F2F), Color(0xFF303F9F)
    ),
    backgroundColor = Color.White,
    gridLineColor = Color.Black,
    onCellClick = { index -> viewModel.selectCell(index) }
)
```

### Панель управления

```kotlin
@Composable
fun SudokuControls(
    onNumberClick: (Int) -> Unit,
    onDelete: () -> Unit,
    onHint: () -> Unit,
    onToggleNote: () -> Unit,
    noteMode: Boolean
)
```

- Кнопки 1–9 — квадратные, скругление 8.dp, тёмно-зелёный фон, цифры по центру.
- Кнопки «Заметки», «Стереть», «Подсказка» — в одну строку, тёмно-зелёные, одинаковой высоты.

---

## Правила игры

### Базовые правила судоку

- Поле 9×9 нужно заполнить цифрами 1–9.
- В каждой строке цифры не повторяются.
- В каждом столбце цифры не повторяются.
- В каждом блоке 3×3 цифры не повторяются.
- Изначальные цифры (givens) нельзя изменять.

### Правила конкретной реализации

- Выбор клетки: тап по пустой редактируемой клетке.
- Режим «Заметки»: при включении нажатия 1–9 добавляют/убирают мини-цифры в выбранной пустой клетке.
  Это ваши подсказки, они не влияют на проверку.
- Режим обычного ввода: при выключенных заметках кнопки 1–9 записывают число в клетку.
- «Стереть»: очищает выбранную клетку и её заметки.
- «Подсказка»: заполняет корректным числом выбранную пустую клетку (или первую доступную пустую) и
  делает её нередактируемой.
- Победа: когда все клетки заполнены и совпадают с эталонным решением.
- Поражение: только при включённом таймере, по истечении времени.
- Таймер по умолчанию: EASY — 10 мин, NORMAL — 15 мин, HARD — 20 мин.

---

## Индексация и координаты

- Клетки нумеруются от 0 до 80 слева направо, сверху вниз.
- `row = index / 9`, `col = index % 9`.
- Блок 3×3: верхний-левый индекс блока для клетки `(row, col)` — `((row/3)*3, (col/3)*3)`.

---

## Кастомизация вида

- `numberColors: List<Color>` — цвета для цифр 1..9. Индекс — `digit-1`.
- `backgroundColor` — цвет фона поля (за ячейками).
- `gridLineColor` — цвет линий сетки.
- Выделение выбранной клетки можно менять через фон конкретной ячейки (см. `cellBg`).

Подсказки по стилям:

- Для читаемости: основное число — 18–22 sp, заметки — 9–11 sp.
- Контрасты: числа гивенов — полужирные; пользовательские — обычные.

---

## Производительность и архитектура

- Состояние разбито по спискам `mutableStateListOf`: `board`, `notes`, `isCellEditable`.
- Для заметок используется **иммутабельный подход** на уровне элемента: при любом изменении
  присваивается **новый `Set`**, что даёт точечные рекомпозиции ячеек.
- В `LazyVerticalGrid` используются стабильные `key = { idx, _ -> idx }`.
- Анимация контента ячеек — через `AnimatedContent`, но состояние контента вычисляется синхронно в
  композиции (без лишнего `remember`), чтобы изменения были мгновенными.
- Таймер работает в `viewModelScope`, корректно завершается при победе/поражении.

Рекомендации:

- Не храните внутри `@Composable` больших структур — только ссылки на `State` из VM.
- Избегайте мутаций коллекций заметок на месте — всегда присваивайте новое множество.

---

## Пример быстрого запуска

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val vm: SudokuViewModel = viewModel()
                when (vm.screenState.value) {
                    ScreenState.Menu -> DifficultyScreen { diff, timed ->
                        vm.startGame(
                            diff,
                            timed
                        )
                    }
                    ScreenState.Game -> SudokuGameScreen(vm)
                }
            }
        }
    }
}
```
