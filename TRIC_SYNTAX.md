# Triton-C Syntax Guide

## Basic Language Features

### Comments and Structure
- **Comments**: Use semicolons `;` for comments (not `//` or `/* */`)
- **Line endings**: Newlines are significant, one statement per line
- **Whitespace**: Spaces are not significant
- **No semicolons**: No semicolons needed at end of statements

### Functions
```
func function_name(param1: type, param2: type): return_type {
    ; function body
}
```

- Functions can be defined in any order (no forward declarations needed)
- No function nesting allowed
- Return type is optional (defaults to `long`)
- Parameters are optional typing (defaults to `long`)
- Entry point is `main()` function

Example:
```
func main(): byte {
    return 100
}

func add(a: int, b: int): int {
    return a + b
}
```

## Type System

### Basic Types
- `long` - Default type, also acts as raw pointer
- `int` - Integer type
- `byte` - Byte type
- `T*` - Explicit pointer to type T
- `T[]` - Array notation (equivalent to `T*`, just stylistic choice)

### Type Inference and Declarations
```
var x: int = 5          ; explicit typing
var y = 10              ; inferred as long
var z = int* malloc(4)  ; explicit cast with inference
```

### Structs
- Structs can **only** be used as pointers (`Struct*`)
- Never use bare struct types (`Struct`)

```
struct TestStruct {
    var field1: long
    var field2: byte[]
    var field3: int*
}

func main(): byte {
    var s: TestStruct* = malloc(strideOf(TestStruct))
    s.field1 = 42
}
```

## Memory Management

### Memory Allocation
- `malloc(size)` - Returns `long` (raw pointer), must be cast to typed pointer
- `strideOf(Type)` - Compile-time size of type in bytes

```
var ptr: int* = malloc(4 * strideOf(int))  ; allocate array of 4 ints
var single: int* = malloc(strideOf(int))   ; allocate single int
```

### Pointer Operations

#### Dereferencing with `@`
```
var ptr: long* = malloc(strideOf(long))
@ptr = 42           ; write to memory
var value = @ptr    ; read from memory
var typed = int@ptr ; read as specific type
```

#### Array Access
- Only works with typed pointers (`T*`), not raw pointers (`long`)
- `ptr[index]` - scales by type size automatically

```
var arr: int* = malloc(4 * strideOf(int))
arr[0] = 10         ; array indexing
arr[1] = arr[0] + 5
```

#### Raw Pointer Arithmetic
- `ptr + 1` always adds 1 byte (no scaling)
- Use `@(ptr + offset)` for raw arithmetic

```
var ptr: byte* = malloc(8)
@(ptr + 6) = 7              ; write at offset 6
var val = byte@(ptr + 6)    ; read from offset 6
```

### Array Literals
Use `@` syntax to assign array literals:
```
var arr: int* = malloc(4 * strideOf(int))
@arr = [1, 2, 3, 4]  ; assign array literal to memory
```

## Variables and Scope

### Variable Declaration
```
var name: type = value  ; explicit typing
var name = value        ; type inference (defaults to long)
```

### Scope
- Variables are block-scoped
- `global` keyword for global variables

```
global g: int = 100  ; global variable, can be initialized with expressions

func main(): byte {
    var local = 42   ; block-scoped
    if (local > 0) {
        var nested = 10  ; nested block scope
    }
}
```

## Control Flow

### Conditional Statements
- Conditions can be any integer (0 = false, non-zero = true)

```
if (condition) {
    ; statements
} else if (other_condition) {
    ; statements  
} else {
    ; statements
}
```

### Loops
- Only `while` loops available
- No `continue` or `break` statements

```
while (condition) {
    ; loop body
}
```

## Operators

### Binary Operators
```
+   ; addition
-   ; subtraction  
*   ; multiplication
/   ; division
%   ; modulo
<   ; less than
<=  ; less than or equal
>   ; greater than
>=  ; greater than or equal
==  ; equality
!=  ; not equal
&&  ; logical AND
||  ; logical OR
&   ; bitwise AND
|   ; bitwise OR
^   ; bitwise XOR
<<  ; left shift
>>  ; right shift
>>> ; arithmetic right shift
```

### Unary Operators
```
-   ; negation
~   ; bitwise NOT
!   ; logical NOT
```

### Assignment
- Only basic assignment `=`
- No compound assignment operators (`+=`, `-=`, etc.)

## Type Casting
- Unchecked casting with `T expr` syntax (no parentheses)
- Can cast between any types

```
var x: long = 42
var y = byte x      ; cast long to byte
var ptr = int* x    ; cast long to int pointer
```

## Memory Layout Example

```
import memory

struct TestStruct {
    var a: long
    var b: byte[]
    var c: int[]*
}

func main(): byte {
    ; Allocate struct
    var s: TestStruct* = malloc(strideOf(TestStruct))
    if (s == 0) { return 0 }

    ; Set fields
    s.a = 0x0FBEADDEEFBEADDE
    
    ; Allocate and populate byte array
    s.b = malloc(8 * strideOf(byte))
    if (s.b == 0) { return 1 }
    @s.b = [1, 2, 3, 4]
    s.b[4] = 5
    s.b[5] = s.b[4] + 1
    @(s.b + 6) = 7
    s.b[7] = byte@(s.b + 6) + 1

    ; Allocate array of int pointers
    s.c = malloc(3 * strideOf(int[]))
    if (s.c == 0) { return 2 }
    
    ; Allocate child arrays
    var child1: int* = malloc(4 * strideOf(int))
    if (child1 == 0) { return 3 }
    @child1 = [0xFF, 0xAA, 0xFF, 0xAA]
    
    var child2 = int* malloc(4 * strideOf(int))
    if (child2 == 0) { return 4 }
    @child2 = [0xAA, 0xBB, 0xCC, 0xDD]
    
    ; Store pointers in array
    @s.c = [child1, child2, child1]
    s.c[0][0] = 0x0EADBEEF
    
    return 100
}
```

## Key Points to Remember

1. **Null pointer**: `0` represents null
2. **Memory safety**: Always check malloc return values
3. **Pointer types**: Use `T*` for typed pointers, `long` for raw pointers
4. **Dereferencing**: Always use `@` syntax
5. **Array access**: Only works with typed pointers (`T*`)
6. **Structs**: Always use as pointers (`Struct*`)
7. **No automatic memory management**: Manual malloc/free (if available)
8. **Function order**: Functions can be defined in any order
9. **Type defaults**: Everything defaults to `long` if not specified