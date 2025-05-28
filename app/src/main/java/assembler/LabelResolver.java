package assembler;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves labels and assigns addresses in the assembly code.
 */
public class LabelResolver {
    private final Map<String, Integer> labelAddressMap = new HashMap<>();

    /**
     * Registers a label with its address.
     */
    public void addLabel(String label, int address) {
        labelAddressMap.put(label, address);
    }

    /**
     * Gets the address for a label, or null if not found.
     */
    public Integer getAddress(String label) {
        return labelAddressMap.get(label);
    }

    /**
     * Checks if a label is already defined.
     */
    public boolean isDefined(String label) {
        return labelAddressMap.containsKey(label);
    }
}
