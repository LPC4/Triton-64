    ; Program entry point
_start:
    JAL main, ra
    HLT
    ; Function: main

    ; ======================================
    ; Function: main
    ; ======================================
main:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 64
    SUB sp, sp, t0
    ; Allocated 64 bytes for 8 local variables
    ; Processing local variable declarations
    ; Declaration: x
    LDI t0, 5
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: y
    LDI t0, 10
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: z
    LDI t0, 0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: counter
    LDI t0, 0
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: fact
    ; Function call: factorial
    ; Preparing function call: factorial
    ; Evaluating argument 0
    LDI t0, 5
    PUSH t0
    JAL factorial, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: sum
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, -48
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: product
    ; Binary operation: MUL
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    MUL t0, t1, t2
    LDI t1, -56
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: factorial
    ; Function call: factorial
    ; Preparing function call: factorial
    ; Evaluating argument 0
    LDI t0, 4
    PUSH t0
    JAL factorial, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -64
    ADD t1, fp, t1
    ST t1, t0
    ; Processing function body
    ; If statement
    ; Comparison: Variable(name=x) LT Variable(name=y)
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 >= t2
    JPN skip_ge_3, t0
    JMP else_1
skip_ge_3:
    ; Assignment: z
    ; Binary operation: SUB
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    JMP endif_2
else_1:
    ; Assignment: z
    ; Binary operation: SUB
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
endif_2:
    ; While loop
loop_4:
    ; Comparison: Variable(name=counter) LT IntegerLiteral(value=5)
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 5
    SUB t2, t1, t0
    ; Jump if t1 >= t0
    JPN skip_ge_6, t2
    JMP endloop_5
skip_ge_6:
    ; Assignment: counter
    ; Binary operation: ADD
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, -32
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_4
endloop_5:
    ; return statement
    ; Binary operation: ADD
    ; Binary operation: ADD
    ; Binary operation: ADD
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -56
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, -40
    ADD t1, fp, t1
    LD t2, t1
    ADD t1, t0, t2
    LDI t0, -24
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    MOV a0, t0
    JMP main_end_0
    ; Function epilogue
main_end_0:
    LDI t0, 64
    ADD sp, sp, t0
    ; Deallocated 64 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from main
    ; Function: factorial

    ; =================================
    ; Function: factorial
    ; =================================
factorial:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; Processing function body
    ; If statement
    ; Comparison: Variable(name=n) LE IntegerLiteral(value=1)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    SUB t2, t1, t0
    ; Jump if t1 > t0
    JPP else_8, t2
    ; return statement
    LDI t0, 1
    MOV a0, t0
    JMP factorial_end_7
    JMP endif_9
else_8:
    ; return statement
    ; Binary operation: MUL
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Function call: factorial
    ; Preparing function call: factorial
    ; Saving 1 live temporaries
    PUSH t1
    ; Evaluating argument 0
    ; Binary operation: SUB
    LDI t0, 16
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 1
    SUB t3, t2, t0
    PUSH t3
    JAL factorial, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Restoring 1 saved temporaries
    POP t1
    MUL t2, t1, t0
    MOV a0, t2
    JMP factorial_end_7
endif_9:
    ; Function epilogue
factorial_end_7:
    POP fp
    POP ra
    JMP ra                               ; Return from factorial