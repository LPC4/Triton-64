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
    LDI t0, 32
    SUB sp, sp, t0
    ; Allocated 32 bytes for 4 local variables
    ; Processing local variable declarations
    ; Declaration: a
    LDI t0, 20
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: b
    LDI t0, 30
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: d
    LDI t0, 0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: c
    LDI t0, 0
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; Processing function body
    ; If statement
    ; Comparison: Variable(name=a) LT Variable(name=b)
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
    ; Assignment: c
    ; Binary operation: SUB
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    JMP endif_2
else_1:
    ; Assignment: c
    ; Binary operation: SUB
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
endif_2:
    ; While loop
loop_4:
    ; Comparison: Variable(name=c) LT IntegerLiteral(value=100)
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 100
    SUB t2, t1, t0
    ; Jump if t1 >= t0
    JPN skip_ge_6, t2
    JMP endloop_5
skip_ge_6:
    ; Assignment: d
    ; Function call: add
    ; Preparing function call: add
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, 1
    PUSH t1
    PUSH t0
    JAL add, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; Assignment: c
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
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP main_end_0
    ; Function epilogue
main_end_0:
    LDI t0, 32
    ADD sp, sp, t0
    ; Deallocated 32 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from main
    ; Function: add

    ; =======================================
    ; Function: add
    ; =======================================
add:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; Processing function body
    ; return statement
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    MOV a0, t0
    JMP add_end_7
    ; Function epilogue
add_end_7:
    POP fp
    POP ra
    JMP ra                               ; Return from add