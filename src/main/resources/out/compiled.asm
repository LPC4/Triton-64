    ; Program entry point
_start:
    ; Allocating space for 10 global variables
    ; Allocated global variable 'console_cursor' at GP+0
    ; Allocated global variable 'screenWidth' at GP+8
    ; Allocated global variable 'screenHeight' at GP+16
    ; Allocated global variable 'framebufferBase' at GP+24
    ; Allocated global variable 'freeListHead' at GP+32
    ; Allocated global variable 'HEAP_START' at GP+40
    ; Allocated global variable 'HEAP_SIZE' at GP+48
    ; Allocated global variable 'HEADER_SIZE' at GP+56
    ; Allocated global variable 'MIN_BLOCK_SIZE' at GP+64
    ; Allocated global variable 'ALIGNMENT' at GP+72
    LDI t0, 80
    ADD hp, gp, t0
    ; Heap pointer set to SP + 80 for global variables
    ; Initializing global variables
    ; Initializing global 'console_cursor'
    LDI t0, 0
    ST gp, t0
    ; Initializing global 'screenWidth'
    LDI t0, 80
    LDI t1, 8
    ADD t1, gp, t1
    ST t1, t0
    ; Initializing global 'screenHeight'
    LDI t0, 30
    LDI t1, 16
    ADD t1, gp, t1
    ST t1, t0
    ; Initializing global 'framebufferBase'
    LDI t0, 539099136
    LDI t1, 24
    ADD t1, gp, t1
    ST t1, t0
    ; Initializing global 'freeListHead'
    LDI t0, 0
    LDI t1, 32
    ADD t1, gp, t1
    ST t1, t0
    ; Initializing global 'HEAP_START'
    LDI t0, 402784256
    LDI t1, 40
    ADD t1, gp, t1
    ST t1, t0
    ; Initializing global 'HEAP_SIZE'
    LDI t0, 134217728
    LDI t1, 48
    ADD t1, gp, t1
    ST t1, t0
    ; Initializing global 'HEADER_SIZE'
    LDI t0, 8
    LDI t1, 56
    ADD t1, gp, t1
    ST t1, t0
    ; Initializing global 'MIN_BLOCK_SIZE'
    LDI t0, 16
    LDI t1, 64
    ADD t1, gp, t1
    ST t1, t0
    ; Initializing global 'ALIGNMENT'
    LDI t0, 8
    LDI t1, 72
    ADD t1, gp, t1
    ST t1, t0
    JAL main, ra
    HLT

    ; ==============================
    ; Function: console_init
    ; ==============================
console_init:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 0
    ; Assignment to global variable: console_cursor
    ST gp, t0
    ; Expression statement
    ; Function call: console_clear
    ; Preparing function call: console_clear
    JAL console_clear, ra
    MOV t0, a0
    ; Function epilogue
console_init_end_0:
    POP fp
    POP ra
    JMP ra                               ; Return from console_init

    ; =============================
    ; Function: console_clear
    ; =============================
console_clear:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 2 local variables
    ; Declaration: i
    LDI t0, 0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: total_chars
    ; Binary operation: MUL
    LDI t1, 8
    ADD t1, gp, t1
    LD t0, t1
    LDI t2, 16
    ADD t2, gp, t2
    LD t1, t2
    MUL t2, t0, t1
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    ; While loop
loop_2:
    ; Comparison: Variable(name=i) LT Variable(name=total_chars)
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 >= t2
    JPN skip_ge_4, t0
    JMP endloop_3
skip_ge_4:
    ; Expression statement
    ; Function call: writeChar
    ; Preparing function call: writeChar
    ; Evaluating argument 0
    ; Binary operation: ADD
    LDI t1, 24
    ADD t1, gp, t1
    LD t0, t1
    ; Binary operation: MUL
    LDI t1, -8
    ADD t1, fp, t1
    LD t2, t1
    LDI t1, 4
    MUL t3, t2, t1
    ADD t1, t0, t3
    ; Evaluating argument 1
    LDI t0, 32
    PUSH t0
    PUSH t1
    JAL writeChar, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: i
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_2
endloop_3:
    LDI t0, 0
    ; Assignment to global variable: console_cursor
    ST gp, t0
    ; Function epilogue
console_clear_end_1:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from console_clear

    ; ============================
    ; Function: console_scroll
    ; ============================
console_scroll:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 80
    SUB sp, sp, t0
    ; Allocated 80 bytes for 9 local variables
    ; Declaration: src_line
    LDI t0, 1
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: dst_line
    LDI t0, 0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_6:
    ; Comparison: Variable(name=src_line) LT Variable(name=screenHeight)
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 16
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    ; Jump if t1 >= t0
    JPN skip_ge_8, t2
    JMP endloop_7
skip_ge_8:
    ; Declaration: src_start
    ; Binary operation: ADD
    LDI t1, 24
    ADD t1, gp, t1
    LD t0, t1
    ; Binary operation: MUL
    ; Binary operation: MUL
    LDI t1, -8
    ADD t1, fp, t1
    LD t2, t1
    LDI t3, 8
    ADD t3, gp, t3
    LD t1, t3
    MUL t3, t2, t1
    LDI t1, 4
    MUL t2, t3, t1
    ADD t1, t0, t2
    LDI t0, -24
    ADD t0, fp, t0
    ST t0, t1
    ; Declaration: dst_start
    ; Binary operation: ADD
    LDI t1, 24
    ADD t1, gp, t1
    LD t0, t1
    ; Binary operation: MUL
    ; Binary operation: MUL
    LDI t1, -16
    ADD t1, fp, t1
    LD t2, t1
    LDI t3, 8
    ADD t3, gp, t3
    LD t1, t3
    MUL t3, t2, t1
    LDI t1, 4
    MUL t2, t3, t1
    ADD t1, t0, t2
    LDI t0, -32
    ADD t0, fp, t0
    ST t0, t1
    ; Declaration: char_in_line
    LDI t0, 0
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_9:
    ; Comparison: Variable(name=char_in_line) LT Variable(name=screenWidth)
    LDI t0, -40
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 8
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    ; Jump if t1 >= t0
    JPN skip_ge_11, t2
    JMP endloop_10
