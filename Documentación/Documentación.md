# Documentación Proyecto 3 Seguridad del Software

## Pablo Mesén - 2023071259 y Alonso Durán - 2023044780

## 1. ¿Descubrió OpenJML todos los posibles problemas? ¿Es el código correcto después de los cambios?

### Archivo `Bag.java`

OpenJML descubrió con precisión todos los errores de seguridad de memoria y de lógica de tamaño que el compilador de Java no detecta estáticamente. Los bugs identificados y corregidos fueron:

| # | Ubicación | Error original | Descripción | Solución |
|---|---|---|---|---|
| 1 | `removeOnce`, `removeAll`, `getCount` | Condición de bucle `i <= n` | El ciclo permitía que `i` alcanzara el valor `n`, provocando un acceso a `contents[n]`, posición que está fuera de los límites válidos del arreglo. | Cambiar la condición del ciclo de `i <= n` a `i < n` para recorrer únicamente las posiciones válidas del arreglo. |
| 2 | `add(int)` | Creación de arreglo con `new int[2*n]` | Cuando el bag estaba vacío (`n == 0`), se creaba un arreglo de tamaño 0, impidiendo almacenar nuevos elementos. | Cambiar la asignación a `new int[2*n + 1]` para garantizar que siempre exista al menos una posición disponible. |
| 3 | `add(Bag)` | Desplazamiento incorrecto `n + 1` en `arraycopy` | La copia comenzaba una posición después de donde debía, dejando un hueco en la posición `n` y provocando escrituras fuera de los límites del arreglo. | Corregir el desplazamiento de copia, reemplazando `n + 1` por `n` en la llamada a `arraycopy`. |
| 4 | `add(Bag)` | No se actualizaba `n` después de copiar los elementos | Aunque los elementos de la segunda bolsa se copiaban correctamente, el tamaño lógico de la bolsa no se incrementaba, dejando los elementos añadidos invisibles para operaciones posteriores. | Actualizar el tamaño lógico de la bolsa después de la copia mediante `n = n + b.n`. |
| 5 | `Bag(Bag)` | Campos sin inicializar antes de llamar a `this.add(b)` | El constructor invocaba `add(b)` cuando `contents` aún era `null`, lo que podía causar errores durante la copia de elementos. | Inicializar la bolsa antes de la llamada a `add(b)` con `n = 0` y `contents = new int[0]`. |

Sin embargo, hay al menos un problema lógico que OpenJML no detectó con las especificaciones utilizadas:

`removeAll` tiene un fallo de lógica funcional. Cuando el intercambio `contents[i] = contents[n]` coloca un elemento igual a `elt` en la posición `i`, el índice `i` avanza de todas formas en la siguiente iteración, saltándose ese duplicado. Es posible que tras `removeAll(3)` sobre `{3, 5, 3}` quede un `3` sin eliminar `{3, 5}`. OpenJML no lo detectó porque la postcondición especificada (`n <= \old(n)`) sólo habla del tamaño, no de la ausencia del elemento. Para capturarlo habría que especificar `ensures getCount(elt) == 0`, pero OpenJML no soporta `\num_of`, lo que impidió escribir esa postcondición funcional completa.

Asimismo, OpenJML no comprueba propiedades relacionadas con el comportamiento dinámico del programa (por ejemplo, que en `removeOnce` siempre se garantice encontrar el elemento si existe).

El código es correcto para los usos habituales dentro de los límites de capacidad declarados (`n <= Integer.MAX_VALUE / 2`), pero no se puede afirmar con plena certeza que sea funcionalmente completo, porque la especificación de `removeAll` es más débil de lo deseable.

---

### Archivo `Amount.java`

OpenJML identificó correctamente todos los errores de lógica aritmética que el compilador de Java no detecta estáticamente. Los bugs identificados y corregidos fueron:

| # | Ubicación | Error original | Descripción | Solución |
|---|---|---|---|---|
| 1 | `negate()` | Parámetros invertidos | Se pasaban `-euros` y `-cents` en orden incorrecto al constructor, produciendo objetos con signo erróneo. | Corregir el orden de los parámetros en la llamada a `new Amount`. |
| 2 | `add()` | Operador incorrecto en la segunda condición | Se restaba 1 a `new_euros` cuando debía sumarse al convertir el exceso de cents a euros positivos. | Cambiar `-1` por `+1` en la actualización de `new_euros`. |
| 3 | `add()` | Límites con `<` y `>` en vez de cubrir los valores exactos | Las condiciones de desbordamiento no cubrían los valores exactos `±100`, dejando representaciones inválidas como `Amount(n, 100)` sin normalizar. | Ajustar las condiciones de los primeros dos `if` para usar `< -99` y `> 99`. |
| 4 | `add()` | Condición no estricta en el cuarto `if` | Usar `new_cents >= 0` en vez de `new_cents > 0` causaba correcciones innecesarias cuando el resultado era `(0€, 0¢)`, produciendo `(1€, -100¢)`. | Cambiar a `new_cents > 0` para evitar modificar el caso neutro. |

