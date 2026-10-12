package net.kapitencraft.scripted.lang.exe;

public enum Opcode {
    RETURN, RETURN_ARG, THROW,
    TRACE,
    NULL(1), TRUE(1), FALSE(1),
    DUP(1), DUP_X1(1), DUP_X2(1), DUP2(2),
    DUP2_X1(2), DUP2_X2(2),
    POP(-1), POP_2(-1),
    GET(1), GET_0(1), GET_1(1), GET_2(1),
    ASSIGN(-1), ASSIGN_0(-1), ASSIGN_1(-1), ASSIGN_2(-1),
    SLICE(-3),
    ARRAY_LENGTH(-1),
    IA_NEW, DA_NEW, CA_NEW, FA_NEW, RA_NEW,
    IA_STORE(false, -3), DA_STORE(false, -3), CA_STORE(false, -3), FA_STORE(false, -3), RA_STORE(false, -3),
    IA_LOAD(-1), DA_LOAD(-1), CA_LOAD(-1), FA_LOAD(-1), RA_LOAD(-1),
    I_M1(1), I_0(1), I_1(1), I_2(1), I_3(1), I_4(1), I_5(1),
    D_M1(1), D_1(1),
    F_M1(1), F_1(1),
    I_CONST(1), D_CONST(1), S_CONST(1), F_CONST(1),
    I_SH_L(-1), I_SH_R(-1),
    IIRC,
    INVOKE_VIRTUAL(false, -1), INVOKE_STATIC(false, -1),
    CONCENTRATION(-1), D2F,
    INSTANCEOF,
    NOT,
    EQUAL(-1),
    NEQUAL(-1),
    I_LESSER(-1), D_LESSER(-1), F_LESSER(-1),
    I_GREATER(-1), D_GREATER(-1), F_GREATER(-1),
    I_LEQUAL(-1), D_LEQUAL(-1), F_LEQUAL(-1),
    I_GEQUAL(-1), D_GEQUAL(-1), F_GEQUAL(-1),
    I_NEGATION, D_NEGATION, F_NEGATION,
    I_ADD(-1), D_ADD(-1), F_ADD(-1),
    I_SUB(-1), D_SUB(-1), F_SUB(-1),
    I_MUL(-1), D_MUL(-1), F_MUL(-1),
    I_DIV(-1), D_DIV(-1), F_DIV(-1),
    I_POW(-1), D_POW(-1), F_POW(-1),
    I_MOD(-1), D_MOD(-1), F_MOD(-1),
    JUMP(false, 0), JUMP_IF_FALSE(false, -1), SWITCH(-1),
    GET_FIELD, GET_STATIC(1), PUT_FIELD(false, -2), PUT_STATIC(false, -1), NEW(false, 1);

    /**
     * determines whether the given Opcode is pure. pure operations are free of side effects, like changing class attributes
     */
    private final boolean pure;
    private final int stackChange;

    Opcode(boolean pure, int stackChange) {
        this.pure = pure;
        this.stackChange = stackChange;
    }

    Opcode(int stackChange) {
        this(true, stackChange);
    }

    Opcode() {
        this(0);
    }

    public static Opcode byId(int offset) {
        return values()[offset];
    }

    public boolean isPure() {
        return pure;
    }

    public int getStackChange() {
        return stackChange;
    }
}
