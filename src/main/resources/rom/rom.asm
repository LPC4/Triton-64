; ROM initialization code for a hypothetical system
; This code sets up the stack pointer, heap pointer, global pointer, and jumps to the
; start of RAM for execution.
; It uses unsafe loads that clobber temporary registers, so don't use those in ROM code.
; you can use the temps in runtime code, if you use safe loads (LDI) instead of unsafe loads (LDIU).

LDIU sp, 0x2001FFF7 ; Set stack pointer to top of stack (RAM end - 8 bytes)
LDIU hp, 0x20000000 ; Set heap pointer to start of heap-stack area
LDIU gp, 0x20000000 ; Set global pointer to start of heap-stack area
LDIU t6, 0x00020000 ; Set t6 to start of RAM
JMP t6              ; Jump to start of RAM, we're jumping to register so no temps clobbered
