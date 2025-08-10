    ; Program entry point
_start:
    ; Allocating space for 6 global variables
    ; Allocated global variable 'freeListHead' at GP+0
    ; Allocated global variable 'HEAP_START' at GP+8
    ; Allocated global variable 'HEAP_SIZE' at GP+16
    ; Allocated global variable 'HEADER_SIZE' at GP+24
    ; Allocated global variable 'MIN_BLOCK_SIZE' at GP+32
    ; Allocated global variable 'ALIGNMENT' at GP+40
    LDI t0, 48
    ADD hp, gp, t0
    ; Heap pointer set to SP + 48 for global variables
    ; Initializing global variables
    ; Initializing global 'freeListHead'
    LDI t0, 0
    ST gp, t0
    ; Initializing global 'HEAP_START'
    LDI t0, 402784256
    LDI t1, 8
    ADD t1, gp, t1
    ST t1, t0
    ; Initializing global 'HEAP_SIZE'
    LDI t0, 134217728
    LDI t1, 16
    ADD t1, gp, t1
    ST t1, t0
    ; Initializing global 'HEADER_SIZE'
    LDI t0, 8
    LDI t1, 24
    ADD t1, gp, t1
    ST t1, t0
    ; Initializing global 'MIN_BLOCK_SIZE'
    LDI t0, 16
    LDI t1, 32
    ADD t1, gp, t1
    ST t1, t0
    ; Initializing global 'ALIGNMENT'
    LDI t0, 8
    LDI t1, 40
    ADD t1, gp, t1
    ST t1, t0
    JAL main, ra
    HLT

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
write64_end_0:
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
    JMP read64_end_1
    ; Function epilogue
read64_end_1:
    POP fp
    POP ra
    JMP ra                               ; Return from read64

    ; ==================================
    ; Function: align_up
    ; ==================================
align_up:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; return statement
    ; Binary operation: BITWISE_AND
    ; Binary operation: SUB
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, 1
    SUB t2, t0, t1
    ; Unary operation: NOT
    ; Binary operation: SUB
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    SUB t3, t1, t0
    NOT t0, t3
    AND t1, t2, t0
    MOV a0, t1
    JMP align_up_end_2
    ; Function epilogue
align_up_end_2:
    POP fp
    POP ra
    JMP ra                               ; Return from align_up

    ; ============================
    ; Function: get_block_size
    ; ============================
get_block_size:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; return statement
    ; Binary operation: SHR
    ; Function call: read64
    ; Preparing function call: read64
    ; Evaluating argument 0
    LDI t0, 16
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
    MOV a0, t2
    JMP get_block_size_end_3
    ; Function epilogue
get_block_size_end_3:
    POP fp
    POP ra
    JMP ra                               ; Return from get_block_size

    ; ============================
    ; Function: get_next_block
    ; ============================
get_next_block:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; return statement
    ; Binary operation: BITWISE_AND
    ; Function call: read64
    ; Preparing function call: read64
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL read64, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Binary operation: SUB
    ; Binary operation: SHL
    LDI t1, 1
    LDI t2, 32
    SHL t3, t1, t2
    LDI t1, 1
    SUB t2, t3, t1
    AND t1, t0, t2
    MOV a0, t1
    JMP get_next_block_end_4
    ; Function epilogue
get_next_block_end_4:
    POP fp
    POP ra
    JMP ra                               ; Return from get_next_block

    ; ================================
    ; Function: set_header
    ; ================================
set_header:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; Expression statement
    ; Function call: write64
    ; Preparing function call: write64
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    ; Binary operation: BITWISE_OR
    ; Binary operation: SHL
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, 32
    SHL t3, t2, t0
    LDI t0, 32
    ADD t0, fp, t0
    LD t2, t0
    OR t0, t3, t2
    PUSH t0
    PUSH t1
    JAL write64, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Function epilogue
set_header_end_5:
    POP fp
    POP ra
    JMP ra                               ; Return from set_header

    ; =================================
    ; Function: init_heap
    ; =================================
