package com.example.myap;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

//���㹦��

class BaseCalculator {

    private final char[] operSet = {'+', '-', '��', '/', '(', ')', '#'}; //���������

    //Map�ṹ�������ȡ��������±�
    private final Map<Character, Integer> operMap = new HashMap<Character, Integer>() {{
        put('+', 0);
        put('-', 1);
        put('��', 2);
        put('/', 3);
        put('(', 4);
        put(')', 5);
        put('#', 6);
    }};

    //��������ȼ���operPrior[oper1�±�][oper2�±�]
    private final char[][] operPrior = {
       /* (o1,o2)  +    -    ��    /    (    )    # */
       /*  +  */ {'>', '>', '<', '<', '<', '>', '>'},
       /*  -  */ {'>', '>', '<', '<', '<', '>', '>'},
       /*  ��  */ {'>', '>', '>', '>', '<', '>', '>'},
       /*  /  */ {'>', '>', '>', '>', '<', '>', '>'},
       /*  (  */ {'<', '<', '<', '<', '<', '=', ' '},
       /*  )  */ {'>', '>', '>', '>', ' ', '>', '>'},
       /*  #  */ {'<', '<', '<', '<', '<', ' ', '='},
    };

    //����2����������ȼ��ȽϵĽ��'<','=','>'
    private char getPrior(char oper1, char oper2) {
        return operPrior[operMap.get(oper1)][operMap.get(oper2)]; //Map.get������ȡ��������±�
    }

    //����������
    private double operate(double a, char oper, double b) {
        switch (oper) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '��':
                return a * b;
            case '/':
                if (b == 0) {
                    return Double.MAX_VALUE; //�����쳣
                }
                return a / b;
            default:
                return 0;
        }
    }

    //������ͨ������ʽ
    private double calSubmath(String math) {
        if (math.length() == 0) {
            return Double.MAX_VALUE;
        } else {
            if (!hasOper(math.substring(1, math.length())) ) {
            	return Double.parseDouble(math);//�������ֻ��һ�����֣�û���������ֱ��������
            }

            //����flag���ڴ洢math��ʼλ�õĸ�������-3-5�е�-3������-��ʶ��������������
            int flag = 0;
            if (math.charAt(0) == '-') {  //math.charAt(i)��ʾȡmath�ַ����±�Ϊi���ַ�
                flag = 1;//�������ʽ�ĵ�һ����Ϊ����
                math = math.substring(1, math.length());
            }

            Stack<Character> operStack = new Stack<>(); //oper������ջ
            Stack<Double> numStack = new Stack<>();     //num������ջ

            operStack.push('#'); //����ջ��Ԫ��
            math += "#"; //Ϊmath�ַ���ĩβ������ȼ���͵�#

            String tempNum = ""; //�ݴ�����str

            //����math
            for (int i = 0; i < math.length(); i++) {

                char charOfMath = math.charAt(i); //����math�е�char

                //(1)num��ջ
                if (!isOper(charOfMath)                                         //1.����oper
                        || charOfMath == '-' && math.charAt(i - 1) == '(') {    //2.��'-'����'-'�����'('��˵������math�м��ø���
                    tempNum += charOfMath;                                    

                    //1.1 ��ȡ��һ��char
                    i++;
                    charOfMath = math.charAt(i);

                    //1.2 �ж���һ��char�ǲ���oper,�����oper���ͽ�numѹ��numStack
                    if (isOper(charOfMath)) {   //�������ɹ�ʱ���´�forѭ����ֱ������else�����
                        double num = Double.parseDouble(tempNum); //��tempNum������ַ���ת��Ϊ����
                        if (flag == 1) {        //�ָ�math��λ�ĸ���
                            num = -num;
                            flag = 0;
                        }
                        numStack.push(num); //push num
                        tempNum = ""; //����tempNum
                    }

                    //1.3 //���ˣ������´�ѭ��for��������i++ʹ�����������char
                    i--;
                }

                //(2)oper��ջ
                else {

                    switch (getPrior(operStack.peek(), charOfMath)) {
                        //operStack.peek()ȡջ��Ԫ��
                        //2.1 ջ��oper���ȼ��ͣ���oper��ջ
                        case '<':
                            operStack.push(charOfMath);
                            break;

                        //2.2 ˵����ǰ��charOfMathΪ')'����ջ��operΪ'('��ȥ��'('����ʵҲ��mathȥ���ŵĹ���
                        case '=':   //() ##
                            operStack.pop();
                            break;

                        //2.3 ջ��oper���ȼ��ߣ�oper��ջ������num������push��numStack
                        case '>':
                            char oper = operStack.pop(); //ȡջ��������
                            double b = numStack.pop(); //ȡ������ջջ������
                            double a = numStack.pop(); //ȡ������ջջ������
                            if (operate(a, oper, b) == Double.MAX_VALUE)
                                return Double.MAX_VALUE;
                            numStack.push(operate(a, oper, b));  //�ѽ����������ջ��
                            i--; //�����Ƚϸ�oper��ջ��oper�Ĺ�ϵ
                            break;
                    }
                }
            }
            return numStack.peek(); //����ջ�����Ϊ���ļ�����
        }
    }

    //����math�������һЩ����math�Ĵ���
    double cal(String math) {
        if (math.length() == 0) { //�����쳣
            return Double.MAX_VALUE;
        } else {
            //����ʽֻ�����ֵ��������ӵ�2��char��ʼmath��û��oper������-123��123
            if (!hasOper(math.substring(1, math.length())) ) {
                return Double.parseDouble(math);
            }
            //��ͨ����
            else {
                return calSubmath(math);
            }
        }
    }

    //�ж�String���Ƿ��������
    private boolean hasOper(String s) {
        return s.contains("+") || s.contains("-") || s.contains("��") || s.contains("/");
    }

    //�ж��ַ��Ƿ�Ϊ�����
    private boolean isOper(char c) {
        int i;
        for (i = 0; i < operSet.length; i++) {
            if (c == operSet[i]) {
                break;
            }
        }
        //break������˵����oper��i != operSize
        return i != operSet.length;  //i��=6˵�� c�ǲ�����
    }
}
