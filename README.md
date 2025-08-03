# Triton-64 Virtual Machine

A complete 64-bit virtual machine implementation featuring a custom CPU architecture, assembler, compiler, and development environment built in Java with JavaFX.

## Overview

The Triton-64 VM is a comprehensive virtual machine system that includes:

- **Custom 64-bit CPU Architecture**: Complete instruction set with 32 registers
- **TriC Programming Language**: High-level language that compiles to Triton-64 assembly
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
|-------|---------|---------|
| `ra`  | `r0`    | Return address |
| `sp`  | `r1`    | Stack pointer |
| `fp`  | `r2`    | Frame pointer |
| `gp`  | `r3`    | Global pointer |
| `hp`  | `r4`    | Heap pointer |
| `s0-s9` | `r5-r14` | Saved registers |
| `a0-a6` | `r15-r21` | Argument registers |
| `t0-t8` | `r22-r30` | Temporary registers |
| `t9`  | `r31`   | **Reserved for assembler use** |

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

TriC is a high-level programming language that compiles to Triton-64 assembly. It provides:

- **Control Structures**: if/else, while loops, for loops
- **Functions**: Functions with proper calling conventions
- **Memory Management**: Automatic stack management with manual heap control
- **Expressions**: Full expression evaluation with operator precedence

### Example TriC Program

```tric
; Triton-C Feature Showcase
; Demonstrates varisters, functions, control flow, and arithmetic

; Main program
func main() {
    ; Register declarations
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

    ; Infinite loop to halt execution
    while (1) {}
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

The TriC compiler implements a full compilation pipeline:

1. **Lexical Analysis**: Token generation from source code
2. **Parsing**: AST construction with syntax validation
3. **Code Generation**: Assembly code emission with optimization

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
├── Main.java                    # Application entry point and VM orchestration
├── assembler/
│   ├── Assembler.java          # Main assembler with pseudo-instruction support
│   ├── Expander.java           # Pseudo-instruction expansion engine
│   ├── Parser.java             # Assembly syntax parsing
│   ├── Preprocessor.java       # Source code preprocessing
│   └── SymbolTable.java        # Label and symbol management
├── compiler/
│   ├── TriCCompiler.java       # Main compiler interface
│   ├── Lexer.java              # Tokenization
│   ├── Parser.java             # AST generation
│   ├── CodeGenerator.java      # Assembly emission
│   ├── CodeGenContext.java     # Context for code generation
│   └── ast/                    # Abstract syntax tree nodes
├── cpu/
│   ├── Cpu.java                # CPU core implementation
│   ├── InstructionSet.java     # ISA definition and encoding
│   ├── InstructionInfo.java    # Instruction metadata
│   └── RegisterInfo.java       # Register naming and aliases
├── memory/
│   ├── Memory.java             # Memory management system
│   └── MemoryMap.java          # Address space layout
├── rom/
│   ├── RomDumper.java          # ROM generation utility
│   └── ROMData.java            # Generated ROM data (auto-generated)
└── visual/
    ├── CpuViewer.java          # CPU state visualization
    ├── MemoryViewer.java       # Memory dump visualization
    ├── MemoryPrinter.java      # Memory content printing
    └── style/                  # Styles for visual components
    
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