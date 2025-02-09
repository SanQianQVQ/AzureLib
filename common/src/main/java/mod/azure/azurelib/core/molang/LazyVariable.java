/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.molang;

import mod.azure.azurelib.core.math.Variable;

import java.util.function.DoubleSupplier;

/**
 * Lazy override of Variable, to allow for deferred value calculation. <br>
 * Optimises rendering as values are not touched until needed (if at all)
 */
public class LazyVariable extends Variable {

    private DoubleSupplier valueSupplier;

    public LazyVariable(String name, double value) {
        this(name, () -> value);
    }

    public LazyVariable(String name, DoubleSupplier valueSupplier) {
        super(name, 0);

        this.valueSupplier = valueSupplier;
    }

    /**
     * Instantiates a copy of this variable from this variable's current value and name
     */
    public static LazyVariable from(Variable variable) {
        return new LazyVariable(variable.getName(), variable.get());
    }

    /**
     * Set the new value for the variable, acting as a constant
     */
    @Override
    public void set(double value) {
        this.valueSupplier = () -> value;
    }

    /**
     * Set the new value supplier for the variable
     */
    public void set(DoubleSupplier valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    /**
     * Get the current value of the variable
     */
    @Override
    public double get() {
        return this.valueSupplier.getAsDouble();
    }
}
