# Triton-64 Virtual Machine

A complete 64-bit virtual machine implementation featuring a custom CPU architecture, assembler, compiler, and development environment built in Java with JavaFX.

## Overview

The Triton-64 VM is a comprehensive virtual machine system that includes:

- **Custom 64-bit CPU Architecture**: Complete instruction set with 32 registers
- **TriC Programming Language + Compiler**: High-level language that compiles to Triton-64 assembly
- **Assembler**: Converts assembly code to machine code with macro expansion
- **Memory Management**: Sophisticated memory mapping with ROM, RAM, MMIO, and framebuffer regions
- **Visual Debugging Tools**: Real-time CPU and memory viewers
- **ROM System**: Bootable ROM with integrated assembly code

## Architecture

### CPU Specifications

- **Architecture**: 64-bit RISC-style processor
- **Registers**: 32 general-purpose 64-bit registers
- **Instruction Set**: 32-bit fixed-width instructions
- **Memory Model**: Unified 64-bit address space
- **Execution Model**: Sequential execution with conditional jumps

### Register Layout

| Alias | Numeric | Purpose |
| --- | --- | --- |
| `ra` | `r0` | Return address |
| `sp` | `r1` | Stack pointer |
| `fp` | `r2` | Frame pointer |
| `gp` | `r3` | Global pointer |
| `hp` | `r4` | Heap pointer |
| `s0-s9` | `r5-r14` | Saved registers |
| `a0-a6` | `r15-r21` | Argument registers |
| `t0-t8` | `r22-r30` | Temporary registers |
| `t9` | `r31` | **Reserved for assembler use** |

### Memory Layout

```
0x0000_0000_0000_0000  ┌─────────────────┐
                       │      ROM        │  128 KB
0x0000_0000_0002_0000  ├─────────────────┤
                       │      RAM        │  512 MB
0x0000_0000_2002_0000  ├─────────────────┤
                       │      MMIO       │  2 MB
0x0000_0000_2022_0000  ├─────────────────┤
                       │  Framebuffer    │  16 MB
0x0000_0000_2122_0000  └─────────────────┘
```

## Instruction Set Architecture

### Core Instructions

#### Special Operations

- `NOP` - No operation
- `HLT` - Halt CPU execution

#### Data Movement

- `MOV rdest, rsrc` - Copy register value
- `LDI rdest, imm10` - Load 10-bit signed immediate (-512 to +511)
- `LD rdest, rsrc` - Load from memory address
- `ST rdest, rsrc` - Store to memory address

#### Arithmetic Operations

- `ADD rdest, rsrc1, rsrc2` - Addition
- `SUB rdest, rsrc1, rsrc2` - Subtraction
- `MUL rdest, rsrc1, rsrc2` - Multiplication
- `DIV rdest, rsrc1, rsrc2` - Division
- `NEG rdest, rsrc` - Arithmetic negation

#### Bitwise Operations

- `AND rdest, rsrc1, rsrc2` - Bitwise AND
- `OR rdest, rsrc1, rsrc2` - Bitwise OR
- `XOR rdest, rsrc1, rsrc2` - Bitwise XOR
- `NOT rdest, rsrc` - Bitwise NOT
- `SHL rdest, rsrc1, rsrc2` - Logical shift left
- `SHR rdest, rsrc1, rsrc2` - Logical shift right
- `SAR rdest, rsrc1, rsrc2` - Arithmetic shift right

#### Control Flow

- `JMP rdest` - Unconditional jump to register address
- `JZ rdest, rsrc` - Jump if zero
- `JNZ rdest, rsrc` - Jump if not zero
- `JPP rdest, rsrc` - Jump if positive
- `JPN rdest, rsrc` - Jump if negative
- `JAL rdest, rsrc` - Jump and link (store return address)

### Pseudo-Instructions

The assembler provides powerful macro expansions for common operations:

#### Label Operations

```assembly
loop:               ; Define jump target
JMP loop           ; Jump to label (expands to address loading)
JZ loop, r0        ; Conditional jump to label
```

#### Extended Immediate Loading