init_heap:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    ; If statement
    ; Comparison: Variable(name=freeListHead) EQ LongLiteral(value=0)
    LD t0, gp
    LDI t1, 0
    SUB t2, t0, t1
    ; Jump if t0 != t1
    JNZ else_7, t2
    LDI t1, 8
    ADD t1, gp, t1
    LD t0, t1
    ; Assignment to global variable: freeListHead
    ST gp, t0
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LD t0, gp
    ; Evaluating argument 1
    LDI t2, 16
    ADD t2, gp, t2
    LD t1, t2
    ; Evaluating argument 2
    LDI t2, 0
    PUSH t2
    PUSH t1
    PUSH t0
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
else_7:
    ; Function epilogue
init_heap_end_6:
    POP fp
    POP ra
    JMP ra                               ; Return from init_heap

    ; ==============================
    ; Function: unlink_block
    ; ==============================
unlink_block:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 2 local variables
    ; Declaration: next
    ; Function call: get_next_block
    ; Preparing function call: get_next_block
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_next_block, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=prev) EQ LongLiteral(value=0)
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_10, t2
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to global variable: freeListHead
    ST gp, t1
    JMP endif_11
else_10:
    ; Declaration: prev_size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t0, -8
    ADD t0, fp, t0
    LD t3, t0
    PUSH t3
    PUSH t2
    PUSH t1
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
endif_11:
    ; Function epilogue
unlink_block_end_9:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from unlink_block

    ; ===============================
    ; Function: split_block
    ; ===============================
split_block:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 32
    SUB sp, sp, t0
    ; Allocated 32 bytes for 3 local variables
    ; Declaration: total_size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: remaining
    ; Binary operation: SUB
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=remaining) GE Variable(name=MIN_BLOCK_SIZE)
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 32
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    ; Jump if t1 < t0
    JPN else_13, t2
    ; Declaration: new_block
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LD t0, gp
    PUSH t0
    PUSH t2
    PUSH t1
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to global variable: freeListHead
    ST gp, t1
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t0, 0
    PUSH t0
    PUSH t2
    PUSH t1
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
    JMP endif_14
else_13:
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LDI t0, 0
    PUSH t0
    PUSH t2
    PUSH t1
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
endif_14:
    ; Function epilogue
split_block_end_12:
    LDI t0, 32
    ADD sp, sp, t0
    ; Deallocated 32 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from split_block

    ; ====================================
    ; Function: malloc
    ; ====================================
malloc:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 48
    SUB sp, sp, t0
    ; Allocated 48 bytes for 6 local variables
    ; If statement
    ; Comparison: Variable(name=size_req) EQ LongLiteral(value=0)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_16, t2
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP malloc_end_15
else_16:
    ; Expression statement
    ; Function call: init_heap
    ; Preparing function call: init_heap
    JAL init_heap, ra
    MOV t0, a0
    ; Declaration: aligned_size
    ; Function call: align_up
    ; Preparing function call: align_up
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t2, 40
    ADD t2, gp, t2
    LD t0, t2
    PUSH t0
    PUSH t1
    JAL align_up, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: total_needed
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 24
    ADD t2, gp, t2
    LD t0, t2
    ADD t2, t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: current
    LD t0, gp
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: prev
    LDI t0, 0
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_18:
    ; Comparison: Variable(name=current) NE LongLiteral(value=0)
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ endloop_19, t2
    ; Declaration: block_size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: next_block
    ; Function call: get_next_block
    ; Preparing function call: get_next_block
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_next_block, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -48
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=block_size) GE Variable(name=total_needed)
    LDI t0, -40
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 < t2
    JPN else_20, t0
    ; Expression statement
    ; Function call: unlink_block
    ; Preparing function call: unlink_block
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -32
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL unlink_block, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; Expression statement
    ; Function call: split_block
    ; Preparing function call: split_block
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    PUSH t2
    PUSH t1
    JAL split_block, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    ; return statement
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 24
    ADD t2, gp, t2
    LD t0, t2
    ADD t2, t1, t0
    MOV a0, t2
    JMP malloc_end_15
else_20:
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to local variable: prev
    LDI t0, -32
    ADD t0, fp, t0
    ST t0, t1
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to local variable: current
    LDI t0, -24
    ADD t0, fp, t0
    ST t0, t1
    JMP loop_18
endloop_19:
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP malloc_end_15
    ; Function epilogue
malloc_end_15:
    LDI t0, 48
    ADD sp, sp, t0
    ; Deallocated 48 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from malloc

    ; ==============================
    ; Function: is_valid_ptr
    ; ==============================
