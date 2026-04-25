# Notas de Refactorización de la Gramática

## grammar1.txt

Normalización de nombres para mejorar legibilidad y tooling.

- **No Terminales (Vn):** comienzan con mayúscula.
- **Terminales (Vt):** comienzan con minúscula o son símbolos.

---

## grammar2.txt

Eliminación del operador opcional de EBNF (`?`) a BNF puro.

Desde:

- `A ::= B?`

Hacia:

- `A ::= B | λ`

---

## grammar3.txt

Eliminación del operador de repetición de EBNF (`*`) mediante recursión por derecha.

Desde:

- `A ::= X Y*`

Hacia:

- `A ::= X YList`
- `YList ::= Y YList | λ`

---

## grammar4.txt

Se aplicó factorización directa.

Se aplicó factorización indirecta, esto es, factorizar reglas que incluían prefijos comunes a través de sus no
terminales.

Las reglas anulables se unifican con sufijos estables: `Opt`, `List`, `Tail`.

Muchas reglas con encadenado (`Chaining`) competían en la tabla. La decisión fue ligar el encadenado a la expresión
más interna (más a la derecha), que es el comportamiento esperado en el lenguaje. Para ello se tuvo que revisar en
qué situaciones el encadenado era ambiguo, es decir, no podía distinguirse entre un encadenado externo o interno, ya
que ambos de ellos producían reglas λ.

### Casos de Refactorización

#### 1) Normalización de anulables y listas

Para evitar variantes repetidas de una misma forma, se normalizan auxiliares.

Antes:

```bnf
CurrentArguments ::= ( ExpsList ) | ( )
ExpsList ::= Exp , ExpList
```

Después:

```bnf
CurrentArguments ::= ( ExpsList )
ExpsList ::= Exp ExpsTail | λ
ExpsTail ::= , Exp ExpsTail | λ
```

---

#### 2) Eliminación de ambigüedad en Operand

Antes:

```bnf
Operand ::= Literal | Primary ChainingOpt
```

Después:

```bnf
Operand ::= Literal | Primary
```

Esto fue posible ya que **todas** las producciones de `Primary` incluían `ChainingOpt` en sus derivaciones, por lo que
dejar ambos duplicaba caminos de parseo, y por lo tanto introducía ambigüedad.

```bnf
Operand ::= Literal | Primary ChainingOpt
Primary ::= A ChainingOpt | B ChainingOpt ...
```

---

#### 3) Factorización de prefijo compartido en primarios con identificador

`VarsAccess` y `MethodCall` empezaban igual (`idMetAt ...`). Eso generaba conflicto FIRST/FIRST, particularmente
porque ambos son producciones de `Primary`:

Antes:

```bnf
Primary ::= VarsAccess | MethodCall | ...
VarsAccess ::= idMetAt ...
MethodCall ::= idMetAt ...
```

Después:

```bnf
Primary ::= IdPrimary | ...
IdPrimary ::= idMetAt IdPrimaryTail
IdPrimaryTail ::= ...
```

Con esto, la decisión se difiere al token siguiente (`(`, `[` o nada), particular para cada caso. Si el conflicto
hubiera reaparecido en un estadío posterior, se hubiera aplicado la misma técnica.

---

#### 4) Simplificación de llamada estática

Antes había una expansión anidada vía `MethodCall`, que sumaba ambigüedad estructural.

Antes:

```bnf
MethodCall ::= idMetAt CurrentArguments ChainingOpt
StaticMethodCall ::= idclass . MethodCall ChainingOpt
```

Después:

```bnf
StaticMethodCall ::= idclass . idMetAt CurrentArguments ChainingOpt
```

Si esto no se hacía se introducía una ambigüedad en un estadío posterior.

---

##### Resultado final

La tabla LL(1) presenta un único conflicto, particularmente en la regla

```bnf
ElseOpt ::= else Sentence | λ
```

Corresponde al famoso *dangling else*, y se prefiere resolver bajo convención dentro del análisis semántico antes
que con una modificación de la gramática (según internet, más complicado).
