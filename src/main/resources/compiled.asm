    ; Program entry point
_start:
    JAL main, ra
    HLT

    ; =================================
    ; Function: writeChar
    ; =================================
writeChar:
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
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, 32
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
    LDI t0, 40
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
writeChar_end_0:
    POP fp
    POP ra
    JMP ra                               ; Return from writeChar

    ; ======================================
    ; Function: main
    ; ======================================
main:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 112
    SUB sp, sp, t0
    ; Allocated 112 bytes for 13 local variables
    ; Declaration: framebufferBase
    LDI t0, 539099136
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: framebufferSize
    ; Binary operation: MUL
    LDI t0, 80
    LDI t1, 30
    MUL t2, t0, t1
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: mmioBase
    LDI t0, 537001984
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: queueHeadOffset
    LDI t0, 8
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: queueControlOffset
    LDI t0, 12
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: fgColor
    LDI t0, 15
    LDI t1, -48
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: bgColor
    LDI t0, 0
    LDI t1, -56
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: cursorIndex
    LDI t0, 0
    LDI t1, -64
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: screenWidth
    LDI t0, 80
    LDI t1, -72
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: screenHeight
    LDI t0, 30
    LDI t1, -80
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_2:
    LDI t0, 1
    JZ endloop_3, t0
    ; Declaration: inputChar
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -32
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LD t1, t0
    LDI t0, -88
    ADD t0, fp, t0
    ST t0, t1
    ; If statement
    ; Comparison: Variable(name=inputChar) NE LongLiteral(value=255)
    LDI t0, -88
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 255
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ else_4, t2
    ; If statement
    ; Comparison: Variable(name=inputChar) EQ LongLiteral(value=10)
    LDI t0, -88
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 10
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_6, t2
    ; Assignment to variable: cursorIndex
    ; Binary operation: MUL
    ; Binary operation: ADD
    ; Binary operation: DIV
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -72
    ADD t0, fp, t0
    LD t2, t0
    DIV t0, t1, t2
    LDI t1, 1
    ADD t2, t0, t1
    LDI t0, -72
    ADD t0, fp, t0
    LD t1, t0
    MUL t0, t2, t1
    LDI t1, -64
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=cursorIndex) GE Variable(name=framebufferSize)
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 < t2
    JPN else_8, t0
    ; Assignment to variable: cursorIndex
    LDI t0, 0
    LDI t1, -64
    ADD t1, fp, t1
    ST t1, t0
else_8:
else_6:
    ; If statement
    ; Comparison: Variable(name=inputChar) EQ LongLiteral(value=8)
    LDI t0, -88
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 8
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_10, t2
    ; If statement
    ; Comparison: Variable(name=cursorIndex) GT LongLiteral(value=0)
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 <= t0
    JPP skip_le_14, t2
    JMP else_12
skip_le_14:
    ; Assignment to variable: cursorIndex
    ; Binary operation: SUB
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    SUB t2, t1, t0
    LDI t0, -64
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: addr
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: MUL
    LDI t0, -64
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 4
    MUL t3, t2, t0
    ADD t0, t1, t3
    LDI t1, -96
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: writeChar
    ; Preparing function call: writeChar
    ; Evaluating argument 0
    LDI t0, -96
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, 0
    ; Evaluating argument 2
    LDI t2, -48
    ADD t2, fp, t2
    LD t3, t2
    ; Evaluating argument 3
    LDI t2, -56
    ADD t2, fp, t2
    LD t4, t2
    PUSH t4
    PUSH t3
    PUSH t0
    PUSH t1
    JAL writeChar, ra
    LDI t0, 32
    ADD sp, sp, t0
    ; Cleaned up 4 arguments from stack
    MOV t0, a0
else_12:
else_10:
    ; If statement
    ; Logical AND: BinaryOp(op=NE, left=Variable(name=inputChar), right=LongLiteral(value=10)) && BinaryOp(op=NE, left=Variable(name=inputChar), right=LongLiteral(value=8))
    ; Comparison: Variable(name=inputChar) NE LongLiteral(value=10)
    LDI t0, -88
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 10
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ else_15, t2
    ; Comparison: Variable(name=inputChar) NE LongLiteral(value=8)
    LDI t0, -88
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 8
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ else_15, t2
    ; If statement
    ; Comparison: Variable(name=cursorIndex) LT Variable(name=framebufferSize)
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 >= t2
    JPN skip_ge_19, t0
    JMP else_17
skip_ge_19:
    ; Declaration: addr
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: MUL
    LDI t0, -64
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 4
    MUL t3, t2, t0
    ADD t0, t1, t3
    LDI t1, -96
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: writeChar
    ; Preparing function call: writeChar
    ; Evaluating argument 0
    LDI t0, -96
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -88
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t0, -48
    ADD t0, fp, t0
    LD t3, t0
    ; Evaluating argument 3
    LDI t0, -56
    ADD t0, fp, t0
    LD t4, t0
    PUSH t4
    PUSH t3
    PUSH t2
    PUSH t1
    JAL writeChar, ra
    LDI t0, 32
    ADD sp, sp, t0
    ; Cleaned up 4 arguments from stack
    MOV t0, a0
    ; Assignment to variable: cursorIndex
    ; Binary operation: ADD
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, -64
    ADD t0, fp, t0
    ST t0, t2
else_17:
else_15:
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -40
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, 1
    ; Assignment to dereferenced address at t0
    ST t0, t1
else_4:
    JMP loop_2
endloop_3:
    ; Function epilogue
main_end_1:
    LDI t0, 112
    ADD sp, sp, t0
    ; Deallocated 112 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from main