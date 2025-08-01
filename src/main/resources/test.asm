; infinite loop with adding 1 to a register
LDI s1, 0x0000 ; Load immediate value 0 into s1
LDI s2, 0x0001 ; Load immediate value 0 into s2
LDI s3, 0xDEADBEEFDEADBEE ; Load immediate value into s3
.loop:
ADD s4, s4, 1     ; Increment s4 by 1 (this is just to show that the loop continues)
PUSH s4
POP s5
MOV s1, s5
JMP .loop