is_valid_ptr:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 32
    SUB sp, sp, t0
    ; Allocated 32 bytes for 3 local variables
    ; If statement
    ; Comparison: Variable(name=ptr) EQ LongLiteral(value=0)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_23, t2
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP is_valid_ptr_end_22
else_23:
    ; Declaration: header_addr
    ; Binary operation: SUB
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 24
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; If statement
    ; Logical OR: BinaryOp(op=LT, left=Variable(name=header_addr), right=Variable(name=HEAP_START)) || BinaryOp(op=GE, left=Variable(name=header_addr), right=BinaryOp(op=ADD, left=Variable(name=HEAP_START), right=Variable(name=HEAP_SIZE)))
    ; Comparison: Variable(name=header_addr) LT Variable(name=HEAP_START)
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 8
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    ; Jump if t1 >= t0
    JPN skip_ge_28, t2
    JMP or_eval_right_27
skip_ge_28:
    JMP or_skip_29
or_eval_right_27:
    ; Comparison: Variable(name=header_addr) GE BinaryOp(op=ADD, left=Variable(name=HEAP_START), right=Variable(name=HEAP_SIZE))
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: ADD
    LDI t2, 8
    ADD t2, gp, t2
    LD t0, t2
    LDI t3, 16
    ADD t3, gp, t3
    LD t2, t3
    ADD t3, t0, t2
    SUB t0, t1, t3
    ; Jump if t1 < t3
    JPN else_25, t0
or_skip_29:
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP is_valid_ptr_end_22
else_25:
    ; Declaration: size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Logical OR: BinaryOp(op=LT, left=Variable(name=size), right=Variable(name=MIN_BLOCK_SIZE)) || BinaryOp(op=GT, left=Variable(name=size), right=Variable(name=HEAP_SIZE))
    ; Comparison: Variable(name=size) LT Variable(name=MIN_BLOCK_SIZE)
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 32
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    ; Jump if t1 >= t0
    JPN skip_ge_33, t2
    JMP or_eval_right_32
skip_ge_33:
    JMP or_skip_34
or_eval_right_32:
    ; Comparison: Variable(name=size) GT Variable(name=HEAP_SIZE)
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 16
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    ; Jump if t1 <= t0
    JPP skip_le_35, t2
    JMP else_30
skip_le_35:
or_skip_34:
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP is_valid_ptr_end_22
else_30:
    ; Declaration: next
    ; Function call: get_next_block
    ; Preparing function call: get_next_block
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_next_block, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=next) NE LongLiteral(value=0)
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ else_36, t2
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP is_valid_ptr_end_22
else_36:
    ; return statement
    LDI t0, 1
    MOV a0, t0
    JMP is_valid_ptr_end_22
    ; Function epilogue
is_valid_ptr_end_22:
    LDI t0, 32
    ADD sp, sp, t0
    ; Deallocated 32 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from is_valid_ptr

    ; ======================
    ; Function: coalesce_free_blocks
    ; ======================
coalesce_free_blocks:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 48
    SUB sp, sp, t0
    ; Allocated 48 bytes for 5 local variables
    ; If statement
    ; Comparison: Variable(name=freeListHead) EQ LongLiteral(value=0)
    LD t0, gp
    LDI t1, 0
    SUB t2, t0, t1
    ; Jump if t0 != t1
    JNZ else_39, t2
    ; return statement
    JMP coalesce_free_blocks_end_38
else_39:
    ; Declaration: current
    LD t0, gp
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_41:
    ; Comparison: Variable(name=current) NE LongLiteral(value=0)
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ endloop_42, t2
    ; Declaration: current_size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: next
    ; Function call: get_next_block
    ; Preparing function call: get_next_block
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_next_block, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Logical AND: BinaryOp(op=NE, left=Variable(name=next), right=LongLiteral(value=0)) && BinaryOp(op=EQ, left=Variable(name=next), right=BinaryOp(op=ADD, left=Variable(name=current), right=Variable(name=current_size)))
    ; Comparison: Variable(name=next) NE LongLiteral(value=0)
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ else_43, t2
    ; Comparison: Variable(name=next) EQ BinaryOp(op=ADD, left=Variable(name=current), right=Variable(name=current_size))
    LDI t0, -24
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
    ; Jump if t1 != t0
    JNZ else_43, t2
    ; Declaration: next_size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: next_next
    ; Function call: get_next_block
    ; Preparing function call: get_next_block
    ; Evaluating argument 0
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_next_block, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    LDI t0, -32
    ADD t0, fp, t0
    LD t3, t0
    ADD t0, t2, t3
    ; Evaluating argument 2
    LDI t2, -40
    ADD t2, fp, t2
    LD t3, t2
    PUSH t3
    PUSH t0
    PUSH t1
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
    JMP endif_44