skip_ge_11:
    ; Declaration: src_char
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: MUL
    LDI t0, -40
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 4
    MUL t3, t2, t0
    ADD t0, t1, t3
    LDI t1, -48
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: dst_char
    ; Binary operation: ADD
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: MUL
    LDI t0, -40
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 4
    MUL t3, t2, t0
    ADD t0, t1, t3
    LDI t1, -56
    ADD t1, fp, t1
    ST t1, t0
    LDI t0, -56
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -48
    ADD t0, fp, t0
    LD t2, t0
    LD t0, t2
    ; Assignment to dereferenced address at t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, -56
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Binary operation: ADD
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t3, t1, t0
    LD t0, t3
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -56
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 2
    ADD t2, t1, t0
    ; Binary operation: ADD
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 2
    ADD t3, t1, t0
    LD t0, t3
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -56
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 3
    ADD t2, t1, t0
    ; Binary operation: ADD
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 3
    ADD t3, t1, t0
    LD t0, t3
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -40
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: char_in_line
    LDI t0, -40
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_9
endloop_10:
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: src_line
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: dst_line
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_6
endloop_7:
    ; Declaration: lastLineStart
    ; Binary operation: ADD
    LDI t1, 24
    ADD t1, gp, t1
    LD t0, t1
    ; Binary operation: MUL
    ; Binary operation: MUL
    ; Binary operation: SUB
    LDI t2, 16
    ADD t2, gp, t2
    LD t1, t2
    LDI t2, 1
    SUB t3, t1, t2
    LDI t2, 8
    ADD t2, gp, t2
    LD t1, t2
    MUL t2, t3, t1
    LDI t1, 4
    MUL t3, t2, t1
    ADD t1, t0, t3
    LDI t0, -64
    ADD t0, fp, t0
    ST t0, t1
    ; Declaration: i
    LDI t0, 0
    LDI t1, -72
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_12:
    ; Comparison: Variable(name=i) LT Variable(name=screenWidth)
    LDI t0, -72
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 8
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    ; Jump if t1 >= t0
    JPN skip_ge_14, t2
    JMP endloop_13
skip_ge_14:
    ; Expression statement
    ; Function call: writeChar
    ; Preparing function call: writeChar
    ; Evaluating argument 0
    ; Binary operation: ADD
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: MUL
    LDI t0, -72
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 4
    MUL t3, t2, t0
    ADD t0, t1, t3
    ; Evaluating argument 1
    LDI t1, 32
    PUSH t1
    PUSH t0
    JAL writeChar, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Binary operation: ADD
    LDI t0, -72
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: i
    LDI t0, -72
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_12
endloop_13:
    ; Function epilogue
console_scroll_end_5:
    LDI t0, 80
    ADD sp, sp, t0
    ; Deallocated 80 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from console_scroll

    ; ==============================
    ; Function: console_putc
    ; ==============================
console_putc:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 1 local variables
    ; If statement
    ; Comparison: Variable(name=char) EQ LongLiteral(value=10)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 10
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_16, t2
    ; Declaration: row
    ; Binary operation: DIV
    LD t0, gp
    LDI t2, 8
    ADD t2, gp, t2
    LD t1, t2
    DIV t2, t0, t1
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; If statement
    ; Comparison: Variable(name=row) GE BinaryOp(op=SUB, left=Variable(name=screenHeight), right=LongLiteral(value=1))
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: SUB
    LDI t2, 16
    ADD t2, gp, t2
    LD t0, t2
    LDI t2, 1
    SUB t3, t0, t2
    SUB t0, t1, t3
    ; Jump if t1 < t3
    JPN else_18, t0
    ; Expression statement
    ; Function call: console_scroll
    ; Preparing function call: console_scroll
    JAL console_scroll, ra
    MOV t0, a0
    ; Binary operation: MUL
    ; Binary operation: SUB
    LDI t1, 16
    ADD t1, gp, t1
    LD t0, t1
    LDI t1, 1
    SUB t2, t0, t1
    LDI t1, 8
    ADD t1, gp, t1
    LD t0, t1
    MUL t1, t2, t0
    ; Assignment to global variable: console_cursor
    ST gp, t1
    JMP endif_19
else_18:
    ; Binary operation: MUL
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t1, 8
    ADD t1, gp, t1
    LD t0, t1
    MUL t1, t2, t0
    ; Assignment to global variable: console_cursor
    ST gp, t1
endif_19:
    ; return statement
    JMP console_putc_end_15
else_16:
    ; If statement
    ; Comparison: Variable(name=console_cursor) GE BinaryOp(op=MUL, left=Variable(name=screenWidth), right=Variable(name=screenHeight))
    LD t0, gp
    ; Binary operation: MUL
    LDI t2, 8
    ADD t2, gp, t2
    LD t1, t2
    LDI t3, 16
    ADD t3, gp, t3
    LD t2, t3
    MUL t3, t1, t2
    SUB t1, t0, t3
    ; Jump if t0 < t3
    JPN else_20, t1
    ; Expression statement
    ; Function call: console_scroll
    ; Preparing function call: console_scroll
    JAL console_scroll, ra
    MOV t0, a0
    ; Binary operation: MUL
    ; Binary operation: SUB
    LDI t1, 16
    ADD t1, gp, t1
    LD t0, t1
    LDI t1, 1
    SUB t2, t0, t1
    LDI t1, 8
    ADD t1, gp, t1
    LD t0, t1
    MUL t1, t2, t0
    ; Assignment to global variable: console_cursor
    ST gp, t1
else_20:
    ; Expression statement
    ; Function call: writeChar
    ; Preparing function call: writeChar
    ; Evaluating argument 0
    ; Binary operation: ADD
    LDI t1, 24
    ADD t1, gp, t1
    LD t0, t1
    ; Binary operation: MUL
    LD t1, gp
    LDI t2, 4
    MUL t3, t1, t2
    ADD t1, t0, t3
    ; Evaluating argument 1
    LDI t0, 16
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL writeChar, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Binary operation: ADD
    LD t0, gp
    LDI t1, 1
    ADD t2, t0, t1
    ; Assignment to global variable: console_cursor
    ST gp, t2
    ; Function epilogue
console_putc_end_15:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from console_putc

    ; ==============================
    ; Function: console_puts
    ; ==============================
console_puts:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 1 local variables
    ; Declaration: i
    LDI t0, 0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_23:
    ; Comparison: Variable(name=i) LT Variable(name=len)
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 >= t2
    JPN skip_ge_25, t0
    JMP endloop_24
skip_ge_25:
    ; Expression statement
    ; Function call: console_putc
    ; Preparing function call: console_putc
    ; Evaluating argument 0
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LD t1, t0
    PUSH t1
    JAL console_putc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: i
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_23
endloop_24:
    ; Function epilogue
console_puts_end_22:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from console_puts

    ; =====================================
    ; Function: print
    ; =====================================
print:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; Expression statement
    ; Function call: console_puts
    ; Preparing function call: console_puts
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL console_puts, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Function epilogue
print_end_26:
    POP fp
    POP ra
    JMP ra                               ; Return from print

    ; ===================================
    ; Function: println
    ; ===================================
println:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; Expression statement
    ; Function call: print
    ; Preparing function call: print
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL print, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: console_putc
    ; Preparing function call: console_putc
    ; Evaluating argument 0
    LDI t0, 10
    PUSH t0
    JAL console_putc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Function epilogue
println_end_27:
    POP fp
    POP ra
    JMP ra                               ; Return from println

    ; =================================
    ; Function: print_raw
    ; =================================
print_raw:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; Expression statement
    ; Function call: console_puts
    ; Preparing function call: console_puts
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL console_puts, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Function epilogue
print_raw_end_28:
    POP fp
    POP ra
    JMP ra                               ; Return from print_raw

    ; =================================
    ; Function: writeChar
    ; =================================
writeChar:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 2 local variables
    ; Declaration: fgColor
    LDI t0, 15
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: bgColor
    LDI t0, 0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    ; Assignment to dereferenced address at t1
    ST t1, t2
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to dereferenced address at t2
    ST t2, t1
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 2
    ADD t2, t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to dereferenced address at t2
    ST t2, t1
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 3
    ADD t2, t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Function epilogue
writeChar_end_29:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from writeChar

    ; ===================================
    ; Function: write64
    ; ===================================
write64:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    ; Assignment to dereferenced address at t1
    ST t1, t2
    ; Function epilogue
write64_end_30:
    POP fp
    POP ra
    JMP ra                               ; Return from write64

    ; ====================================
    ; Function: read64
    ; ====================================
read64:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; return statement
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LD t0, t1
    MOV a0, t0
    JMP read64_end_31
    ; Function epilogue
read64_end_31:
    POP fp
    POP ra
    JMP ra                               ; Return from read64

    ; ==================================
    ; Function: align_up
    ; ==================================
align_up:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; return statement
    ; Binary operation: BITWISE_AND
    ; Binary operation: SUB
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, 1
    SUB t2, t0, t1
    ; Unary operation: NOT
    ; Binary operation: SUB
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    SUB t3, t1, t0
    NOT t0, t3
    AND t1, t2, t0
    MOV a0, t1
    JMP align_up_end_32
    ; Function epilogue
align_up_end_32:
    POP fp
    POP ra
    JMP ra                               ; Return from align_up

    ; ============================
    ; Function: get_block_size
    ; ============================
get_block_size:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; return statement
    ; Binary operation: SHR
    ; Function call: read64
    ; Preparing function call: read64
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL read64, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, 32
    SHR t2, t0, t1
    MOV a0, t2
    JMP get_block_size_end_33
    ; Function epilogue
get_block_size_end_33:
    POP fp
    POP ra
    JMP ra                               ; Return from get_block_size

    ; ============================
    ; Function: get_next_block
    ; ============================
get_next_block:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; return statement
    ; Binary operation: BITWISE_AND
    ; Function call: read64
    ; Preparing function call: read64
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL read64, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Binary operation: SUB
    ; Binary operation: SHL
    LDI t1, 1
    LDI t2, 32
    SHL t3, t1, t2
    LDI t1, 1
    SUB t2, t3, t1
    AND t1, t0, t2
    MOV a0, t1
    JMP get_next_block_end_34
    ; Function epilogue
get_next_block_end_34:
    POP fp
    POP ra
    JMP ra                               ; Return from get_next_block

    ; ================================
    ; Function: set_header
    ; ================================
set_header:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; Expression statement
    ; Function call: write64
    ; Preparing function call: write64
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    ; Binary operation: BITWISE_OR
    ; Binary operation: SHL
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 32
    SHL t3, t2, t0
    LDI t0, 32
    ADD t0, fp, t0
    LD t2, t0
    OR t0, t3, t2
    PUSH t0
    PUSH t1
    JAL write64, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Function epilogue
set_header_end_35:
    POP fp
    POP ra
    JMP ra                               ; Return from set_header

    ; =================================
    ; Function: init_heap
    ; =================================
init_heap:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; If statement
    ; Comparison: Variable(name=freeListHead) EQ LongLiteral(value=0)
    LDI t1, 32
    ADD t1, gp, t1
    LD t0, t1
    LDI t1, 0
    SUB t2, t0, t1
    ; Jump if t0 != t1
    JNZ else_37, t2
    LDI t1, 40
    ADD t1, gp, t1
    LD t0, t1
    ; Assignment to global variable: freeListHead
    LDI t1, 32
    ADD t1, gp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LDI t1, 32
    ADD t1, gp, t1
    LD t0, t1
    ; Evaluating argument 1
    LDI t2, 48
    ADD t2, gp, t2
    LD t1, t2
    ; Evaluating argument 2
    LDI t2, 0
    PUSH t2
    PUSH t1
    PUSH t0
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
else_37:
    ; Function epilogue
init_heap_end_36:
    POP fp
    POP ra
    JMP ra                               ; Return from init_heap

    ; ==============================
    ; Function: unlink_block
    ; ==============================
unlink_block:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 2 local variables
    ; Declaration: next
    ; Function call: get_next_block
    ; Preparing function call: get_next_block
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_next_block, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=prev) EQ LongLiteral(value=0)
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_40, t2
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to global variable: freeListHead
    LDI t0, 32
    ADD t0, gp, t0
    ST t0, t1
    JMP endif_41
else_40:
    ; Declaration: prev_size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t0, -8
    ADD t0, fp, t0
    LD t3, t0
    PUSH t3
    PUSH t2
    PUSH t1
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
endif_41:
    ; Function epilogue
unlink_block_end_39:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from unlink_block

    ; ===============================
    ; Function: split_block
    ; ===============================
split_block:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 32
    SUB sp, sp, t0
    ; Allocated 32 bytes for 3 local variables
    ; Declaration: total_size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: remaining
    ; Binary operation: SUB
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=remaining) GE Variable(name=MIN_BLOCK_SIZE)
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 64
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    ; Jump if t1 < t0
    JPN else_43, t2
    ; Declaration: new_block
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t3, 32
    ADD t3, gp, t3
    LD t0, t3
    PUSH t0
    PUSH t2
    PUSH t1
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to global variable: freeListHead
    LDI t0, 32
    ADD t0, gp, t0
    ST t0, t1
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t0, 0
    PUSH t0
    PUSH t2
    PUSH t1
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
    JMP endif_44
else_43:
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t0, 0
    PUSH t0
    PUSH t2
    PUSH t1
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
endif_44:
    ; Function epilogue
split_block_end_42:
    LDI t0, 32
    ADD sp, sp, t0
    ; Deallocated 32 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from split_block

    ; ====================================
    ; Function: malloc
    ; ====================================
