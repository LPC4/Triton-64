LDI64 sp, 0x20020000 ; Set stack pointer to top of RAM
LDI64 hp, 0x20000000 ; Set heap pointer to start of RAM
LDI64 gp, 0x20000000 ; Set global pointer to start of RAM
LDI64 t5, 0x00020000 ; Set t5 to start of RAM
JMP t5               ; Jump to start of RAM