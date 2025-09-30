import java.util.*;

public class TabularTransposition {

    // Encrypt plaintext using a tabular transposition cipher
    public static String encrypt(String plaintext, int[] key, List<String> tableRows) {
        plaintext = plaintext.replaceAll("\\s+", "").toUpperCase();
        int cols = key.length;
        int rows = (int) Math.ceil((double) plaintext.length() / cols);
        char[][] matrix = new char[rows][cols];
        int index = 0;

        // Fill table row by row (pad with X if needed)
        for (int r = 0; r < rows; r++) {
            StringBuilder rowText = new StringBuilder();
            for (int c = 0; c < cols; c++) {
                if (index < plaintext.length()) {
                    matrix[r][c] = plaintext.charAt(index++);
                } else {
                    matrix[r][c] = 'X';
                }
                rowText.append(matrix[r][c]).append(" ");
            }
            tableRows.add(rowText.toString());
        }

        StringBuilder cipher = new StringBuilder();
        // Read columns according to key order (1-based to 0-based)
        for (int k = 1; k <= cols; k++) {
            for (int c = 0; c < cols; c++) {
                if (key[c] == k) {
                    for (int r = 0; r < rows; r++) {
                        cipher.append(matrix[r][c]);
                    }
                }
            }
        }
        return cipher.toString();
    }

    // Decrypt ciphertext
    public static String decrypt(String ciphertext, int[] key, List<String> tableRows) {
        int cols = key.length;
        int rows = ciphertext.length() / cols;
        char[][] matrix = new char[rows][cols];
        int index = 0;

        // Fill matrix column by column according to key order
        for (int k = 1; k <= cols; k++) {
            for (int c = 0; c < cols; c++) {
                if (key[c] == k) {
                    for (int r = 0; r < rows; r++) {
                        matrix[r][c] = ciphertext.charAt(index++);
                    }
                }
            }
        }

        // Save table rows for display
        for (int r = 0; r < rows; r++) {
            StringBuilder rowText = new StringBuilder();
            for (int c = 0; c < cols; c++) {
                rowText.append(matrix[r][c]).append(" ");
            }
            tableRows.add(rowText.toString());
        }

        StringBuilder plain = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                plain.append(matrix[r][c]);
            }
        }
        return plain.toString().replaceAll("X+$", "");
    }

    // Validate that text contains only letters
    private static boolean isAlphabetic(String text) {
        return text.matches("[a-zA-Z]+");
    }

    
    private static int[] readValidKey(Scanner sc) {
        while (true) {
            System.out.print("Enter numeric key (e.g. \"3 1 2\" or \"312\" for 3 columns): ");
            String input = sc.nextLine().trim();

            // allow only digits and spaces
            if (!input.matches("[0-9 ]+")) {
                System.out.println("Invalid input! Use digits and optional spaces (e.g. 3 1 2 or 312).\n");
                continue;
            }

            // If input contains spaces -> treat as space-separated numbers (supports multi-digit numbers)
            if (input.contains(" ")) {
                String[] parts = input.split("\\s+");
                int n = parts.length;
                int[] key = new int[n];
                boolean[] seen = new boolean[n + 1];
                boolean valid = true;

                for (int i = 0; i < n; i++) {
                    try {
                        key[i] = Integer.parseInt(parts[i]);
                    } catch (NumberFormatException e) { valid = false; break; }
                    // For space-separated mode we still require permutation of 1..n
                    if (key[i] < 1 || key[i] > n || seen[key[i]]) { valid = false; break; }
                    seen[key[i]] = true;
                }

                if (valid) return key;
                System.out.println("Invalid key! When using spaces, provide a permutation of 1.." + n + " (e.g. 3 1 2).\n");
                continue;
            }

            // No spaces -> contiguous digits mode, e.g. "312"
            String s = input;
            int n = s.length();
            int[] key = new int[n];
            boolean[] seen = new boolean[n + 1];
            boolean valid = true;
            for (int i = 0; i < n; i++) {
                int val = Character.getNumericValue(s.charAt(i));
                // contiguous-digit mode only supports single-digit values 1..n
                if (val < 1 || val > n || seen[val]) { valid = false; break; }
                key[i] = val;
                seen[val] = true;
            }
            if (valid) return key;
            System.out.println("Invalid key! When entering contiguous digits, they must be a permutation of 1.." + n + " (e.g. 312).\n");
        }
    }

    public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);

    while (true) {
        // Read plaintext
        System.out.print("Enter plaintext (letters only): ");
        String plaintext = sc.nextLine().replaceAll("\\s+", "");
        if (!isAlphabetic(plaintext)) {
            System.out.println("Invalid input! Only letters are allowed.\n");
            continue;
        }

        // Read and validate integer key
        int[] key = readValidKey(sc);

        // Encryption
        List<String> encTable = new ArrayList<>();
        String cipher = encrypt(plaintext, key, encTable);
        System.out.println("\n=== Encryption Table ===");
        for (String row : encTable) System.out.println(row);
        System.out.println("Ciphertext: " + cipher);

        // Decryption option
        String choice;
        while (true) {  // trap until valid y/n
            System.out.print("\nDo you want to decrypt the message? (y/n): ");
            choice = sc.nextLine().trim().toLowerCase();
            if (choice.equals("y") || choice.equals("n")) break;
            System.out.println("Invalid choice! Please enter 'y' or 'n'.");
        }

        if (choice.equals("y")) {
            System.out.print("Press 1 to decrypt the ciphertext just produced, or 2 to enter ciphertext manually (1/2): ");
            String which = sc.nextLine().trim();
            String ciphertextToDecrypt = cipher;

            if (which.equals("2")) {
                System.out.print("Enter ciphertext (letters only): ");
                String manual = sc.nextLine().replaceAll("\\s+", "");
                if (!isAlphabetic(manual)) {
                    System.out.println("Invalid input! Only letters are allowed.\n");
                } else {
                    ciphertextToDecrypt = manual;
                }
            }

            // Validate ciphertext length divisibility
            if (ciphertextToDecrypt.length() % key.length != 0) {
                System.out.println("Error: Ciphertext length must be divisible by key length for decryption.\n");
            } else {
                List<String> decTable = new ArrayList<>();
                String decrypted = decrypt(ciphertextToDecrypt, key, decTable);
                System.out.println("\n=== Decryption Table ===");
                for (String row : decTable) System.out.println(row);
                System.out.println("Decrypted Text: " + decrypted);
            }
        }

        // Ask if user wants to continue
        String again;
        while (true) {  // trap until valid y/n
            System.out.print("\nDo you want to encrypt another message? (y/n): ");
            again = sc.nextLine().trim().toLowerCase();
            if (again.equals("y") || again.equals("n")) break;
            System.out.println("Invalid choice! Please enter 'y' or 'n'.");
        }

        if (!again.equals("y")) {
            System.out.println("Exiting...");
            break;
        }
    }

    sc.close();
}
}