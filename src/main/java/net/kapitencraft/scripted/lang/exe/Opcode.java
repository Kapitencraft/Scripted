package net.kapitencraft.scripted.lang.exe;

public enum Opcode {
    RETURN, RETURN_ARG, THROW,
    TRACE,
    NULL, TRUE, FALSE,
    DUP, DUP_X1, DUP_X2, DUP2,
    DUP2_X1, DUP2_X2,
    POP, POP_2,
    GET, GET_0, GET_1, GET_2,
    ASSIGN, ASSIGN_0, ASSIGN_1, ASSIGN_2,
    SLICE,
    ARRAY_LENGTH,
    IA_NEW, DA_NEW, CA_NEW, FA_NEW, RA_NEW,
    IA_STORE, DA_STORE, CA_STORE, FA_STORE, RA_STORE,
    IA_LOAD, DA_LOAD, CA_LOAD, FA_LOAD, RA_LOAD,
    I_M1, I_0, I_1, I_2, I_3, I_4, I_5,
    D_M1, D_1,
    F_M1, F_1,
    I_CONST, D_CONST, S_CONST, F_CONST,
    INVOKE_VIRTUAL, INVOKE_STATIC,
    CONCENTRATION, D2F,
    AND, XOR, OR, NOT,
    EQUAL,
    NEQUAL,
    I_LESSER, D_LESSER, F_LESSER,
    I_GREATER, D_GREATER, F_GREATER,
    I_LEQUAL, D_LEQUAL, F_LEQUAL,
    I_GEQUAL, D_GEQUAL, F_GEQUAL,
    I_NEGATION, D_NEGATION, F_NEGATION,
    I_ADD, D_ADD, F_ADD,
    I_SUB, D_SUB, F_SUB,
    I_MUL, D_MUL, F_MUL,
    I_DIV, D_DIV, F_DIV,
    I_POW, D_POW, F_POW,
    I_MOD, D_MOD, F_MOD,
    JUMP, JUMP_IF_FALSE, SWITCH,
    GET_FIELD, GET_STATIC, PUT_FIELD, PUT_STATIC, NEW,
    REGISTRY;

    public static Opcode byId(int offset) {
        return values()[offset];
    }
}