else_43:
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to local variable: current
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t1
endif_44:
    JMP loop_41
endloop_42:
    ; Function epilogue
coalesce_free_blocks_end_38:
    LDI t0, 48
    ADD sp, sp, t0
    ; Deallocated 48 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from coalesce_free_blocks

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
    ; If statement
    ; Binary operation: BITWISE_AND
    ; Unary operation: NOT
    ; Function call: is_valid_ptr
    ; Preparing function call: is_valid_ptr
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL is_valid_ptr, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    NOT t1, t0
    LDI t0, 1
    AND t2, t1, t0
    JZ else_46, t2
    ; return statement
    JMP free_end_45
else_46:
    ; Declaration: header_addr
    ; Binary operation: SUB
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 24
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: block_size
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; Expression statement
    ; Function call: set_header
    ; Preparing function call: set_header
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    ; Evaluating argument 2
    LD t0, gp
    PUSH t0
    PUSH t2
    PUSH t1
    JAL set_header, ra
    LDI t0, 24
    ADD sp, sp, t0
    ; Cleaned up 3 arguments from stack
    MOV t0, a0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to global variable: freeListHead
    ST gp, t1
    ; Expression statement
    ; Function call: coalesce_free_blocks
    ; Preparing function call: coalesce_free_blocks
    JAL coalesce_free_blocks, ra
    MOV t0, a0
    ; Function epilogue
free_end_45:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from free

    ; ===================================
    ; Function: realloc
    ; ===================================
realloc:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 48
    SUB sp, sp, t0
    ; Allocated 48 bytes for 6 local variables
    ; If statement
    ; Comparison: Variable(name=ptr) EQ LongLiteral(value=0)
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_49, t2
    ; return statement
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    MOV a0, t0
    JMP realloc_end_48
else_49:
    ; If statement
    ; Comparison: Variable(name=new_size) EQ LongLiteral(value=0)
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_51, t2
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP realloc_end_48
else_51:
    ; If statement
    ; Binary operation: BITWISE_AND
    ; Unary operation: NOT
    ; Function call: is_valid_ptr
    ; Preparing function call: is_valid_ptr
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL is_valid_ptr, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    NOT t1, t0
    LDI t0, 1
    AND t2, t1, t0
    JZ else_53, t2
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP realloc_end_48
else_53:
    ; Declaration: header_addr
    ; Binary operation: SUB
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t2, 24
    ADD t2, gp, t2
    LD t0, t2
    SUB t2, t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: current_size
    ; Binary operation: SUB
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_block_size, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t2, 24
    ADD t2, gp, t2
    LD t1, t2
    SUB t2, t0, t1
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t2
    ; Declaration: aligned_new_size
    ; Function call: align_up
    ; Preparing function call: align_up
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    ; Evaluating argument 1
    LDI t2, 40
    ADD t2, gp, t2
    LD t0, t2
    PUSH t0
    PUSH t1
    JAL align_up, ra
    LDI t0, 16
    ADD sp, sp, t0
    ; Cleaned up 2 arguments from stack
    MOV t0, a0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=aligned_new_size) LE Variable(name=current_size)
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -16
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 > t2
    JPP else_55, t0
    ; return statement
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP realloc_end_48
else_55:
    ; Declaration: new_ptr
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=new_ptr) EQ LongLiteral(value=0)
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_57, t2
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP realloc_end_48
else_57:
    ; Declaration: copy_size
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -40
    ADD t0, fp, t0
    ST t0, t1
    ; If statement
    ; Comparison: Variable(name=aligned_new_size) LT Variable(name=copy_size)
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -40
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 >= t2
    JPN skip_ge_61, t0
    JMP else_59
skip_ge_61:
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    ; Assignment to local variable: copy_size
    LDI t0, -40
    ADD t0, fp, t0
    ST t0, t1
else_59:
    ; Declaration: i
    LDI t0, 0
    LDI t1, -48
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_62:
    ; Comparison: Variable(name=i) LT Variable(name=copy_size)
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -40
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 >= t2
    JPN skip_ge_64, t0
    JMP endloop_63