```assembly
LDI r0, 0x123456789ABCDEF  ; 64-bit immediate (multi-instruction expansion)
LDIU r0, 0x123456789ABCDEF ; Unsafe fast load (clobbers temps)
```

#### Stack Operations

```assembly
PUSH r0, r1, r2    ; Push multiple registers to stack
POP r0, r1, r2     ; Pop multiple registers from stack
```

#### Immediate ALU Operations

```assembly
ADD r0, r1, 42     ; Add immediate (expands to temp + register operation)
SUB r0, r1, 100    ; Subtract immediate
```

## TriC Programming Language

TriC is a high-level programming language designed specifically for the Triton-64 Virtual Machine. It provides a more accessible way to write programs for the VM compared to writing raw assembly code. TriC supports modern programming constructs while being mindful of the underlying VM architecture.

### Language Features

- **Variables**: Declare and use variables with automatic stack management. Currently, only integer types are supported.
- **Functions**: Define functions with parameters and return values, following the Triton-64 calling convention.
- **Control Flow**: Use `if-else` statements and `while` loops for conditional and repetitive execution.
- **Expressions**: Write complex expressions with arithmetic, logical, and comparison operators.
- **Function Calls**: Call functions with up to 7 arguments, adhering to the register-based calling convention.
- **Dereferencing**: Access memory locations using pointers (@ keyword).
- **Inline Assembly**: Embed raw assembly code within TriC programs for low-level operations.

### Syntax Examples

Here are some snippets demonstrating TriC's syntax:

#### Variable Declaration and Assignment

```tric
var x = 10
var y = 20
var sum = x + y
```

#### Function Definition

```tric
func add(a, b) {
    return a + b
}
```

#### If-Else Statement

```tric
if (x > y) {
    z = x - y
} else {
    z = y - x
}
```

#### While Loop

```tric
var i = 0
while (i < 10) {
    i = i + 1
}
```

#### Function Call

```tric
var result = add(5, 3)
```

For a more comprehensive example, see the Example TriC Program below.

### Compiler Overview

The Tri-C compiler is responsible for translating TriC source code into Triton-64 assembly code. It consists of several components:

- **Lexer**: Breaks the source code into tokens.
- **Parser**: Analyzes the tokens to build an Abstract Syntax Tree (AST) representing the program's structure.
- **Code Generator**: Traverses the AST to generate corresponding assembly code, handling register allocation, stack management, and control flow.

The compiler is designed to produce efficient assembly code while maintaining readability and debuggability.

### Integration with Triton-64 VM

TriC programs are compiled to assembly code, which can then be assembled and run on the Triton-64 VM. The compiler ensures that the generated code adheres to the VM's architecture, including register usage and memory management.

When writing TriC programs, developers should be aware of the following:

- Variables are stored on the stack, with automatic management of stack frames for functions.
- Functions use the `ra` register for return addresses and `a0` for return values.
- The stack pointer (`sp`) and frame pointer (`fp`) are managed according to the VM's conventions.

### Development and Debugging

While developing TriC programs, you can leverage the VM's debugging tools:

- **CPU Viewer**: Monitor register states and program counter during execution.
- **Memory Viewer**: Inspect memory contents, including the stack and heap.

Additionally, the compiler can generate debug information to map assembly instructions back to TriC source code lines, aiding in debugging.

### Example TriC Program

Below is a complete TriC program that demonstrates various language features:

```tric
; Triton-C Feature Showcase
; Demonstrates variables, functions, control flow, and arithmetic

; Main program
func main() {
    ; Variable declarations
    var x = 5
    var y = 10
    var z = 0

    ; If-else demonstration
    if (x < y) {
        z = y - x
    } else {
        z = x - y
    }

    ; While loop demonstration
    var counter = 0
    while (counter < 5) {
        counter = counter + 1
    }

    ; Function calls
    var fact = factorial(5)  ; 120

    ; Arithmetic operations
    var sum = x + y
    var product = x * y
    var factorial = factorial(4)  ; 24
    
    ; Memory operations
    var a = -1 ; test value
    var fb_start = 0x20220000
    var fb_size  = 0x10000 ; 64 KiB frame buffer size

    @fb_start = a      ; store the value 0xDEADBEEF at the address stored in a, which is 539099136

    var offset = fb_start;
    var i = 0

    while (i < 100) {
        a = a - 1
        while (offset < fb_size + fb_start) {
            @offset = a             ; store the value 0xDEADBEEF at the address fb_start + offset
            offset = offset + 8     ; increment offset by 4 bytes
        }
        offset = fb_start ; reset offset to the start of the frame buffer
        i = i + 1
    }


    ; Return the total of all operations
    return sum + product + fact + z  ; 15 + 50 + 120 + 5 = 190 stored in a0
}

; Function with return value
func factorial(n) {
    if (n <= 1) {
        return 1
    } else {
        return n * factorial(n - 1)
    }
}
```

