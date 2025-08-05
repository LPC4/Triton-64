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
    ; Declaration: a
    ; Unary operation: NEG
    LDI t0, 1
    NEG t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t1
    ; Declaration: fb_start
    LDI t0, 539099136
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: fb_size
    LDI t0, 65536
    LDI t1, -24
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
    ; Declaration: offset
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -32
    ADD t0, fp, t0
    ST t0, t1
    ; While loop
loop_1:
    LDI t0, 1
    JZ endloop_2, t0
    ; Assignment to variable: a
    ; Binary operation: SUB
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    SUB t2, t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; While loop
loop_3:
    ; Comparison: Variable(name=offset) LT BinaryOp(op=ADD, left=Variable(name=fb_size), right=Variable(name=fb_start))
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t3, t0
    ADD t0, t2, t3
    SUB t2, t1, t0
    ; Jump if t1 >= t0
    JPN skip_ge_5, t2
    JMP endloop_4
skip_ge_5:
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    ; Assignment to dereferenced address at t1
    ST t1, t2
    ; Assignment to variable: offset
    ; Binary operation: ADD
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 8
    ADD t2, t1, t0
    LDI t0, -32
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_3
endloop_4:
    ; Assignment to variable: offset
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -32
    ADD t0, fp, t0
    ST t0, t1
    JMP loop_1
endloop_2:
    ; return statement
    JMP main_end_0
    ; Function epilogue
main_end_0:
    LDI t0, 32
    ADD sp, sp, t0
    ; Deallocated 32 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from main