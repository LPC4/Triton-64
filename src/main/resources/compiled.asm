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
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 1 local variables
    ; Declaration: fb_start
    LDI t0, 539099136
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 3735928559
    ; Assignment to dereferenced address at t1
    ST t1, t0
    ; return statement
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LD t0, t1
    MOV a0, t0
    JMP main_end_0
    ; Function epilogue
main_end_0:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from main