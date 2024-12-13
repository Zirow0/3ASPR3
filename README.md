# Опис програми

Ця програма обчислює попарні суми елементів великого одновимірного масиву двома паралельними методами: **Work Dealing** та **Work Stealing**.

## Задача

Знайти попарну суму елементів масиву за наступною формулою:

```
(a1 + a2) + (a2 + a3) + (a3 + a4) + ... + (an-2 + an-1) + (an-1 + an)
```

Кількість елементів масиву, а також значення його діапазону задає користувач. Час виконання обчислень замірюється для обох підходів.

---

## Структура програми

Програма використовує дві стратегічні методи обчислення:

1. **Work Dealing** – Завдання розділяються на окремі підзадачі, які обчислюються в окремих потоках.
2. **Work Stealing** – Завдання обчислюються паралельно з використанням `ForkJoinPool`, де завдання можуть "вкрадатися" іншими потоками для балансування навантаження.


## Пояснення до коду

### 1. **Генерація масиву**
```java
private static int[] generateRandomArray(int n, int start, int end)
```
Ця функція генерує випадкові значення в заданому діапазоні від `start` до `end`.

---

### 2. **Work Dealing**
```java
private static long executeWorkDealing(int[] array)
```
Метод розбиває масив на рівні частини між потоками. Кожен потік обчислює свою частину, а потім результати об'єднуються.

---

### 3. **Work Stealing**
```java
private static long executeWorkStealing(int[] array)
```
Тут використовується **ForkJoinPool**, щоб завдання обчислювалися динамічно та ефективно балансувалися між потоками.

## Висновок

- **Work Dealing**: Статичний розподіл роботи між потоками.
- **Work Stealing**: Динамічний механізм, де завдання ефективніше балансуються.
