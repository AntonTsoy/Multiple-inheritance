# Multiple inheritance in Java using annotations
## Running
- First, you need `JDK 23`
- Write you hierarchy
- After some interface (e.g. `IPerson`) annotated with `@InheritanceRoot` run `./gradlew clean build` (clean may be excluded) to generate root abstract class (e.g. `IPersonRoot`)
- All descendants must extend root class and be annotated by `@ExtendsMultiple({Ancestor1.class, ...})`
- After that you can call `super.next<MethodName>()` in your descendants to invoke their ancestors

## ТЗ
### Пояснение
Гомогенная иерархия означает, что у нее есть единый корень (интерфейс), и все операции над иерархией определяются им. Должно работать полноценное комбинирование методов, как в CLOS (включая обход «соседей»). Можно считать, что все свойства приватные.

### Рекомендуемая схема реализации
- В качестве корня иерархии следует использовать обычный Java-интерфейс, под видом которого будут использоваться все объекты классов иерархии
- Реализуйте аннотацию для корневого интерфейса и ее обработку, в результате которой будет генерироваться вспомогательный класс – фактический корень иерархии с точки зрения Java-машины. Например, для ISomeInterface можно сгенерировать ISomeInterfaceRoot.
- Для каждой операции из корневого интерфейса должен быть сгенерирован дополнительный c такой же сигнатурой, который будет играть роль call-next-method в CLOS. Например, для someMethod можно сгенерировать nextSomeMethod
- Все классы иерархии явно наследуются (с точки зрения Java) от сгенерированного класса ISomeInterfaceRoot, а фактические суперклассы передаются через аннотацию.
- Само множественное наследование и диспетчеризация в нем обеспечивается через композицию (можно либо инструментировать аннотированные классы иерархии, либо сгенерировать дополнительные)