malloc:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 48
    SUB sp, sp, t0
    ; Allocated 48 bytes for 6 local variables
    ; If statement
    ; Comparison: Variable(name=size_req) EQ LongLiteral(value=0)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_46, t2
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP malloc_end_45
else_46:
    ; Expression statement
    ; Function call: init_heap
    ; Preparing function call: init_heap
    JAL init_heap, ra
    MOV t0, a0
    ; Declaration: aligned_size
    ; Function call: align_up
    ; Preparing function call: align_up
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t2, 72
    ADD t2, gp, t2
    LD t0, t2
    PUSH t0
    PUSH t1
    JAL align_up, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: total_needed
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 56
    ADD t2, gp, t2
    LD t0, t2
    ADD t2, t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: current
    LDI t1, 32
    ADD t1, gp, t1
    LD t0, t1
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: prev
    LDI t0, 0
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_48:
    ; Comparison: Variable(name=current) NE LongLiteral(value=0)
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ endloop_49, t2
    ; Declaration: block_size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: next_block
    ; Function call: get_next_block
    ; Preparing function call: get_next_block
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_next_block, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -48
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=block_size) GE Variable(name=total_needed)
    LDI t0, -40
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 < t2
    JPN else_50, t0
    ; Expression statement
    ; Function call: unlink_block
    ; Preparing function call: unlink_block
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -32
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL unlink_block, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: split_block
    ; Preparing function call: split_block
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL split_block, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; return statement
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 56
    ADD t2, gp, t2
    LD t0, t2
    ADD t2, t1, t0
    MOV a0, t2
    JMP malloc_end_45
else_50:
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to local variable: prev
    LDI t0, -32
    ADD t0, fp, t0
    ST t0, t1
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to local variable: current
    LDI t0, -24
    ADD t0, fp, t0
    ST t0, t1
    JMP loop_48
endloop_49:
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP malloc_end_45
    ; Function epilogue
malloc_end_45:
    LDI t0, 48
    ADD sp, sp, t0
    ; Deallocated 48 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from malloc

    ; ==============================
    ; Function: is_valid_ptr
    ; ==============================
is_valid_ptr:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 32
    SUB sp, sp, t0
    ; Allocated 32 bytes for 3 local variables
    ; If statement
    ; Comparison: Variable(name=ptr) EQ LongLiteral(value=0)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_53, t2
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP is_valid_ptr_end_52
else_53:
    ; Declaration: header_addr
    ; Binary operation: SUB
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 56
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; If statement
    ; Logical OR: BinaryOp(op=LT, left=Variable(name=header_addr), right=Variable(name=HEAP_START)) || BinaryOp(op=GE, left=Variable(name=header_addr), right=BinaryOp(op=ADD, left=Variable(name=HEAP_START), right=Variable(name=HEAP_SIZE)))
    ; Comparison: Variable(name=header_addr) LT Variable(name=HEAP_START)
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 40
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    ; Jump if t1 >= t0
    JPN skip_ge_58, t2
    JMP or_eval_right_57
skip_ge_58:
    JMP or_skip_59
or_eval_right_57:
    ; Comparison: Variable(name=header_addr) GE BinaryOp(op=ADD, left=Variable(name=HEAP_START), right=Variable(name=HEAP_SIZE))
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: ADD
    LDI t2, 40
    ADD t2, gp, t2
    LD t0, t2
    LDI t3, 48
    ADD t3, gp, t3
    LD t2, t3
    ADD t3, t0, t2
    SUB t0, t1, t3
    ; Jump if t1 < t3
    JPN else_55, t0
or_skip_59:
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP is_valid_ptr_end_52
else_55:
    ; Declaration: size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Logical OR: BinaryOp(op=LT, left=Variable(name=size), right=Variable(name=MIN_BLOCK_SIZE)) || BinaryOp(op=GT, left=Variable(name=size), right=Variable(name=HEAP_SIZE))
    ; Comparison: Variable(name=size) LT Variable(name=MIN_BLOCK_SIZE)
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 64
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    ; Jump if t1 >= t0
    JPN skip_ge_63, t2
    JMP or_eval_right_62
skip_ge_63:
    JMP or_skip_64
or_eval_right_62:
    ; Comparison: Variable(name=size) GT Variable(name=HEAP_SIZE)
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 48
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    ; Jump if t1 <= t0
    JPP skip_le_65, t2
    JMP else_60
skip_le_65:
or_skip_64:
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP is_valid_ptr_end_52
else_60:
    ; Declaration: next
    ; Function call: get_next_block
    ; Preparing function call: get_next_block
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_next_block, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=next) NE LongLiteral(value=0)
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ else_66, t2
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP is_valid_ptr_end_52
else_66:
    ; return statement
    LDI t0, 1
    MOV a0, t0
    JMP is_valid_ptr_end_52
    ; Function epilogue
is_valid_ptr_end_52:
    LDI t0, 32
    ADD sp, sp, t0
    ; Deallocated 32 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from is_valid_ptr

    ; ======================
    ; Function: coalesce_free_blocks
    ; ======================
coalesce_free_blocks:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 48
    SUB sp, sp, t0
    ; Allocated 48 bytes for 5 local variables
    ; If statement
    ; Comparison: Variable(name=freeListHead) EQ LongLiteral(value=0)
    LDI t1, 32
    ADD t1, gp, t1
    LD t0, t1
    LDI t1, 0
    SUB t2, t0, t1
    ; Jump if t0 != t1
    JNZ else_69, t2
    ; return statement
    JMP coalesce_free_blocks_end_68
else_69:
    ; Declaration: current
    LDI t1, 32
    ADD t1, gp, t1
    LD t0, t1
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_71:
    ; Comparison: Variable(name=current) NE LongLiteral(value=0)
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ endloop_72, t2
    ; Declaration: current_size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: next
    ; Function call: get_next_block
    ; Preparing function call: get_next_block
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_next_block, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Logical AND: BinaryOp(op=NE, left=Variable(name=next), right=LongLiteral(value=0)) && BinaryOp(op=EQ, left=Variable(name=next), right=BinaryOp(op=ADD, left=Variable(name=current), right=Variable(name=current_size)))
    ; Comparison: Variable(name=next) NE LongLiteral(value=0)
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ else_73, t2
    ; Comparison: Variable(name=next) EQ BinaryOp(op=ADD, left=Variable(name=current), right=Variable(name=current_size))
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t3, t0
    ADD t0, t2, t3
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_73, t2
    ; Declaration: next_size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: next_next
    ; Function call: get_next_block
    ; Preparing function call: get_next_block
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_next_block, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, -32
    ADD t0, fp, t0
    LD t3, t0
    ADD t0, t2, t3
    ; Evaluating argument 2
    LDI t2, -40
    ADD t2, fp, t2
    LD t3, t2
    PUSH t3
    PUSH t0
    PUSH t1
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
    JMP endif_74
