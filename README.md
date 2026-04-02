# searchLib

Библиотека для индексации текстовых файлов по словам.

## Что умеет

- Добавлять файл или директорию в индекс.
- Искать файлы по слову.
- Удалять файл/директорию из индекса.
- Автоматически реагировать на изменения файлов и каталогов.
- Поддерживать расширяемую токенизацию через интерфейс Tokenizer.

## Технологии

- Java 21
- Maven
- JUnit 5

## Требования к окружению

1. Установить Java 21.
2. Установить Maven 3.9+.
3. Проверить версии:

```bash
java -version
mvn -version
```

## Сборка проекта и запуск

```bash
mvn clean package
```

```bash
java -cp target/classes org.example.Main
```

После запуска доступны команды:

- add <path> - добавить файл или директорию в индекс
- remove <path> - удалить файл или директорию из индекса
- search <word> - найти файлы по слову
- help - показать список команд
- exit - выйти

Пример сессии:

```text
> add /home/user/docs
Добавлено: /home/user/docs
> search java
/home/user/docs/notes.txt
> remove /home/user/docs
Удалено: /home/user/docs
> exit
```

Если путь не существует или указан файл не .txt, команда add/remove вернет сообщение об ошибке.

## Запуск тестов

```bash
mvn test
```

## Структура

- src/main/java/org/example/library/SearchLibrary.java - внешний класс, представляющий API библиотеки
- src/main/java/org/example/service/IndexServiceImpl.java - индексация и поиск
- src/main/java/org/example/watcher/FileWatcher.java - отслеживание изменений файлов на диске
- src/main/java/org/example/storage/IndexStorageImpl.java - потокобезопасное хранилище индекса
- src/main/java/org/example/token/Tokenizer.java - интерфейс токенизации
- src/main/java/org/example/token/SimpleWhiteSpaceTokenizer.java - базовая реализация токенизатора
- src/main/java/org/example/Main.java - консольное приложение, взаимодействующее с библиотекой

