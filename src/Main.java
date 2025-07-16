import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Calculator {
    //bolbol
    public static void main(String[] args) {
        WelcomeMessage();
        CalculatorLoop();
    }

    static void WelcomeMessage() {
        System.out.println("Enter mathematical your expression please.");
        System.out.println("Supported operations: +, -, *, /");
    }

    static void CalculatorLoop() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = GetInput("Enter the expression: ", scanner);
            if (input.equalsIgnoreCase("exit")) { // הוספת אפשרות יציאה
                System.out.println("Exiting calculator. Goodbye!");
                break;
            }
            ProcessExpression(input);
        }
    }

    static String GetInput(String prompt, Scanner scanner) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    static void ProcessExpression(String expression) {
        try {
            String cleanedInput = CleanExpression(expression);

            float result = EvaluateMathematicalExpression(cleanedInput);

            DisplayResult(expression, result);
        } catch (IllegalArgumentException ex) {
            // Used for FormatException equivalents
            System.out.println("Input Error: " + ex.getMessage());
        } catch (ArithmeticException ex) {
            // Used for DivideByZeroException equivalents
            System.out.println("Error!! " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("An unexpected error occurred: " + ex.getMessage());
        }
    }

    static String CleanExpression(String expression) {
        return expression.replace(" ", "");
    }

    static void DisplayResult(String originalExpression, float result) {
        // Check if the float result is an integer (e.g., -3.0 should be displayed as -3)
        if (result == (long) result) {
            System.out.println("Result: " + originalExpression + " = " + (long) result);
        } else {
            System.out.println("Result: " + originalExpression + " = " + result);
        }
    }

    static float EvaluateMathematicalExpression(String expression) {
        List<String> tokens = TokenizeExpression(expression);

        List<Float> numbers = new ArrayList<>();
        List<Character> operators = new ArrayList<>();

        PopulateNumbersAndOperators(tokens, numbers, operators);

        // יש ליצור עותקים כי הפונקציות ישנו את הרשימות המקוריות
        List<Float> currentNumbers = new ArrayList<>(numbers);
        List<Character> currentOperators = new ArrayList<>(operators);

        ProcessHighPrecedenceOperations(currentNumbers, currentOperators);

        return ProcessLowPrecedenceOperations(currentNumbers, currentOperators);
    }

    static List<String> TokenizeExpression(String expression) {
        List<String> rawTokens = new ArrayList<>();

        // Regex שתוקן בפעם הקודמת: תומך במספרים שמתחילים בנקודה עשרונית (לדוגמה: .5)
        String pattern = "(\\d+(?:\\.\\d+)?|\\.\\d+)|([+\\-*/])|(.)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matches = regex.matcher(expression);

        int lastIndex = 0;

        while (matches.find()) {
            if (matches.start() > lastIndex) {
                throw new IllegalArgumentException("Invalid character sequence detected: '" +
                        expression.substring(lastIndex, matches.start()) + "'.");
            }

            if (matches.group(3) != null) {
                throw new IllegalArgumentException("Invalid character: '" + matches.group() + "'.");
            }

            rawTokens.add(matches.group());
            lastIndex = matches.end();
        }

        if (lastIndex < expression.length()) {
            throw new IllegalArgumentException("Invalid trailing characters: '" + expression.substring(lastIndex) + "'.");
        }

        List<String> finalTokens = new ArrayList<>();
        for (int i = 0; i < rawTokens.size(); i++) {
            String currentToken = rawTokens.get(i);

            // טיפול באופרטורים יחידים (Unary operators) כמו + ו-
            if (currentToken.equals("+") || currentToken.equals("-")) {
                // אם זהו האופרטור הראשון בביטוי, או שהוא מופיע מיד אחרי אופרטור אחר
                boolean isUnary = (i == 0) || (i > 0 && IsOperator(rawTokens.get(i - 1).charAt(0)));

                // טיפול מיוחד במקרה של --. נהפוך אותו ל-+.
                // זה יתבצע כאן לפני הטיפול הכללי ביונארי.
                if (i > 0 && currentToken.equals("-") && rawTokens.get(i-1).equals("-")) {
                    finalTokens.set(finalTokens.size() - 1, "+"); // שנה את ה- '-' הקודם ל- '+'
                    // כעת נמשיך כאילו זה היה '+' רגיל עם המספר שאחריו
                    if (i + 1 < rawTokens.size() && isParsableAsFloat(rawTokens.get(i + 1))) {
                        finalTokens.add(rawTokens.get(i + 1)); // הוסף את המספר
                        i++; // דלג על המספר שכבר שולב
                        continue; // עבור לאיטרציה הבאה
                    } else {
                        throw new IllegalArgumentException("Invalid expression: '-' must be followed by a number after another '-'.");
                    }
                }

                if (isUnary) {
                    if (i + 1 < rawTokens.size() && isParsableAsFloat(rawTokens.get(i + 1))) {
                        // צור מספר מאוחד (לדוגמה, "-5" או "+5")
                        finalTokens.add(currentToken + rawTokens.get(i + 1));
                        i++; // דלג על הטוקן הבא שכבר שולב
                        continue;
                    } else {
                        // אם יש אופרטור יחיד ללא מספר אחריו, זהו שגיאה
                        throw new IllegalArgumentException("Invalid expression: Operator " + currentToken + " must be followed by a number.");
                    }
                }
            }
            finalTokens.add(currentToken);
        }
        return finalTokens;
    }

    private static boolean isParsableAsFloat(String s) {
        try {
            Float.parseFloat(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static void PopulateNumbersAndOperators(List<String> tokens, List<Float> numbers, List<Character> operators) {
        for (String token : tokens) {
            try {
                float number = Float.parseFloat(token);
                numbers.add(number);
            } catch (NumberFormatException e) {
                // אם זה לא מספר, זה חייב להיות אופרטור יחיד תקין
                if (token.length() == 1 && IsOperator(token.charAt(0))) {
                    operators.add(token.charAt(0));
                } else {
                    // אם זה לא מספר ולא אופרטור תקין, זרוק שגיאה
                    throw new IllegalArgumentException("Invalid number or operator format: '" + token + "'. Please check your expression for typos or unexpected characters.");
                }
            }
        }

        // וודא שיש לפחות מספר אחד
        if (numbers.isEmpty()) {
            throw new IllegalArgumentException("Invalid expression: No valid numbers found.");
        }
        // וודא שמספר האופרטורים קטן ממספר המספרים (לדוגמה: 2 מספרים ו-1 אופרטור, או 1 מספר ו-0 אופרטורים)
        if (operators.size() >= numbers.size()) {
            throw new IllegalArgumentException("Invalid expression: Too many operators or missing operands for binary operations.");
        }
    }

    static void ProcessHighPrecedenceOperations(List<Float> numbers, List<Character> operators) {
        for (int i = operators.size() - 1; i >= 0; i--) { // Iterate backwards
            char op = operators.get(i);
            if (op == '*' || op == '/') {
                if (numbers.size() <= i || numbers.size() <= i + 1) { // Check for two operands
                    throw new IllegalArgumentException("Invalid expression: Missing operand for multiplication/division.");
                }

                float num1 = numbers.get(i);
                float num2 = numbers.get(i + 1);
                float result;

                if (op == '*') {
                    result = num1 * num2;
                } else { // op == '/'
                    if (num2 == 0) {
                        throw new ArithmeticException("Cannot divide by zero.");
                    }
                    result = num1 / num2;
                }

                operators.remove(i);
                numbers.remove(i + 1); // Remove right operand first
                numbers.set(i, result); // Update left operand with result
            }
        }
    }

    static float ProcessLowPrecedenceOperations(List<Float> numbers, List<Character> operators) {
        if (numbers.isEmpty()) {
            throw new IllegalArgumentException("Invalid expression: No numbers to calculate.");
        }

        // If only one number remains and no operators, it's the final result
        if (numbers.size() == 1 && operators.isEmpty()) {
            return numbers.get(0);
        }

        // Iterate backwards
        for (int i = operators.size() - 1; i >= 0; i--) {
            char op = operators.get(i);
            if (numbers.size() <= i || numbers.size() <= i + 1) { // Check for two operands
                throw new IllegalArgumentException("Invalid expression: Missing operand for addition/subtraction.");
            }

            float num1 = numbers.get(i);
            float num2 = numbers.get(i + 1);
            float result;

            if (op == '+') {
                result = num1 + num2;
            } else if (op == '-') {
                result = num1 - num2;
            } else {
                // This should not happen if high precedence operations are handled correctly
                throw new IllegalStateException("Unexpected operator " + op + " after precedence handling.");
            }

            operators.remove(i);
            numbers.remove(i + 1); // Remove right operand first
            numbers.set(i, result); // Update left operand with result
        }

        // After all operations, only one number should remain in the list
        if (numbers.size() != 1 || !operators.isEmpty()) {
            throw new IllegalStateException("Calculation error: Expected a single result but found multiple numbers or remaining operators.");
        }

        return numbers.get(0);
    }

    static boolean IsOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }
}