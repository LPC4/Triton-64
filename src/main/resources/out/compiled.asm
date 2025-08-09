    ; Program entry point
_start:
    ; Allocating space for 1 global variables
    ; Allocated global variable 'freeListHead' at GP+0
    LDI t0, 8
    ADD hp, gp, t0
    ; Heap pointer set to SP + 8 for global variables
    ; Initializing global variables
    ; Initializing global 'freeListHead'
    LDI t0, 0
    ST gp, t0
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
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 1 local variables
    ; Declaration: ptr1
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 16
    PUSH t0
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=ptr1) EQ LongLiteral(value=0)
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_1, t2
    ; return statement
    ; Unary operation: NEG
    LDI t0, 1
    NEG t1, t0
    MOV a0, t1
    JMP main_end_0
else_1:
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1311768467463790320
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
write64_end_3:
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
    JMP read64_end_4
    ; Function epilogue
read64_end_4:
    POP fp
    POP ra
    JMP ra                               ; Return from read64

    ; ====================================
    ; Function: malloc
    ; ====================================
malloc:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 80
    SUB sp, sp, t0
    ; Allocated 80 bytes for 9 local variables
    ; If statement
    ; Comparison: Variable(name=freeListHead) EQ LongLiteral(value=0)
    LD t0, gp
    LDI t1, 0
    SUB t2, t0, t1
    ; Jump if t0 != t1
    JNZ else_6, t2
    LDI t0, 402784256
    ; Assignment to global variable: freeListHead
    ST gp, t0
    ; Expression statement
    ; Function call: write64
    ; Preparing function call: write64
    ; Evaluating argument 0
    LD t0, gp
    ; Evaluating argument 1
    ; Binary operation: BITWISE_OR
    ; Binary operation: SHL
    LDI t1, 134217728
    LDI t2, 32
    SHL t3, t1, t2
    LDI t1, 0
    OR t2, t3, t1
    PUSH t2
    PUSH t0
    JAL write64, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
else_6:
    ; Declaration: current
    LD t0, gp
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: prev
    LDI t0, 0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: lowMask
    ; Binary operation: SUB
    ; Binary operation: SHL
    LDI t0, 1
    LDI t1, 32
    SHL t2, t0, t1
    LDI t0, 1
    SUB t1, t2, t0
    LDI t0, -24
    ADD t0, fp, t0
    ST t0, t1
    ; Declaration: highMask
    ; Unary operation: NOT
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    NOT t0, t1
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_8:
    ; Comparison: Variable(name=current) NE LongLiteral(value=0)
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ endloop_9, t2
    ; Declaration: header
    ; Function call: read64
    ; Preparing function call: read64
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL read64, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: blockSize
    ; Binary operation: SHR
    LDI t0, -40
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 32
    SHR t2, t1, t0
    LDI t0, -48
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: nextBlock
    ; Binary operation: BITWISE_AND
    LDI t0, -40
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -24
    ADD t0, fp, t0
    LD t2, t0
    AND t0, t1, t2
    LDI t1, -56
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=blockSize) GE BinaryOp(op=ADD, left=Variable(name=size_req), right=LongLiteral(value=8))
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 8
    ADD t3, t2, t0
    SUB t0, t1, t3
    ; Jump if t1 < t3
    JPN else_10, t0
    ; If statement
    ; Comparison: Variable(name=prev) EQ LongLiteral(value=0)
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_12, t2
    LDI t0, -56
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to global variable: freeListHead
    ST gp, t1
    JMP endif_13
else_12:
    ; Declaration: prevHeader
    ; Function call: read64
    ; Preparing function call: read64
    ; Evaluating argument 0
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL read64, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -64
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: sizePart
    ; Binary operation: BITWISE_AND
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -32
    ADD t0, fp, t0
    LD t2, t0
    AND t0, t1, t2
    LDI t1, -72
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: write64
    ; Preparing function call: write64
    ; Evaluating argument 0
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    ; Binary operation: BITWISE_OR
    LDI t0, -72
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, -56
    ADD t0, fp, t0
    LD t3, t0
    OR t0, t2, t3
    PUSH t0
    PUSH t1
    JAL write64, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
endif_13:
    ; return statement
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 8
    ADD t2, t1, t0
    MOV a0, t2
    JMP malloc_end_5
else_10:
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to local variable: prev
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t1
    LDI t0, -56
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to local variable: current
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t1
    JMP loop_8
endloop_9:
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP malloc_end_5
    ; Function epilogue
malloc_end_5:
    LDI t0, 80
    ADD sp, sp, t0
    ; Deallocated 80 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from malloc

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
    ; Declaration: header
    ; Binary operation: SUB
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 8
    SUB t2, t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: blockSize
    ; Binary operation: SHR
    ; Function call: read64
    ; Preparing function call: read64
    ; Evaluating argument 0
    LDI t0, -8
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
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    ; Expression statement
    ; Function call: write64
    ; Preparing function call: write64
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    ; Binary operation: BITWISE_OR
    ; Binary operation: SHL
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 32
    SHL t3, t2, t0
    LD t0, gp
    OR t2, t3, t0
    PUSH t2
    PUSH t1
    JAL write64, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to global variable: freeListHead
    ST gp, t1
    ; Function epilogue
free_end_14:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from free