This program showcases variable usage, control flow, function definitions, recursive function calls and memory operations. After execution, the result (190) is stored in the `a0` register.

## System Components

### Assembler (`org.lpc.assembler.Assembler`)

The assembler performs sophisticated two-pass assembly:

1. **First Pass**: Symbol resolution and size calculation
2. **Pseudo-instruction Expansion**: Converts macros to native instructions
3. **Second Pass**: Machine code generation

Key features:

- **Symbol Table**: Global label resolution
- **Macro System**: Powerful pseudo-instruction expansion

### Compiler (`org.lpc.compiler.TriCCompiler`)

The Tri-C compiler is a crucial component that translates TriC source code into Triton-64 assembly. It follows a standard compilation pipeline:

1. **Lexical Analysis (**`Lexer`**)**: Converts the source code into a stream of tokens.
2. **Parsing (**`Parser`**)**: Constructs an Abstract Syntax Tree (AST) from the tokens, validating the syntax.
3. **Code Generation (**`CodeGenerator`**)**: Walks the AST to emit assembly code, managing registers, stack frames, and control flow.

Key features of the compiler include:

- **Register Management**: Efficient allocation and deallocation of temporary registers.
- **Stack Management**: Automatic handling of stack frames for functions and local variables.
- **Control Flow Generation**: Proper generation of jumps and labels for `if-else` and `while` statements.
- **Function Handling**: Support for function calls with parameter passing and return values.

The compiler is designed to be modular, with each component responsible for a specific phase of compilation, making it easier to maintain and extend.

### Memory System (`org.lpc.memory.Memory`)

Sophisticated memory management with:

- **Region Protection**: ROM write protection
- **Address Validation**: Bounds checking for all accesses
- **Endianness Handling**: Little-endian byte ordering
- **MMIO Support**: Memory-mapped I/O simulation

### CPU (`org.lpc.cpu.Cpu`)

The CPU core provides:

- **Instruction Execution**: Full ISA implementation
- **Register Management**: 32 64-bit general-purpose registers
- **Control Flow**: Program counter management with jumps

## Development Tools

### Visual Debuggers

The system includes JavaFX-based debugging tools:

#### CPU Viewer (`org.lpc.visual.CpuViewer`)

- Real-time register display
- Program counter tracking
- Execution status monitoring

#### Memory Viewer (`org.lpc.visual.MemoryViewer`)

- Memory region visualization
- Hexadecimal dump display
- Address-based navigation
- Real-time memory updates

### ROM Development (`org.lpc.rom.RomDumper`)

The ROM system allows bootable code development:

1. Write assembly code in `rom.asm`
2. Run `RomDumper` to generate `ROMData.java`
3. Boot ROM code executes automatically on VM startup

## Project Structure