else_73:
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to local variable: current
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t1
endif_74:
    JMP loop_71
endloop_72:
    ; Function epilogue
coalesce_free_blocks_end_68:
    LDI t0, 48
    ADD sp, sp, t0
    ; Deallocated 48 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from coalesce_free_blocks

    ; ======================================
    ; Function: free
    ; ======================================
free:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 2 local variables
    ; If statement
    ; Binary operation: BITWISE_AND
    ; Unary operation: NOT
    ; Function call: is_valid_ptr
    ; Preparing function call: is_valid_ptr
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL is_valid_ptr, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    NOT t1, t0
    LDI t0, 1
    AND t2, t1, t0
    JZ else_76, t2
    ; return statement
    JMP free_end_75
else_76:
    ; Declaration: header_addr
    ; Binary operation: SUB
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 56
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: block_size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t3, 32
    ADD t3, gp, t3
    LD t0, t3
    PUSH t0
    PUSH t2
    PUSH t1
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to global variable: freeListHead
    LDI t0, 32
    ADD t0, gp, t0
    ST t0, t1
    ; Expression statement
    ; Function call: coalesce_free_blocks
    ; Preparing function call: coalesce_free_blocks
    JAL coalesce_free_blocks, ra
    MOV t0, a0
    ; Function epilogue
free_end_75:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from free

    ; ===================================
    ; Function: realloc
    ; ===================================
realloc:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 48
    SUB sp, sp, t0
    ; Allocated 48 bytes for 6 local variables
    ; If statement
    ; Comparison: Variable(name=ptr) EQ LongLiteral(value=0)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_79, t2
    ; return statement
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    MOV a0, t0
    JMP realloc_end_78
else_79:
    ; If statement
    ; Comparison: Variable(name=new_size) EQ LongLiteral(value=0)
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_81, t2
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP realloc_end_78
else_81:
    ; If statement
    ; Binary operation: BITWISE_AND
    ; Unary operation: NOT
    ; Function call: is_valid_ptr
    ; Preparing function call: is_valid_ptr
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL is_valid_ptr, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    NOT t1, t0
    LDI t0, 1
    AND t2, t1, t0
    JZ else_83, t2
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP realloc_end_78
else_83:
    ; Declaration: header_addr
    ; Binary operation: SUB
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 56
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: current_size
    ; Binary operation: SUB
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t2, 56
    ADD t2, gp, t2
    LD t1, t2
    SUB t2, t0, t1
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: aligned_new_size
    ; Function call: align_up
    ; Preparing function call: align_up
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t2, 72
    ADD t2, gp, t2
    LD t0, t2
    PUSH t0
    PUSH t1
    JAL align_up, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=aligned_new_size) LE Variable(name=current_size)
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 > t2
    JPP else_85, t0
    ; return statement
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP realloc_end_78
else_85:
    ; Declaration: new_ptr
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=new_ptr) EQ LongLiteral(value=0)
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_87, t2
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP realloc_end_78
else_87:
    ; Declaration: copy_size
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -40
    ADD t0, fp, t0
    ST t0, t1
    ; If statement
    ; Comparison: Variable(name=aligned_new_size) LT Variable(name=copy_size)
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -40
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 >= t2
    JPN skip_ge_91, t0
    JMP else_89
skip_ge_91:
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to local variable: copy_size
    LDI t0, -40
    ADD t0, fp, t0
    ST t0, t1
else_89:
    ; Declaration: i
    LDI t0, 0
    LDI t1, -48
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_92:
    ; Comparison: Variable(name=i) LT Variable(name=copy_size)
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -40
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 >= t2
    JPN skip_ge_94, t0
    JMP endloop_93
skip_ge_94:
    ; Binary operation: ADD
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -48
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Binary operation: ADD
    LDI t1, 16
    ADD t1, fp, t1
    LD t2, t1
    LDI t1, -48
    ADD t1, fp, t1
    LD t3, t1
    ADD t1, t2, t3
    LD t2, t1
    ; Assignment to dereferenced address at t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: i
    LDI t0, -48
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_92
endloop_93:
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; return statement
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP realloc_end_78
    ; Function epilogue
realloc_end_78:
    LDI t0, 48
    ADD sp, sp, t0
    ; Deallocated 48 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from realloc

    ; ====================================
    ; Function: calloc
    ; ====================================
calloc:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 32
    SUB sp, sp, t0
    ; Allocated 32 bytes for 3 local variables
    ; Declaration: total_size
    ; Binary operation: MUL
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    MUL t0, t1, t2
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: ptr
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=ptr) EQ LongLiteral(value=0)
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_96, t2
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP calloc_end_95
else_96:
    ; Declaration: i
    LDI t0, 0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_98:
    ; Comparison: Variable(name=i) LT Variable(name=total_size)
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 >= t2
    JPN skip_ge_100, t0
    JMP endloop_99
skip_ge_100:
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -24
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, 0
    ; Assignment to dereferenced address at t0
    ST t0, t1
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: i
    LDI t0, -24
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_98
endloop_99:
    ; return statement
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP calloc_end_95
    ; Function epilogue
calloc_end_95:
    LDI t0, 32
    ADD sp, sp, t0
    ; Deallocated 32 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from calloc

    ; ============================
    ; Function: get_heap_stats
    ; ============================
get_heap_stats:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 1 local variables
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t1
    ST t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t1
    ST t1, t0
    LDI t0, 32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t1
    ST t1, t0
    ; Declaration: current
    LDI t1, 32
    ADD t1, gp, t1
    LD t0, t1
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_102:
    ; Comparison: Variable(name=current) NE LongLiteral(value=0)
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ endloop_103, t2
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t2, t0
    LD t0, t2
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Saving 2 live temporaries
    PUSH t1
    PUSH t0
    ; Evaluating argument 0
    LDI t2, -8
    ADD t2, fp, t2
    LD t3, t2
    PUSH t3
    JAL get_block_size, ra
    LDI t2, 8
    ADD sp, sp, t2
    ; Cleaned up 1 arguments from stack
    MOV t2, a0
    ; Restoring 2 saved temporaries
    POP t0
    POP t1
    ADD t3, t0, t2
    ; Assignment to dereferenced address at t1
    ST t1, t3
    LDI t0, 32
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: ADD
    LDI t0, 32
    ADD t0, fp, t0
    LD t2, t0
    LD t0, t2
    LDI t2, 1
    ADD t3, t0, t2
    ; Assignment to dereferenced address at t1
    ST t1, t3
    ; Function call: get_next_block
    ; Preparing function call: get_next_block
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_next_block, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Assignment to local variable: current
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    JMP loop_102
endloop_103:
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: SUB
    LDI t2, 48
    ADD t2, gp, t2
    LD t0, t2
    LDI t2, 16
    ADD t2, fp, t2
    LD t3, t2
    LD t2, t3
    SUB t3, t0, t2
    ; Assignment to dereferenced address at t1
    ST t1, t3
    ; Function epilogue
