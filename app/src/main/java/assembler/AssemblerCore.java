package assembler;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main entry point for the Motorola 6800 assembler (console version).
 */
public class AssemblerCore {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java assembler.AssemblerCore <input.asm>");
            return;
        }
        String inputPath = args[0];
        List<String> lines = Files.readAllLines(Paths.get(inputPath));

        InstructionSetManager instructionSet = new InstructionSetManager();
        LabelResolver labelResolver = new LabelResolver();
        Parser parser = new Parser();
        List<AssemblyLine> parsedLines = parser.parse(lines);
        CodeGenerator codeGen = new CodeGenerator(instructionSet, labelResolver);
        List<Byte> machineCode = codeGen.generate(parsedLines);

        System.out.println("Assembly completed. Machine code length: " + machineCode.size());
        // ... İstenirse makine kodunu dosyaya yazma eklenebilir ...
    }
}
