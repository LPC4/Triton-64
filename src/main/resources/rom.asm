LDIU sp, 0x20020000 ; Set stack pointer to top of RAM
LDIU hp, 0x20000000 ; Set heap pointer to start of RAM
LDIU gp, 0x20000000 ; Set global pointer to start of RAM
LDIU t6, 0x00020000 ; Set t6 to start of RAM, using t7 as it shouldnt interfere with other expansions
                    ; t6 = JMP register for labels, but we're doing it manually
JMP t6              ; Jump to start of RAM, we're jumping to register so no temps clobbered