get_heap_stats_end_101:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from get_heap_stats

    ; ============================
    ; Function: get_last_digit
    ; ============================
get_last_digit:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 2 local variables
    ; Declaration: tens
    ; Binary operation: DIV
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 10
    DIV t2, t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: last_digit
    ; Binary operation: SUB
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: MUL
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 10
    MUL t3, t2, t0
    SUB t0, t1, t3
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; return statement
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP get_last_digit_end_104
    ; Function epilogue
get_last_digit_end_104:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from get_last_digit

    ; =====================
    ; Function: get_digit_at_position
    ; =====================
get_digit_at_position:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 2 local variables
    ; Declaration: temp
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t1
    ; Declaration: i
    LDI t0, 0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_106:
    ; Comparison: Variable(name=i) LT Variable(name=position)
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 >= t2
    JPN skip_ge_108, t0
    JMP endloop_107
skip_ge_108:
    ; Binary operation: DIV
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 10
    DIV t2, t1, t0
    ; Assignment to local variable: temp
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: i
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_106
endloop_107:
    ; return statement
    ; Function call: get_last_digit
    ; Preparing function call: get_last_digit
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_last_digit, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    MOV a0, t0
    JMP get_digit_at_position_end_105
    ; Function epilogue
get_digit_at_position_end_105:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from get_digit_at_position

    ; =========================
    ; Function: get_number_length
    ; =========================
get_number_length:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 2 local variables
    ; If statement
    ; Comparison: Variable(name=num) EQ LongLiteral(value=0)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_110, t2
    ; return statement
    LDI t0, 1
    MOV a0, t0
    JMP get_number_length_end_109
else_110:
    ; Declaration: count
    LDI t0, 0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: temp
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t1
    ; While loop
loop_112:
    ; Comparison: Variable(name=temp) GT LongLiteral(value=0)
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 <= t0
    JPP skip_le_114, t2
    JMP endloop_113
skip_le_114:
    ; Binary operation: DIV
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 10
    DIV t2, t1, t0
    ; Assignment to local variable: temp
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: count
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_112
endloop_113:
    ; return statement
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP get_number_length_end_109
    ; Function epilogue
get_number_length_end_109:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from get_number_length

    ; ==========================
    ; Function: number_to_string
    ; ==========================
number_to_string:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 32
    SUB sp, sp, t0
    ; Allocated 32 bytes for 4 local variables
    ; If statement
    ; Comparison: Variable(name=num) EQ LongLiteral(value=0)
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_116, t2
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 48
    ; Assignment to dereferenced address at t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; return statement
    LDI t0, 1
    MOV a0, t0
    JMP number_to_string_end_115
else_116:
    ; Declaration: num_digits
    ; Function call: get_number_length
    ; Preparing function call: get_number_length
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_number_length, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: digit_pos
    ; Binary operation: SUB
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    SUB t2, t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: string_pos
    LDI t0, 0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_118:
    ; Comparison: Variable(name=digit_pos) GE LongLiteral(value=0)
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 < t0
    JPN endloop_119, t2
    ; Declaration: digit
    ; Function call: get_digit_at_position
    ; Preparing function call: get_digit_at_position
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL get_digit_at_position, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -24
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Binary operation: ADD
    LDI t1, 48
    LDI t2, -32
    ADD t2, fp, t2
    LD t3, t2
    ADD t2, t1, t3
    ; Assignment to dereferenced address at t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: string_pos
    LDI t0, -24
    ADD t0, fp, t0
    ST t0, t2
    ; Binary operation: SUB
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    SUB t2, t1, t0
    ; Assignment to local variable: digit_pos
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_118
endloop_119:
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -24
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, 0
    ; Assignment to dereferenced address at t0
    ST t0, t1
    ; return statement
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP number_to_string_end_115
    ; Function epilogue
number_to_string_end_115:
    LDI t0, 32
    ADD sp, sp, t0
    ; Deallocated 32 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from number_to_string

    ; ================
    ; Function: create_message_with_number
    ; ================
create_message_with_number:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 32
    SUB sp, sp, t0
    ; Allocated 32 bytes for 4 local variables
    ; Declaration: pos
    LDI t0, 0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: prefix_pos
    LDI t0, 0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_121:
    ; Comparison: org.lpc.compiler.ast.expressions.Dereference@3f2c74bf NE LongLiteral(value=0)
    ; Binary operation: ADD
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ endloop_122, t2
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Binary operation: ADD
    LDI t1, 24
    ADD t1, fp, t1
    LD t2, t1
    LDI t1, -16
    ADD t1, fp, t1
    LD t3, t1
    ADD t1, t2, t3
    LD t2, t1
    ; Assignment to dereferenced address at t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: pos
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: prefix_pos
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_121
endloop_122:
    ; Declaration: num_len
    ; Function call: number_to_string
    ; Preparing function call: number_to_string
    ; Evaluating argument 0
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Evaluating argument 1
    LDI t1, 32
    ADD t1, fp, t1
    LD t2, t1
    PUSH t2
    PUSH t0
    JAL number_to_string, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -24
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Assignment to local variable: pos
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: suffix_pos
    LDI t0, 0
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_123:
    ; Comparison: org.lpc.compiler.ast.expressions.Dereference@43483538 NE LongLiteral(value=0)
    ; Binary operation: ADD
    LDI t0, 40
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -32
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ endloop_124, t2
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Binary operation: ADD
    LDI t1, 40
    ADD t1, fp, t1
    LD t2, t1
    LDI t1, -32
    ADD t1, fp, t1
    LD t3, t1
    ADD t1, t2, t3
    LD t2, t1
    ; Assignment to dereferenced address at t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: pos
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: suffix_pos
    LDI t0, -32
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_123
endloop_124:
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, 0
    ; Assignment to dereferenced address at t0
    ST t0, t1
    ; return statement
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP create_message_with_number_end_120
    ; Function epilogue
