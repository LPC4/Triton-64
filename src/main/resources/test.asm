; Sample LPC Assembly Program
; Initialization
LDI r1, 5        ; r1 = 5
LDI r2, 3        ; r2 = 3
LDI r3, 0xFF     ; r3 = 255

; Arithmetic operations
ADD r4, r1, r2   ; r4 = r1 + r2 (5 + 3 = 8)
SUB r5, r1, r2   ; r5 = r1 - r2 (5 - 3 = 2)
AND r6, r1, r3   ; r6 = r1 & r3 (5 & 255 = 5)

; Memory operations
LDI64 r10, 0x22100   ; r10 = 256 (memory address)
ST r10, r1     ; mem[256] = r1 (5)
LD r7, r10     ; r7 = mem[256] (5)

; Control flow
LDI r8, 0        ; r8 = 0
JZ .skip, r8     ; Jump to .skip since r8 == 0
LDI r9, 42       ; This will be skipped
.skip:
LDI r9, 24       ; r9 = 24

; Final operation
HLT              ; Halt execution