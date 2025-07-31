; infinite loop with adding 1 to a register
LDI s1, 0x0000 ; Load immediate value 0 into s1
LDI s2, 0x0001 ; Load immediate value 0 into s2
LDI s3, 0xDEADBEEFDEADBEE ; Load immediate value into s3
PUSH s3, s2, s1
POP s4
.loop:
ADD s1, s1, s2 ; Add 1 to s1
JMP .loop