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
    ; Processing function body
    ; Expression statement
    ; Function call: ret
    ; Preparing function call: ret
    JAL ret, ra
    MOV t0, a0
    ; Inline assembly statement
    MOV s1, a0
    LDI a0, 0xDEADBEEF
    ; return statement
    JMP main_end_0
    ; Function epilogue
main_end_0:
    POP fp
    POP ra
    JMP ra                               ; Return from main
    ; Function: ret

    ; =======================================
    ; Function: ret
    ; =======================================
ret:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; Processing function body
    ; return statement
    LDI t0, 5
    MOV a0, t0
    JMP ret_end_1
    ; Function epilogue
ret_end_1:
    POP fp
    POP ra
    JMP ra                               ; Return from ret