skip_ge_64:
    ; Binary operation: ADD
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -48
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    ; Binary operation: ADD
    LDI t1, 16
    ADD t1, fp, t1
    LD t2, t1
    LDI t1, -48
    ADD t1, fp, t1
    LD t3, t1
    ADD t1, t2, t3
    LD t2, t1
    ; Assignment to dereferenced address at t0
    ST t0, t2
    ; Binary operation: ADD
    LDI t0, -48
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: i
    LDI t0, -48
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_62
endloop_63:
    ; Expression statement
    ; Function call: free
    ; Preparing function call: free
    ; Evaluating argument 0
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL free, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; return statement
    LDI t0, -32
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP realloc_end_48
    ; Function epilogue
realloc_end_48:
    LDI t0, 48
    ADD sp, sp, t0
    ; Deallocated 48 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from realloc

    ; ====================================
    ; Function: calloc
    ; ====================================
calloc:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 32
    SUB sp, sp, t0
    ; Allocated 32 bytes for 3 local variables
    ; Declaration: total_size
    ; Binary operation: MUL
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t2, t0
    MUL t0, t1, t2
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; Declaration: ptr
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -16
    ADD t1, fp, t1
    ST t1, t0
    ; If statement
    ; Comparison: Variable(name=ptr) EQ LongLiteral(value=0)
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 != t0
    JNZ else_66, t2
    ; return statement
    LDI t0, 0
    MOV a0, t0
    JMP calloc_end_65
else_66:
    ; Declaration: i
    LDI t0, 0
    LDI t1, -24
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_68:
    ; Comparison: Variable(name=i) LT Variable(name=total_size)
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t2, t0
    SUB t0, t1, t2
    ; Jump if t1 >= t2
    JPN skip_ge_70, t0
    JMP endloop_69
skip_ge_70:
    ; Binary operation: ADD
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, -24
    ADD t0, fp, t0
    LD t2, t0
    ADD t0, t1, t2
    LDI t1, 0
    ; Assignment to dereferenced address at t0
    ST t0, t1
    ; Binary operation: ADD
    LDI t0, -24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    ADD t2, t1, t0
    ; Assignment to local variable: i
    LDI t0, -24
    ADD t0, fp, t0
    ST t0, t2
    JMP loop_68
endloop_69:
    ; return statement
    LDI t0, -16
    ADD t0, fp, t0
    LD t1, t0
    MOV a0, t1
    JMP calloc_end_65
    ; Function epilogue
calloc_end_65:
    LDI t0, 32
    ADD sp, sp, t0
    ; Deallocated 32 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from calloc

    ; ============================
    ; Function: get_heap_stats
    ; ============================
get_heap_stats:
    ; Function prologue
    PUSH ra
    PUSH fp
    MOV fp, sp
    LDI t0, 16
    SUB sp, sp, t0
    ; Allocated 16 bytes for 1 local variables
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t1
    ST t1, t0
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t1
    ST t1, t0
    LDI t0, 32
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    ; Assignment to dereferenced address at t1
    ST t1, t0
    ; Declaration: current
    LD t0, gp
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_72:
    ; Comparison: Variable(name=current) NE LongLiteral(value=0)
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    SUB t2, t1, t0
    ; Jump if t1 == t0
    JZ endloop_73, t2
    LDI t0, 16
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: ADD
    LDI t0, 16
    ADD t0, fp, t0
    LD t2, t0
    LD t0, t2
    ; Function call: get_block_size
    ; Preparing function call: get_block_size
    ; Saving 2 live temporaries
    PUSH t1
    PUSH t0
    ; Evaluating argument 0
    LDI t2, -8
    ADD t2, fp, t2
    LD t3, t2
    PUSH t3
    JAL get_block_size, ra
    LDI t2, 8
    ADD sp, sp, t2
    ; Cleaned up 1 arguments from stack
    MOV t2, a0
    ; Restoring 2 saved temporaries
    POP t0
    POP t1
    ADD t3, t0, t2
    ; Assignment to dereferenced address at t1
    ST t1, t3
    LDI t0, 32
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: ADD
    LDI t0, 32
    ADD t0, fp, t0
    LD t2, t0
    LD t0, t2
    LDI t2, 1
    ADD t3, t0, t2
    ; Assignment to dereferenced address at t1
    ST t1, t3
    ; Function call: get_next_block
    ; Preparing function call: get_next_block
    ; Evaluating argument 0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    PUSH t1
    JAL get_next_block, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    ; Assignment to local variable: current
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    JMP loop_72
endloop_73:
    LDI t0, 24
    ADD t0, fp, t0
    LD t1, t0
    ; Binary operation: SUB
    LDI t2, 16
    ADD t2, gp, t2
    LD t0, t2
    LDI t2, 16
    ADD t2, fp, t2
    LD t3, t2
    LD t2, t3
    SUB t3, t0, t2
    ; Assignment to dereferenced address at t1
    ST t1, t3
    ; Function epilogue
