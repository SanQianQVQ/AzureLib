package mod.azure.azurelib.core.math;

/**
 * Ternary operator class This value implementation allows to return different values depending on given condition value
 */
public class Ternary implements IValue {

    public final IValue condition;

    public final IValue ifTrue;

    public final IValue ifFalse;

    public Ternary(IValue condition, IValue ifTrue, IValue ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public double get() {
        return this.condition.get() != 0 ? this.ifTrue.get() : this.ifFalse.get();
    }

    @Override
    public String toString() {
        return this.condition.toString() + " ? " + this.ifTrue.toString() + " : " + this.ifFalse.toString();
    }
}
