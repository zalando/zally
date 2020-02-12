# Zally

## Levantar Sally Server
```
git clone https://github.com/zalando/zally.git
```
```
cd zally/server
```
```
./gradlew clean build 
```
```
./gradlew bootRun
```
El servidor estará corriendo cuando la terminal muestre un mensaje como este (no importa que salga 97%): 
Started ApplicationKt in 76.343 seconds (JVM running for 77.092)

## Usar Zally desde la línea de comandos
1. Levantar Zally Server
2. En otra terminal, desde el repositorio de zally correr:
```
cd cli/zally/
```
```
go build
```
3. Para ver los comandos usar:
```
./zally help
```
4. Para ver la lista de reglas:
```
./zally rules
```
5. Para usar zally
```
./zally lint <directorio de la definición de la API>
```

## Agregar reglas

### Crear una regla 

- Si se quiere crear solo una regla, se puede añadir al Ruleset de Zally sin necesidad de crear un Ruleset propio. 
- Crear un archivo kotlin con el nombre de la regla.
- Ejemplo de regla: 
```
package de.zalando.zally.ruleset.zally
 
import de.zalando.zally.core.toJsonPointer
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
 
@Rule(
   ruleSet = ZallyRuleSet::class,
   id = "1000",
   severity = Severity.SHOULD,
   title = "Should have 'FIF' on api title"
)
 
class FIFTitleRule {
   val description = "'FIF' should be on the api title"
 
   @Check(Severity.SHOULD)
   fun validate(context: Context): Violation? {
       val title = context.api.info?.title.toString()
       if (!(title.contains("FIF"))) {
           return context.violation("No 'FIF' on title", "/info/title".toJsonPointer())
       } else {
           return null
       }
   }
}
```
- OJO: no compilará ante cualquier error de sintaxis (espacios después de corchetes, debe haber una línea en blanco al final, etc)

### Crear un set de reglas 

OOF


## Ignorar reglas
x-zally-ignore: añadir a la descripción de la api para ignorar reglas.  
Se agrega en un array los números de las reglas a ignorar.
Ejemplo de uso:
```
swagger: '2.0'
x-zally-ignore: [215, 218, 219]
info:
  title: Some API
  version: '1.0.0'
  contact:
    name: Team X
    email: team@x.com
    url: https://team.x.com
paths: {}
```
