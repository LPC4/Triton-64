    JMP main
add:
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, -1
    ADD t0, fp, t0
    ST t0, a0
    LDI t1, -2
    ADD t1, fp, t1
    ST t1, a1
    ; return
    ; Binary Operation: ADD between Variable(name=a) and Variable(name=b)
    LDI t2, -1
    ADD t2, fp, t2
    LD t3, t2
    LDI t4, -2
    ADD t4, fp, t4
    LD t5, t4
    ADD t6, t3, t5
    MOV a0, t6
    JMP add_end_0
add_end_0:
    MOV sp, fp
    POP fp
    POP ra
    JMP ra, ; Return to caller
main:
    MOV fp, sp
    LDI t0, 5
    SUB sp, sp, t0
    ; Declaration: a
    LDI t1, 5
    LDI t2, 1
    ADD t2, fp, t2
    ST t2, t1
    ; Declaration: b
    LDI t3, 10
    LDI t4, 2
    ADD t4, fp, t4
    ST t4, t3
    ; Declaration: c
    LDI t5, 0
    LDI t6, 3
    ADD t6, fp, t6
    ST t6, t5
    ; Declaration: d
    LDI t7, 256
    LDI t8, 4
    ADD t8, fp, t8
    ST t8, t7
    ; Declaration: e
    LDI t9, 0
    LDI t0, 5
    ADD t0, fp, t0
    ST t0, t9
    ; While loop
loop_2:
    LDI t2, 1
    JZ endloop_3, t2
    ; Assignment: e
    ; Binary Operation: ADD between Variable(name=e) and IntegerLiteral(value=1)
    LDI t1, 5
    ADD t1, fp, t1
    LD t4, t1
    LDI t3, 1
    ADD t6, t4, t3
    LDI t5, 5
    ADD t5, fp, t5
    ST t5, t6
    JMP loop_2
endloop_3:
    ; return
    JMP main_end_1
main_end_1:
    HLT ; End of main