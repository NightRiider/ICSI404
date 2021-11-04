public class ALU {

    private boolean zeroFlag;
    private boolean negativeFlag;
    private boolean carryoutFlag;
    private boolean overflowFlag;

    public ALU() {
        zeroFlag = false;
        negativeFlag = false;
        carryoutFlag = false;
        overflowFlag = false;
    }

    public boolean getZF() {
        return zeroFlag;
    }

    public boolean getNF() {
        return negativeFlag;
    }

    public boolean getCF() {
        return carryoutFlag;
    }

    public boolean getOF() {
        return overflowFlag;
    }

    public void updateFlags(LongWord result) {
        if (result.isZero())
            zeroFlag = true;
        if (result.getBit(31))
            negativeFlag = true;
    }

    private void resetFlags() {
        zeroFlag = false;
        negativeFlag = false;
        carryoutFlag = false;
        overflowFlag = false;
    }

    public LongWord operate(int code, LongWord op1, LongWord op2) {

        resetFlags();
        LongWord result = new LongWord();

        if (code == 0) {
            result = op1.and(op2);
            updateFlags(result);
        } else if (code == 1) {
            result = op1.or(op2);
            updateFlags(result);
        } else if (code == 2) {
            result = op1.xor(op2);
            updateFlags(result);
        } else if (code == 3) {
            result = rippleCarryAdd(op1, op2, false);
            updateFlags(result);
        } else if (code == 4) {
            result = rippleCarryAdd(op1, op2, true);
            updateFlags(result);
        } else if (code == 5) {
            result = op1.shiftLeftLogical((int) op2.getUnsigned());
            updateFlags(result);
        } else if (code == 6) {
            result = op1.shiftRightLogical((int) op2.getUnsigned());
            if (result.isZero())
                zeroFlag = true;
        } else if (code == 7) {
            result = op1.shiftRightArithmetic((int) op2.getUnsigned());
            updateFlags(result);
        }

        return result;
    }

    private LongWord rippleCarryAdd(LongWord a, LongWord b, boolean cin) {
        LongWord result;
        LongWord origA = a;
        LongWord origB = b;
        // If false execute addition
        LongWord carry = new LongWord();
        if (!cin) {
            for (int i = 0; i < 32; i++) {


                if((carry.getBit(30) && b.getBit(31)))
                    carryoutFlag = true;

                // contains all the bits in both LongWOrd
                carry = a.and(b);


                // a now only has 1s in spots with 0 + 1
                a = a.xor(b);

                // b now become the result of shifting the carry by one
                b = carry.shiftLeftLogical(1);


                // If there's nothing left to add, break out of loop
 //               if(b.isZero())
 //                   break;

                //System.out.println("C " + carry);

            }
            result = a;
            if (result.isZero()) {
                zeroFlag = true;
            }
            if (origA.getBit(31) == origB.getBit(31) && result.getBit(31) != origA.getBit(31))
                overflowFlag = true;
        } else { // subtract
            LongWord added;
            LongWord borrow;
            for (int i = 31; i > -1; i--) {

                if(i == 0 && !a.getBit(31)) {
                    carryoutFlag = true;
                }

                // flip the bits of the minuend (a)
                borrow = a.not();

                added = borrow.and(b);
                a = a.xor(b);
                b = added.shiftLeftLogical(1);

            }
            result = a;
            if (a.getBit(31) != b.getBit(31) && result.getBit(31) == b.getBit(31))
                overflowFlag = true;
        }
        return result;
    }

}
