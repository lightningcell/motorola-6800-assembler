package assembler;

import java.util.*;

public class ExampleAssembler {
    public static void main(String[] args) {
        // Örnek assembly kodu
        List<String> lines = Arrays.asList(
            "ORG   $8000",
            "START:  LDAA  $9000",
            "ADDA  #1",
            "STAA  $9001",
            "JMP   START",
            "END"
        );

        InstructionSetManager instructionSet = new InstructionSetManager();
        LabelResolver labelResolver = new LabelResolver();
        Parser parser = new Parser();
        List<AssemblyLine> parsedLines = parser.parse(lines);
        CodeGenerator codeGen = new CodeGenerator(instructionSet, labelResolver);
        List<Byte> machineCode = codeGen.generate(parsedLines);

        System.out.println("Makine Kodu:");
        for (Byte b : machineCode) {
            System.out.printf("%02X ", b);
        }
        System.out.println();
    }
}
