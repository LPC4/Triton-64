    ; Program entry point
_start:
    JAL main, ra
    HLT

    ; ======================================
    ; Function: main
    ; ======================================
main:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 48
    SUB sp, sp, t0
    ; Allocated 48 bytes for 6 local variables
    ; Declaration: fb_start
    LDI t0, 539099136
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: i
    LDI t0, 0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_1:
    ; Comparison: Variable(name=i) LT BinaryOp(op=MUL, left=LongLiteral(value=80), right=LongLiteral(value=30))
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: MUL
    LDI t0, 80
    LDI t2, 30
    MUL t3, t0, t2
    SUB t0, t1, t3
    ; Jump if t1 >= t3
    JPN skip_ge_3, t0
    JMP endloop_2
skip_ge_3:
    ; Declaration: ch
    ; Binary operation: ADD
    LDI t0, 97
    LDI t1, -16
    ADD t1, fp, t1
    LD t2, t1
    ADD t1, t0, t2
    LDI t0, -24
    ADD t0, fp, t0
    ST t0, t1
    ; Declaration: fg
    LDI t0, 15
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: bg
    LDI t0, 0
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: addr
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
    LDI t1, -48
    ADD t1, fp, t1
    ST t1, t0
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -24
    ADD t0, fp, t0
    LD t2, t0
    ; Assignment to dereferenced address at t1
    ST t1, t2
    ; Binary operation: ADD
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to dereferenced address at t2
    ST t2, t1
    ; Binary operation: ADD
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 2
    ADD t2, t1, t0
    LDI t0, -40
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to dereferenced address at t2
    ST t2, t1
    ; Binary operation: ADD
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 3
    ADD t2, t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t2
    ST t2, t0
    ; Assignment to variable: i
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_1
endloop_2:
    ; While loop
loop_4:
    LDI t0, 1
    JZ endloop_5, t0
    JMP loop_4
endloop_5:
    ; Function epilogue
main_end_0:
    LDI t0, 48
    ADD sp, sp, t0
    ; Deallocated 48 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from main