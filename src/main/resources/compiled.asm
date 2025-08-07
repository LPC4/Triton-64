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
    LDI t0, 64
    SUB sp, sp, t0
    ; Allocated 64 bytes for 8 local variables
    ; Declaration: screenWidth
    LDI t0, 80
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: screenHeight
    LDI t0, 30
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: framebufferBase
    LDI t0, 539099136
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: framebufferSize
    ; Binary operation: MUL
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    MUL t0, t1, t2
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: mmioBase
    LDI t0, 537001984
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: queueHeadOffset
    LDI t0, 8
    LDI t1, -48
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: cursorIndex
    LDI t0, 0
    LDI t1, -56
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_1:
    LDI t0, 1
    JZ endloop_2, t0
    ; Declaration: inputChar
    ; Binary operation: ADD
    LDI t0, -40
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -48
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LD t1, t0
    LDI t0, -64
    ADD t0, fp, t0
    ST t0, t1
    ; If statement
    ; Comparison: Variable(name=inputChar) NE LongLiteral(value=255)
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 255
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ else_3, t2
    ; Assignment to variable: cursorIndex
    ; Function call: handleInput
    ; Preparing function call: handleInput
    ; Evaluating argument 0
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -56
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t0, -24
    ADD t0, fp, t0
    LD t3, t0
    ; Evaluating argument 3
    LDI t0, -32
    ADD t0, fp, t0
    LD t4, t0
    ; Evaluating argument 4
    LDI t0, -8
    ADD t0, fp, t0
    LD t5, t0
    PUSH t5
    PUSH t4
    PUSH t3
    PUSH t2
    PUSH t1
    JAL handleInput, ra
    LDI t0, 40
    ADD sp, sp, t0
    ; Cleaned up 5 arguments from stack
    MOV t0, a0
    LDI t1, -56
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: confirmInputProcessed
    ; Preparing function call: confirmInputProcessed
    JAL confirmInputProcessed, ra
    MOV t0, a0
else_3:
    JMP loop_1
endloop_2:
    ; Function epilogue
main_end_0:
    LDI t0, 64
    ADD sp, sp, t0
    ; Deallocated 64 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from main

    ; ===============================
    ; Function: handleInput
    ; ===============================
handleInput:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; If statement
    ; Comparison: Variable(name=char) EQ LongLiteral(value=10)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 10
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_6, t2
    ; Assignment to variable: cursorIndex
    ; Function call: handleNewline
    ; Preparing function call: handleNewline
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, 40
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t0, 48
    ADD t0, fp, t0
    LD t3, t0
    PUSH t3
    PUSH t2
    PUSH t1
    JAL handleNewline, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
    LDI t1, 24
    ADD t1, fp, t1
    ST t1, t0
else_6:
    ; If statement
    ; Comparison: Variable(name=char) EQ LongLiteral(value=8)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 8
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_8, t2
    ; Assignment to variable: cursorIndex
    ; Function call: handleBackspace
    ; Preparing function call: handleBackspace
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, 32
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL handleBackspace, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    LDI t1, 24
    ADD t1, fp, t1
    ST t1, t0
else_8:
    ; If statement
    ; Logical AND: BinaryOp(op=NE, left=Variable(name=char), right=LongLiteral(value=10)) && BinaryOp(op=NE, left=Variable(name=char), right=LongLiteral(value=8))
    ; Comparison: Variable(name=char) NE LongLiteral(value=10)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 10
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ else_10, t2
    ; Comparison: Variable(name=char) NE LongLiteral(value=8)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 8
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ else_10, t2
    ; Assignment to variable: cursorIndex
    ; Function call: handleNormalChar
    ; Preparing function call: handleNormalChar
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t0, 32
    ADD t0, fp, t0
    LD t3, t0
    ; Evaluating argument 3
    LDI t0, 40
    ADD t0, fp, t0
    LD t4, t0
    PUSH t4
    PUSH t3
    PUSH t2
    PUSH t1
    JAL handleNormalChar, ra
    LDI t0, 32
    ADD sp, sp, t0
    ; Cleaned up 4 arguments from stack
    MOV t0, a0
    LDI t1, 24
    ADD t1, fp, t1
    ST t1, t0
else_10:
    ; return statement
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP handleInput_end_5
    ; Function epilogue
handleInput_end_5:
    POP fp
    POP ra
    JMP ra                               ; Return from handleInput

    ; =============================
    ; Function: handleNewline
    ; =============================
handleNewline:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; Assignment to variable: cursorIndex
    ; Binary operation: MUL
    ; Binary operation: ADD
    ; Binary operation: DIV
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 32
    ADD t0, fp, t0
    LD t2, t0
    DIV t0, t1, t2
    LDI t1, 1
    ADD t2, t0, t1
    LDI t0, 32
    ADD t0, fp, t0
    LD t1, t0
    MUL t0, t2, t1
    LDI t1, 16
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=cursorIndex) GE Variable(name=framebufferSize)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 < t2
    JPN else_13, t0
    ; Assignment to variable: cursorIndex
    LDI t0, 0
    LDI t1, 16
    ADD t1, fp, t1
    ST t1, t0
else_13:
    ; return statement
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP handleNewline_end_12
    ; Function epilogue
handleNewline_end_12:
    POP fp
    POP ra
    JMP ra                               ; Return from handleNewline

    ; ===========================
    ; Function: handleBackspace
    ; ===========================
handleBackspace:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 1 local variables
    ; If statement
    ; Comparison: Variable(name=cursorIndex) GT LongLiteral(value=0)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 <= t0
    JPP skip_le_18, t2
    JMP else_16
skip_le_18:
    ; Assignment to variable: cursorIndex
    ; Binary operation: SUB
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    SUB t2, t1, t0
    LDI t0, 16
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: addr
    ; Binary operation: ADD
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: MUL
    LDI t0, 16
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 4
    MUL t3, t2, t0
    ADD t0, t1, t3
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: writeChar
    ; Preparing function call: writeChar
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, 0
    PUSH t0
    PUSH t1
    JAL writeChar, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
else_16:
    ; return statement
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP handleBackspace_end_15
    ; Function epilogue
handleBackspace_end_15:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from handleBackspace

    ; ==========================
    ; Function: handleNormalChar
    ; ==========================
handleNormalChar:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 1 local variables
    ; If statement
    ; Comparison: Variable(name=cursorIndex) LT Variable(name=framebufferSize)
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 40
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 >= t2
    JPN skip_ge_22, t0
    JMP else_20
skip_ge_22:
    ; Declaration: addr
    ; Binary operation: ADD
    LDI t0, 32
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: MUL
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 4
    MUL t3, t2, t0
    ADD t0, t1, t3
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: writeChar
    ; Preparing function call: writeChar
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
    JAL writeChar, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Assignment to variable: cursorIndex
    ; Binary operation: ADD
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    ST t0, t2
else_20:
    ; return statement
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP handleNormalChar_end_19
    ; Function epilogue
handleNormalChar_end_19:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from handleNormalChar

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
writeChar_end_23:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from writeChar

    ; =====================
    ; Function: confirmInputProcessed
    ; =====================
confirmInputProcessed:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 2 local variables
    ; Declaration: mmioBase
    LDI t0, 537001984
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: queueControlOffset
    LDI t0, 12
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, 1
    ; Assignment to dereferenced address at t0
    ST t0, t1
    ; Function epilogue
confirmInputProcessed_end_24:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from confirmInputProcessed