create_message_with_number_end_120:
    LDI t0, 32
    ADD sp, sp, t0
    ; Deallocated 32 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from create_message_with_number

    ; ==============================
    ; Function: print_number
    ; ==============================
print_number:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 2 local variables
    ; Declaration: buffer
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 20
    PUSH t0
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: len
    ; Function call: number_to_string
    ; Preparing function call: number_to_string
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, 16
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL number_to_string, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: print
    ; Preparing function call: print
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL print, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Function epilogue
print_number_end_125:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from print_number

    ; =================
    ; Function: print_number_with_newline
    ; =================
print_number_with_newline:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; Expression statement
    ; Function call: print_number
    ; Preparing function call: print_number
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL print_number, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: console_putc
    ; Preparing function call: console_putc
    ; Evaluating argument 0
    LDI t0, 10
    PUSH t0
    JAL console_putc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Function epilogue
print_number_with_newline_end_126:
    POP fp
    POP ra
    JMP ra                               ; Return from print_number_with_newline

    ; ========================
    ; Function: demo_basic_numbers
    ; ========================
demo_basic_numbers:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 32
    SUB sp, sp, t0
    ; Allocated 32 bytes for 3 local variables
    ; Declaration: numbers_to_test
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 40
    PUSH t0
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 4
    ADD t2, t1, t0
    LDI t0, 5
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 8
    ADD t2, t1, t0
    LDI t0, 42
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 12
    ADD t2, t1, t0
    LDI t0, 123
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 16
    ADD t2, t1, t0
    LDI t0, 999
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 20
    ADD t2, t1, t0
    LDI t0, 1000
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t2, t1, t0
    LDI t0, 12345
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 28
    ADD t2, t1, t0
    LDI t0, 99999
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Declaration: i
    LDI t0, 0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_128:
    ; Comparison: Variable(name=i) LT LongLiteral(value=8)
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 8
    SUB t2, t1, t0
    ; Jump if t1 >= t0
    JPN skip_ge_130, t2
    JMP endloop_129
skip_ge_130:
    ; Declaration: test_num
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: MUL
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 4
    MUL t3, t2, t0
    ADD t0, t1, t3
    LD t1, t0
    LDI t0, -24
    ADD t0, fp, t0
    ST t0, t1
    ; Expression statement
    ; Function call: print_number_with_newline
    ; Preparing function call: print_number_with_newline
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL print_number_with_newline, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: i
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_128
endloop_129:
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Function epilogue
demo_basic_numbers_end_127:
    LDI t0, 32
    ADD sp, sp, t0
    ; Deallocated 32 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from demo_basic_numbers

    ; ===================
    ; Function: demo_formatted_messages
    ; ===================
demo_formatted_messages:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 48
    SUB sp, sp, t0
    ; Allocated 48 bytes for 5 local variables
    ; Declaration: message_buffer
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 100
    PUSH t0
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: prefix
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 20
    PUSH t0
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: suffix
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 20
    PUSH t0
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 67
    ; Assignment to dereferenced address at t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, 111
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 2
    ADD t2, t1, t0
    LDI t0, 117
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 3
    ADD t2, t1, t0
    LDI t0, 110
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 4
    ADD t2, t1, t0
    LDI t0, 116
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 5
    ADD t2, t1, t0
    LDI t0, 58
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 6
    ADD t2, t1, t0
    LDI t0, 32
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 7
    ADD t2, t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t2
    ST t2, t0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 32
    ; Assignment to dereferenced address at t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, 105
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 2
    ADD t2, t1, t0
    LDI t0, 116
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 3
    ADD t2, t1, t0
    LDI t0, 101
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 4
    ADD t2, t1, t0
    LDI t0, 109
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 5
    ADD t2, t1, t0
    LDI t0, 115
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 6
    ADD t2, t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Declaration: count
    LDI t0, 1
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_132:
    ; Comparison: Variable(name=count) LE LongLiteral(value=10)
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 10
    SUB t2, t1, t0
    ; Jump if t1 > t0
    JPP endloop_133, t2
    ; Declaration: msg_len
    ; Function call: create_message_with_number
    ; Preparing function call: create_message_with_number
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t0, -32
    ADD t0, fp, t0
    LD t3, t0
    ; Evaluating argument 3
    LDI t0, -24
    ADD t0, fp, t0
    LD t4, t0
    PUSH t4
    PUSH t3
    PUSH t2
    PUSH t1
    JAL create_message_with_number, ra
    LDI t0, 32
    ADD sp, sp, t0
    ; Cleaned up 4 arguments from stack
    MOV t0, a0
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: print
    ; Preparing function call: print
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -40
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL print, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: console_putc
    ; Preparing function call: console_putc
    ; Evaluating argument 0
    LDI t0, 10
    PUSH t0
    JAL console_putc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Binary operation: ADD
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: count
    LDI t0, -32
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_132
endloop_133:
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Function epilogue
demo_formatted_messages_end_131:
    LDI t0, 48
    ADD sp, sp, t0
    ; Deallocated 48 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from demo_formatted_messages

    ; ===========================
    ; Function: demo_math_table
    ; ===========================