Sin embargo, hay al menos un problema que OpenJML no detectó con las especificaciones utilizadas: la especificación de `subtract()` incluye el `ensures` `\result.euros == this.euros - a.euros || \result != null`, que es trivialmente verdadero por su segunda condición, por lo que no aporta ninguna garantía funcional real sobre el resultado de la resta.

Además, OpenJML no verifica propiedades funcionales más complejas como la conmutatividad (`a.add(b).equal(b.add(a))`) o la asociatividad, ya que eso requeriría postcondiciones sobre el valor exacto del resultado que van más allá de las anotaciones utilizadas.

El código es correcto para los usos habituales dentro de los límites declarados en los invariantes (`cents >= -99 && cents <= 99`, `euros` dentro de `±1000000000`), y las especificaciones son suficientes para que OpenJML verifique la consistencia de la representación interna.

## 2. Sugerencias de mejora para OpenJML y JML

### Para OpenJML (la herramienta)

Durante esta práctica no fue posible especificar correctamente algunas propiedades relacionadas con el conteo de elementos porque `OpenJML` no soporta completamente `\num_of` en su proceso de verificación. Mejorar este soporte permitiría escribir especificaciones más precisas y detectar más errores lógicos.

Mensajes de error más orientados al usuario. Errores como `"Implicit references to 'this' are not permitted in constructor assignable clauses"` o `"Not yet supported feature: \num_of"` aparecen sólo en tiempo de verificación, no como advertencias del editor. Un plugin de IDE que marque las anotaciones problemáticas en tiempo real reduciría drásticamente el ciclo de corrección.

### Para JML

Sintaxis más sencilla en los constructores. La distinción entre `assignable \nothing` (correcto en constructores) y `assignable field` (incorrecto porque hay un `this` previo) es contraintuitiva y no está bien documentada. Una palabra más intuitiva como `initializes contents, n` sería más claro.

Formas más simples de expresar conteos. Especificar propiedades relacionadas con la cantidad de veces que aparece un elemento en un arreglo debería ser más sencillo. Actualmente, las limitaciones de `\num_of` obligan a escribir especificaciones menos completas, reduciendo la capacidad de verificar ciertos comportamientos del programa.


## 3. Alternativas a OpenJML para encontrar los mismos problemas

Una alternativa a OpenJML sería combinar pruebas unitarias con JUnit, herramientas de análisis estático como Semgrep, SpotBugs y una revisión manual del código. Las pruebas unitarias permitirían probar distintos casos, por ejemplo una bolsa vacía, elementos duplicados o intentos de eliminar elementos que no existen. Por su parte, las herramientas de análisis estático pueden ayudar a encontrar algunos errores comunes sin necesidad de escribir especificaciones formales, mientras que la revisión de código permite detectar problemas de lógica que pueden pasar desapercibidos para las herramientas automáticas.

### 3.1 ¿Encontraría más, menos o igual cantidad de problemas?

Probablemente encontraría una cantidad similar de problemas, aunque dependería de qué tan completas sean las pruebas. Los errores relacionados con accesos fuera de rango o con arreglos mal dimensionados podrían detectarse fácilmente al ejecutar los casos de prueba adecuados. Sin embargo, algunos errores lógicos más específicos, como el comportamiento de removeAll cuando existen elementos duplicados, podrían pasar desapercibidos si no se prueba exactamente ese escenario.


### 3.2 ¿Encontraría los problemas antes o después que OpenJML?

En la mayoría de los casos los encontraría después. OpenJML analiza el código sin necesidad de ejecutarlo, por lo que puede detectar ciertos errores apenas se verifica el programa. En cambio, las pruebas unitarias sólo encuentran errores cuando se ejecutan los casos de prueba correspondientes. Si un caso no fue considerado por el programador, el error podría permanecer oculto durante mucho tiempo.


### 3.3 ¿Requeriría más o menos trabajo?

Escribir pruebas unitarias requiere menos trabajo al inicio que escribir especificaciones completas en JML. Gran parte del tiempo de esta práctica se dedicó a entender cómo formular invariantes, precondiciones y postcondiciones para que OpenJML pudiera verificarlas correctamente. Además se demoró mucho en realizar la implementación ya que arreglar un error podía producir más en Open JML solo en el pequeño código del proyecto, aplicar JML en un file con muchas líneas de código requiere un trabajo muy tedioso y riguroso. Sin embargo, las pruebas también requieren mantenimiento y es necesario seguir agregando casos a medida que el programa evoluciona.

### 3.4 ¿Proveería más o menos confianza sobre la seguridad del código resultante?

Consideramos que proporcionaría menos confianza. Las pruebas permiten verificar que el programa funciona correctamente para los casos que se ejecutan, pero no garantizan que funcione bien para todas las situaciones posibles. OpenJML, en cambio, intenta demostrar que ciertas propiedades se cumplen para cualquier ejecución que respete las precondiciones definidas. Aunque esto requiere más esfuerzo, ofrece una mayor seguridad sobre la corrección del código.