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
    ; Allocated 32 bytes for 3 local variables
    ; Declaration: b
    ; Unary operation: NEG
    LDI t0, 1
    NEG t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t1
    ; Declaration: a
    LDI t0, 539099136
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    ; Assignment to dereferenced address at t1
    ST t1, t2
    ; Declaration: c
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LD t0, t1
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
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