demo_math_table:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 96
    SUB sp, sp, t0
    ; Allocated 96 bytes for 12 local variables
    ; Declaration: line_buffer
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 50
    PUSH t0
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: times_text
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 10
    PUSH t0
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: equals_text
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 10
    PUSH t0
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 32
    ; Assignment to dereferenced address at t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, 120
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 2
    ADD t2, t1, t0
    LDI t0, 32
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 3
    ADD t2, t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t2
    ST t2, t0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 32
    ; Assignment to dereferenced address at t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, 61
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 2
    ADD t2, t1, t0
    LDI t0, 32
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 3
    ADD t2, t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Declaration: a
    LDI t0, 1
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_135:
    ; Comparison: Variable(name=a) LE LongLiteral(value=5)
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 5
    SUB t2, t1, t0
    ; Jump if t1 > t0
    JPP endloop_136, t2
    ; Declaration: b
    LDI t0, 1
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_137:
    ; Comparison: Variable(name=b) LE LongLiteral(value=5)
    LDI t0, -40
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 5
    SUB t2, t1, t0
    ; Jump if t1 > t0
    JPP endloop_138, t2
    ; Declaration: result
    ; Binary operation: MUL
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -40
    ADD t0, fp, t0
    LD t2, t0
    MUL t0, t1, t2
    LDI t1, -48
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: pos
    LDI t0, 0
    LDI t1, -56
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: len1
    ; Function call: number_to_string
    ; Preparing function call: number_to_string
    ; Evaluating argument 0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -56
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Evaluating argument 1
    LDI t1, -32
    ADD t1, fp, t1
    LD t2, t1
    PUSH t2
    PUSH t0
    JAL number_to_string, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    LDI t1, -64
    ADD t1, fp, t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, -56
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -64
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Assignment to local variable: pos
    LDI t1, -56
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: times_pos
    LDI t0, 0
    LDI t1, -72
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_139:
    ; Comparison: org.lpc.compiler.ast.expressions.Dereference@27769f85 NE LongLiteral(value=0)
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -72
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ endloop_140, t2
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -56
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Binary operation: ADD
    LDI t1, -16
    ADD t1, fp, t1
    LD t2, t1
    LDI t1, -72
    ADD t1, fp, t1
    LD t3, t1
    ADD t1, t2, t3
    LD t2, t1
    ; Assignment to dereferenced address at t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -56
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: pos
    LDI t0, -56
    ADD t0, fp, t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -72
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: times_pos
    LDI t0, -72
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_139
endloop_140:
    ; Declaration: len2
    ; Function call: number_to_string
    ; Preparing function call: number_to_string
    ; Evaluating argument 0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -56
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Evaluating argument 1
    LDI t1, -40
    ADD t1, fp, t1
    LD t2, t1
    PUSH t2
    PUSH t0
    JAL number_to_string, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    LDI t1, -80
    ADD t1, fp, t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, -56
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -80
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Assignment to local variable: pos
    LDI t1, -56
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: equals_pos
    LDI t0, 0
    LDI t1, -88
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_141:
    ; Comparison: org.lpc.compiler.ast.expressions.Dereference@1ca0ddd4 NE LongLiteral(value=0)
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -88
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ endloop_142, t2
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -56
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Binary operation: ADD
    LDI t1, -24
    ADD t1, fp, t1
    LD t2, t1
    LDI t1, -88
    ADD t1, fp, t1
    LD t3, t1
    ADD t1, t2, t3
    LD t2, t1
    ; Assignment to dereferenced address at t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -56
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: pos
    LDI t0, -56
    ADD t0, fp, t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -88
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: equals_pos
    LDI t0, -88
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_141
endloop_142:
    ; Declaration: len3
    ; Function call: number_to_string
    ; Preparing function call: number_to_string
    ; Evaluating argument 0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -56
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Evaluating argument 1
    LDI t1, -48
    ADD t1, fp, t1
    LD t2, t1
    PUSH t2
    PUSH t0
    JAL number_to_string, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    LDI t1, -96
    ADD t1, fp, t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, -56
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -96
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Assignment to local variable: pos
    LDI t1, -56
    ADD t1, fp, t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -56
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, 0
    ; Assignment to dereferenced address at t0
    ST t0, t1
    ; Expression statement
    ; Function call: print
    ; Preparing function call: print
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -56
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL print, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: console_putc
    ; Preparing function call: console_putc
    ; Evaluating argument 0
    LDI t0, 10
    PUSH t0
    JAL console_putc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Binary operation: ADD
    LDI t0, -40
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: b
    LDI t0, -40
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_137
endloop_138:
    ; Binary operation: ADD
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: a
    LDI t0, -32
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_135
endloop_136:
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Function epilogue
demo_math_table_end_134:
    LDI t0, 96
    ADD sp, sp, t0
    ; Deallocated 96 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from demo_math_table

    ; ============================
    ; Function: demo_countdown
    ; ============================
demo_countdown:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 48
    SUB sp, sp, t0
    ; Allocated 48 bytes for 6 local variables
    ; Declaration: prefix
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 15
    PUSH t0
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 67
    ; Assignment to dereferenced address at t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, 111
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 2
    ADD t2, t1, t0
    LDI t0, 117
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 3
    ADD t2, t1, t0
    LDI t0, 110
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 4
    ADD t2, t1, t0
    LDI t0, 116
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 5
    ADD t2, t1, t0
    LDI t0, 100
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 6
    ADD t2, t1, t0
    LDI t0, 111
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 7
    ADD t2, t1, t0
    LDI t0, 119
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 8
    ADD t2, t1, t0
    LDI t0, 110
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 9
    ADD t2, t1, t0
    LDI t0, 58
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 10
    ADD t2, t1, t0
    LDI t0, 32
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 11
    ADD t2, t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Declaration: empty_suffix
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 5
    PUSH t0
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t1
    ST t1, t0
    ; Declaration: countdown
    LDI t0, 50
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_144:
    ; Comparison: Variable(name=countdown) GE LongLiteral(value=0)
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 < t0
    JPN endloop_145, t2
    ; Declaration: message
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 30
    PUSH t0
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: len
    ; Function call: create_message_with_number
    ; Preparing function call: create_message_with_number
    ; Evaluating argument 0
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t0, -24
    ADD t0, fp, t0
    LD t3, t0
    ; Evaluating argument 3
    LDI t0, -16
    ADD t0, fp, t0
    LD t4, t0
    PUSH t4
    PUSH t3
    PUSH t2
    PUSH t1
    JAL create_message_with_number, ra
    LDI t0, 32
    ADD sp, sp, t0
    ; Cleaned up 4 arguments from stack
    MOV t0, a0
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: print
    ; Preparing function call: print
    ; Evaluating argument 0
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -40
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL print, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: console_putc
    ; Preparing function call: console_putc
    ; Evaluating argument 0
    LDI t0, 10
    PUSH t0
    JAL console_putc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Binary operation: SUB
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    SUB t2, t1, t0
    ; Assignment to local variable: countdown
    LDI t0, -24
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: delay
    LDI t0, 0
    LDI t1, -48
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_146:
    ; Comparison: Variable(name=delay) LT LongLiteral(value=100000)
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 100000
    SUB t2, t1, t0
    ; Jump if t1 >= t0
    JPN skip_ge_148, t2
    JMP endloop_147
skip_ge_148:
    ; Binary operation: ADD
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: delay
    LDI t0, -48
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_146
endloop_147:
    JMP loop_144
endloop_145:
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Function epilogue
demo_countdown_end_143:
    LDI t0, 48
    ADD sp, sp, t0
    ; Deallocated 48 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from demo_countdown

    ; ======================================
    ; Function: main
    ; ======================================
main:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; Expression statement
    ; Function call: console_init
    ; Preparing function call: console_init
    JAL console_init, ra
    MOV t0, a0
    ; Expression statement
    ; Function call: demo_countdown
    ; Preparing function call: demo_countdown
    JAL demo_countdown, ra
    MOV t0, a0
    ; While loop
loop_150:
    LDI t0, 1
    JZ endloop_151, t0
    JMP loop_150
endloop_151:
    ; Function epilogue
main_end_149:
    POP fp
    POP ra
    JMP ra                               ; Return from main