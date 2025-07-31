; infinite loop with adding 1 to a register
LDI s1, 0x0000 ; Load immediate value 0 into s1
LDI s2, 0x0001 ; Load immediate value 0 into s2
LDI16 s3, 0xDEAD
.loop:
ADD s1, s1, s2 ; Add 1 to s1
JMP .loop