get_heap_stats_end_71:
    LDI t0, 16
    ADD sp, sp, t0
    ; Deallocated 16 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from get_heap_stats

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
    ; Allocated 48 bytes for 5 local variables
    ; Declaration: ptr
    ; Function call: malloc
    ; Preparing function call: malloc
    ; Evaluating argument 0
    LDI t0, 40
    PUSH t0
    JAL malloc, ra
    LDI t0, 8
    ADD sp, sp, t0
    ; Cleaned up 1 arguments from stack
    MOV t0, a0
    LDI t1, -8
    ADD t1, fp, t1
    ST t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    ; Storing array literal to memory at t1
    LDI t0, 0
    ADD t2, t1, t0
    LDI t3, 1
    ST t2, t3
    LDI t0, 8
    ADD t2, t1, t0
    LDI t3, 2
    ST t2, t3
    LDI t0, 16
    ADD t2, t1, t0
    LDI t3, 3
    ST t2, t3
    LDI t0, 24
    ADD t2, t1, t0
    LDI t3, 4
    ST t2, t3
    LDI t0, 32
    ADD t2, t1, t0
    LDI t3, 5
    ST t2, t3
    ; Declaration: first
    ; Array indexing
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    LDI t2, 8
    MUL t2, t0, t2
    ADD t3, t1, t2
    LD t4, t3
    LDI t0, -16
    ADD t0, fp, t0
    ST t0, t4
    ; Declaration: second
    ; Array indexing
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    LDI t2, 8
    MUL t2, t0, t2
    ADD t3, t1, t2
    LD t4, t3
    LDI t0, -24
    ADD t0, fp, t0
    ST t0, t4
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 2
    LDI t2, 100
    ; Array index assignment
    LDI t3, 8
    MUL t3, t0, t3
    ADD t4, t1, t3
    ST t4, t2
    ; Declaration: sum
    ; Binary operation: ADD
    ; Array indexing
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 0
    LDI t2, 8
    MUL t2, t0, t2
    ADD t3, t1, t2
    LD t4, t3
    ; Array indexing
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 1
    LDI t2, 8
    MUL t2, t0, t2
    ADD t3, t1, t2
    LD t5, t3
    ADD t0, t4, t5
    LDI t1, -32
    ADD t1, fp, t1
    ST t1, t0
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 4
    ; Binary operation: MUL
    ; Array indexing
    LDI t2, -8
    ADD t2, fp, t2
    LD t3, t2
    LDI t2, 0
    LDI t4, 8
    MUL t4, t2, t4
    ADD t5, t3, t4
    LD t6, t5
    LDI t2, 10
    MUL t3, t6, t2
    ; Array index assignment
    LDI t2, 8
    MUL t2, t0, t2
    ADD t4, t1, t2
    ST t4, t3
    ; Declaration: alt
    ; Binary operation: ADD
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 16
    ADD t2, t1, t0
    LD t0, t2
    LDI t1, -40
    ADD t1, fp, t1
    ST t1, t0
    ; While loop
loop_75:
    LDI t0, 1
    JZ endloop_76, t0
    JMP loop_75
endloop_76:
    ; return statement
    ; Array indexing
    LDI t0, -8
    ADD t0, fp, t0
    LD t1, t0
    LDI t0, 4
    LDI t2, 8
    MUL t2, t0, t2
    ADD t3, t1, t2
    LD t4, t3
    MOV a0, t4
    JMP main_end_74
    ; Function epilogue
main_end_74:
    LDI t0, 48
    ADD sp, sp, t0
    ; Deallocated 48 bytes for frame
    POP fp
    POP ra
    JMP ra                               ; Return from main