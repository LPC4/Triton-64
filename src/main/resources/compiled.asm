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
    LDI t0, 144
    SUB sp, sp, t0
    ; Allocated 144 bytes for 17 local variables
    ; Declaration: fb_start
    LDI t0, 539099136
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: fb_size
    LDI t0, 307200
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: bpp
    LDI t0, 4
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: width
    LDI t0, 320
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: red
    LDI t0, 4294901760
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: blue
    LDI t0, 4278190335
    LDI t1, -48
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: tile_size
    LDI t0, 16
    LDI t1, -56
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: offset
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -64
    ADD t0, fp, t0
    ST t0, t1
    ; Declaration: index
    LDI t0, 0
    LDI t1, -72
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_1:
    LDI t0, 1
    JZ endloop_2, t0
    ; Assignment to variable: offset
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -64
    ADD t0, fp, t0
    ST t0, t1
    ; Assignment to variable: index
    LDI t0, 0
    LDI t1, -72
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_3:
    ; Comparison: Variable(name=offset) LT BinaryOp(op=ADD, left=Variable(name=fb_start), right=Variable(name=fb_size))
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: ADD
    LDI t0, -8
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
    ; Declaration: x
    LDI t0, -72
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -80
    ADD t0, fp, t0
    ST t0, t1
    ; Declaration: y
    LDI t0, 0
    LDI t1, -88
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_6:
    ; Comparison: Variable(name=x) GE Variable(name=width)
    LDI t0, -80
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -32
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 < t2
    JPN endloop_7, t0
    ; Assignment to variable: x
    ; Binary operation: SUB
    LDI t0, -80
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -32
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    LDI t1, -80
    ADD t1, fp, t1
    ST t1, t0
    ; Assignment to variable: y
    ; Binary operation: ADD
    LDI t0, -88
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, -88
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_6
endloop_7:
    ; Declaration: tile_x
    LDI t0, -80
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -96
    ADD t0, fp, t0
    ST t0, t1
    ; Declaration: t_x
    LDI t0, 0
    LDI t1, -104
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_8:
    ; Comparison: Variable(name=tile_x) GE Variable(name=tile_size)
    LDI t0, -96
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -56
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 < t2
    JPN endloop_9, t0
    ; Assignment to variable: tile_x
    ; Binary operation: SUB
    LDI t0, -96
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -56
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    LDI t1, -96
    ADD t1, fp, t1
    ST t1, t0
    ; Assignment to variable: t_x
    ; Binary operation: ADD
    LDI t0, -104
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, -104
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_8
endloop_9:
    ; Declaration: tile_y
    LDI t0, -88
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -112
    ADD t0, fp, t0
    ST t0, t1
    ; Declaration: t_y
    LDI t0, 0
    LDI t1, -120
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_10:
    ; Comparison: Variable(name=tile_y) GE Variable(name=tile_size)
    LDI t0, -112
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -56
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 < t2
    JPN endloop_11, t0
    ; Assignment to variable: tile_y
    ; Binary operation: SUB
    LDI t0, -112
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -56
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    LDI t1, -112
    ADD t1, fp, t1
    ST t1, t0
    ; Assignment to variable: t_y
    ; Binary operation: ADD
    LDI t0, -120
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, -120
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_10
endloop_11:
    ; Declaration: sum
    ; Binary operation: ADD
    LDI t0, -104
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -120
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, -128
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: temp
    LDI t0, -128
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -136
    ADD t0, fp, t0
    ST t0, t1
    ; While loop
loop_12:
    ; Comparison: Variable(name=temp) GE LongLiteral(value=2)
    LDI t0, -136
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 2
    SUB t2, t1, t0
    ; Jump if t1 < t0
    JPN endloop_13, t2
    ; Assignment to variable: temp
    ; Binary operation: SUB
    LDI t0, -136
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 2
    SUB t2, t1, t0
    LDI t0, -136
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_12
endloop_13:
    ; If statement
    ; Comparison: Variable(name=temp) EQ LongLiteral(value=0)
    LDI t0, -136
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_14, t2
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -40
    ADD t0, fp, t0
    LD t2, t0
    ; Assignment to dereferenced address at t1
    ST t1, t2
else_14:
    ; If statement
    ; Comparison: Variable(name=temp) EQ LongLiteral(value=1)
    LDI t0, -136
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_16, t2
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -48
    ADD t0, fp, t0
    LD t2, t0
    ; Assignment to dereferenced address at t1
    ST t1, t2
else_16:
    ; Assignment to variable: index
    ; Binary operation: ADD
    LDI t0, -72
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    LDI t0, -72
    ADD t0, fp, t0
    ST t0, t2
    ; Assignment to variable: offset
    ; Binary operation: ADD
    LDI t0, -64
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -24
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, -64
    ADD t1, fp, t1
    ST t1, t0
    JMP loop_3
endloop_4:
    JMP loop_1
endloop_2:
    ; return statement
    JMP main_end_0
    ; Function epilogue
main_end_0:
    LDI t0, 144
    ADD sp, sp, t0
    ; Deallocated 144 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from main