```
org.lpc/
├── Main.java                           # Application entry point
├── assembler/
│   ├── Assembler.java                  # Main assembler with pseudo-instruction support
│   ├── Expander.java                   # Pseudo-instruction expansion engine
│   ├── Parser.java                     # Assembly syntax parsing
│   ├── Preprocessor.java               # Source code preprocessing
│   └── SymbolTable.java                # Label and symbol management
├── compiler/
│   ├── TriCCompiler.java               # Main compiler interface
│   ├── Lexer.java                      # Tokenization
│   ├── Parser.java                     # AST generation
│   ├── CodeGenerator.java              # Assembly emission
│   ├── CodeGenContext.java             # Context for code generation
│   ├── codegen/                        # Code generation utilities
│   │   ├── FunctionManager.java        # Manages function generation and calling conventions
│   │   ├── StackManager.java           # Handles stack operations and variable storage
│   │   ├── InstructionEmitter.java     # Emits assembly instructions
│   │   ├── RegisterManager.java        # Manages register allocation
│   │   ├── CodeGenContext.java         # Manages asm context during code generation
│   │   └── ConditionalGenerator.java   # Generates conditional control flow
│   └── ast/                            # Abstract syntax tree nodes
├── cpu/
│   ├── Cpu.java                        # CPU core implementation
│   ├── InstructionSet.java             # ISA definition and encoding
│   ├── InstructionInfo.java            # Instruction metadata
│   └── RegisterInfo.java               # Register naming and aliases
├── io/
│   ├── IODeviceManager.java            # Manages I/O devices
│   ├── IODevice.java                   # Interface for I/O devices
│   └── devices/                        # Device-specific implementations
├── memory/
│   ├── Memory.java                     # Memory management system
│   └── MemoryMap.java                  # Address space layout
├── rom/
│   ├── RomDumper.java                  # ROM generation utility
│   └── ROMData.java                    # Generated ROM data (auto-generated)
└── visual/
    ├── CpuViewer.java                  # CPU state visualization
    ├── MemoryViewer.java               # Memory dump visualization
    ├── MemoryPrinter.java              # Memory content printing
    ├── TextModeViewer.java             # Visualiser for framebuffer interpreted as text
    ├── PixelModeViewer.java            # Visualiser for framebuffer interpreted as pixels
    └── style/                          # Styles for visual components
```

## Usage Examples

### Basic Assembly Program

```assembly
main:
    LDI r0, 42          ; Load immediate value
    LDI r1, 58          ; Load another value
    ADD r2, r0, r1      ; Add them together
    HLT                 ; Halt execution
```

### Using Pseudo-Instructions

```assembly
main:
    LDI r0, 0x123456789ABCDEF   ; 64-bit immediate load
    PUSH r0, r1, r2             ; Save registers
    
    ; ... some computation ...
    
    POP r0, r1, r2              ; Restore registers
    JMP return_label            ; Jump to label

return_label:
    HLT
```

### Stack Operations

```assembly
main:
    LDI sp, 0x20000000          ; Initialize stack pointer
    
    ; Push values onto stack
    LDI r0, 100
    PUSH r0
    
    ; Call function
    JAL function_addr, ra
    
    ; Pop result
    POP r1
    HLT

function_addr:
    ; Function implementation
    ; Return address is in ra
    JMP ra
```

## Features and Capabilities

### Advanced Assembler Features

- **Intelligent Macro Expansion**: Pseudo-instructions expand to optimal native instruction sequences
- **Temporary Register Management**: Automatic use of temps for complex operations
- **Symbol Resolution**: Forward and backward label references

### Debugging Support

- **Real-time Visualization**: Live updates of CPU and memory state
- **Memory Inspection**: Detailed memory region analysis

## Technical Specifications

### Instruction Encoding

Instructions use a 32-bit fixed-width format:

```
31    27 26    22 21    17 16    12 11     7 6      0
├──────┼────────┼────────┼────────┼────────┼────────┤
│ IMM  │  SRC2  │  SRC   │  DEST  │      OPCODE     │
│(10b) │  (5b)  │  (5b)  │  (5b)  │       (7b)      │
└──────┴────────┴────────┴────────┴────────┴────────┘
```

- **OPCODE**: 7-bit instruction identifier
- **DEST**: 5-bit destination register
- **SRC**: 5-bit source register 1
- **SRC2**: 5-bit source register 2
- **IMM**: 10-bit signed immediate value

## License and Credits

This project demonstrates advanced virtual machine architecture concepts including:

- Custom instruction set design
- Multi-pass assembler implementation
- High-level language compilation
- Memory management systems
- Visual debugging interfaces
- ROM-based system initialization

The Triton-64 VM serves as both an educational tool and a practical platform for systems programming experimentation.