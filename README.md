 Инструкции
------
### Сборка проекта
- Версия Java 1.8
- Версия Maven 4.0.0
- Зависимости
> #### postgresql 
    > version 42.5.0
> #### json-simple
    > version 1.1.1
- Плагины
> #### maven-assembly-plugin
    > version 3.4.2
### Запуск проекта
- для соединения с БД требуется заполнить файл application.properties
    > url
    
    > username
    
    > password
    
    > pool.size - размер пула соединений
    
- запуск через консоль с тремя параметрами
   - #### пример
        >  ##### java -jar AikamTestTask-1.0-SNAPSHOT-jar-with-dependencies.jar stat input.json output.json
        > первый аргумент - тип результата
        
        > второй - файл на вход
        
        > третй - файл на выход
        
