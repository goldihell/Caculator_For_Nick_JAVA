import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Calculator {

    public static void main(String[] args)
    {
        WelcomeMessage();
        CalculatorLoop();
    }

    static void WelcomeMessage()
    {
        System.out.println("Enter mathematical your expression please.");
        System.out.println("Supported operations: +, -, *, /");
    }


    static void CalculatorLoop() {
        Scanner scanner = new Scanner(System.in);
        while (true)
        {
            String input = GetInput("Enter the expression: ", scanner);
            ProcessExpression(input);
        }
    }

    static String GetInput(String prompt, Scanner scanner)
    {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    static void ProcessExpression(String expression)
    {
        try
        {
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

    static String CleanExpression(String expression)
    {
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


    static float EvaluateMathematicalExpression(String expression)
    {
        List<String> tokens = TokenizeExpression(expression);

        List<Float> numbers = new ArrayList<>();
        List<Character> operators = new ArrayList<>();

        PopulateNumbersAndOperators(tokens, numbers, operators);

        ProcessHighPrecedenceOperations(numbers, operators);

        return ProcessLowPrecedenceOperations(numbers, operators);
    }


    static List<String> TokenizeExpression(String expression)
    {
        List<String> rawTokens = new ArrayList<>();

        String pattern = "(\\d+(?:\\.\\d+)?)|([+\\-*/])|(.)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matches = regex.matcher(expression);

        int lastIndex = 0;

        while (matches.find())
        {
            if (matches.start() > lastIndex)
            {
                throw new IllegalArgumentException("Invalid character sequence detected: '" +
                        expression.substring(lastIndex, matches.start()) + "'.");
            }

            if (matches.group(3) != null)
            {
                throw new IllegalArgumentException("Invalid character: '" + matches.group() + "'.");
            }

            rawTokens.add(matches.group());
            lastIndex = matches.end();
        }

        if (lastIndex < expression.length())
        {
            throw new IllegalArgumentException("Invalid trailing characters: '" + expression.substring(lastIndex) + "'.");
        }

        List<String> finalTokens = new ArrayList<>();
        for (int i = 0; i < rawTokens.size(); i++)
        {
            String currentToken = rawTokens.get(i);

            if (currentToken.equals("-"))
            {
                if (i + 1 < rawTokens.size() && isParsableAsFloat(rawTokens.get(i + 1)))
                {
                    boolean isUnary = false;
                    if (i == 0) {
                        isUnary = true;
                    } else {
                        String prevToken = rawTokens.get(i - 1);

                        if (prevToken.length() == 1 && IsOperator(prevToken.charAt(0)) && !prevToken.equals("-")) {
                            isUnary = true;
                        } else if (prevToken.equals("-"))
                        {
                            finalTokens.set(finalTokens.size() - 1, "+");
                            finalTokens.add(rawTokens.get(i + 1));
                            i++;
                            continue;
                        }
                    }

                    if (isUnary)
                    {
                        finalTokens.add("-" + rawTokens.get(i + 1));
                        i++;
                        continue;
                    }
                }
            }
            finalTokens.add(currentToken);
        }
        return finalTokens;
    }

    private static boolean isParsableAsFloat(String s)
    {
        try {
            Float.parseFloat(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static void PopulateNumbersAndOperators(List<String> tokens, List<Float> numbers, List<Character> operators)
    {
        for (String token : tokens)
        {
            try
            {
                float number = Float.parseFloat(token);
                numbers.add(number);
            }
            catch (NumberFormatException e)
            {

                if (token.length() == 1 && IsOperator(token.charAt(0))) {
                    operators.add(token.charAt(0));
                } else
                {
                    throw new IllegalArgumentException("Invalid number or operator format: '" + token + "'. Please check your expression for typos or unexpected characters.");
                }
            }
        }
    }
//sdasdd
    static void ProcessHighPrecedenceOperations(List<Float> numbers, List<Character> operators)
    {
        for (int i = 0; i < operators.size(); i++)
        {
            char op = operators.get(i);
            if (op == '*' || op == '/')
            {
                if (numbers.size() <= i + 1)
                {
                    throw new IllegalArgumentException("Invalid expression: Missing operand for multiplication/division.");
                }

                float num1 = numbers.get(i);
                float num2 = numbers.get(i + 1);
                float result;

                if (op == '*')
                {
                    result = num1 * num2;
                } else { // op == '/'
                    if (num2 == 0)
                    {
                        throw new ArithmeticException("Cannot divide by zero.");
                    }
                    result = num1 / num2;
                }
            }
        }
    }

    static float ProcessLowPrecedenceOperations(List<Float> numbers, List<Character> operators)
    {
        if (numbers.isEmpty())
        {
            throw new IllegalArgumentException("Invalid expression: No numbers to calculate.");
        }

        float finalResult = numbers.get(0);

        for (int i = 0; i < operators.size(); i++)
        {
            if (numbers.size() <= i + 1)
            {
                throw new IllegalArgumentException("Invalid expression: Missing operand for addition/subtraction.");
            }

            char op = operators.get(i);
            float nextNumber = numbers.get(i + 1);

            if (op == '+')
            {
                finalResult += nextNumber;
            }
            else if (op == '-')
            {
                finalResult -= nextNumber;
            }
            else
            {
                throw new IllegalStateException("Unexpected operator " + op + " after precedence handling.");
            }
        }
        return finalResult;
    }

    static boolean IsOperator(char c)
    {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }
}
