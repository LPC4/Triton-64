; Test program to demonstrate JPP and JPN using SUB for comparisons
LDI s1, 30                  ; Load 10 into s1
LDI s2, 20                  ; Load 20 into s2
SUB s3, s1, s2              ; s3 = s1 - s2 (10 - 20 = -10)
JPP gt, s3                 ; Jump to .greater if s3 > 0 (s1 > s2)
JPN lt, s3                 ; Jump to .less if s3 < 0 (s1 < s2)
JZ eq, s3                  ; Jump to .equal if s3 == 0 (s1 == s2)
HLT                         ; Halt if none of the conditions match (should not reach)
gt:
LDI s4, 1                   ; Set s4 = 1 to indicate s1 > s2
JMP done                   ; Jump to end
lt:
LDI s4, -1                  ; Set s4 = -1 to indicate s1 < s2
JMP done                   ; Jump to end
eq:
LDI s4, 0                   ; Set s4 = 0 to indicate s1 == s2
done:
HLT                         ; Halt the program