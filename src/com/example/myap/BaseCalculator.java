package com.example.myap;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

//运算功能

class BaseCalculator {

    private final char[] operSet = {'+', '-', '×', '/', '(', ')', '#'}; //运算符数组

    //Map结构方便后面取运算符的下标
    private final Map<Character, Integer> operMap = new HashMap<Character, Integer>() {{
        put('+', 0);
        put('-', 1);
        put('×', 2);
        put('/', 3);
        put('(', 4);
        put(')', 5);
        put('#', 6);
    }};

    //运算符优先级表，operPrior[oper1下标][oper2下标]
    private final char[][] operPrior = {
       /* (o1,o2)  +    -    ×    /    (    )    # */
       /*  +  */ {'>', '>', '<', '<', '<', '>', '>'},
       /*  -  */ {'>', '>', '<', '<', '<', '>', '>'},
       /*  ×  */ {'>', '>', '>', '>', '<', '>', '>'},
       /*  /  */ {'>', '>', '>', '>', '<', '>', '>'},
       /*  (  */ {'<', '<', '<', '<', '<', '=', ' '},
       /*  )  */ {'>', '>', '>', '>', ' ', '>', '>'},
       /*  #  */ {'<', '<', '<', '<', '<', ' ', '='},
    };

    //返回2个运算符优先级比较的结果'<','=','>'
    private char getPrior(char oper1, char oper2) {
        return operPrior[operMap.get(oper1)][operMap.get(oper2)]; //Map.get方法获取运算符的下标
    }

    //简单四则运算
    private double operate(double a, char oper, double b) {
        switch (oper) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '×':
                return a * b;
            case '/':
                if (b == 0) {
                    return Double.MAX_VALUE; //处理异常
                }
                return a / b;
            default:
                return 0;
        }
    }

    //计算普通的运算式
    private double calSubmath(String math) {
        if (math.length() == 0) {
            return Double.MAX_VALUE;
        } else {
            if (!hasOper(math.substring(1, math.length())) ) {
            	return Double.parseDouble(math);//输入的数只是一串数字，没有运算符，直接输出结果
            }

            //设置flag用于存储math开始位置的负数，如-3-5中的-3，避免-被识别成运算符而出错
            int flag = 0;
            if (math.charAt(0) == '-') {  //math.charAt(i)表示取math字符串下标为i的字符
                flag = 1;//输入计算式的第一个数为负数
                math = math.substring(1, math.length());
            }

            Stack<Character> operStack = new Stack<>(); //oper：操作栈
            Stack<Double> numStack = new Stack<>();     //num：数字栈

            operStack.push('#'); //设置栈底元素
            math += "#"; //为math字符串末尾添加优先级最低的#

            String tempNum = ""; //暂存数字str

            //计算math
            for (int i = 0; i < math.length(); i++) {

                char charOfMath = math.charAt(i); //遍历math中的char

                //(1)num进栈
                if (!isOper(charOfMath)                                         //1.不是oper
                        || charOfMath == '-' && math.charAt(i - 1) == '(') {    //2.是'-'并且'-'左边有'('，说明是在math中间用负数
                    tempNum += charOfMath;                                    

                    //1.1 获取下一个char
                    i++;
                    charOfMath = math.charAt(i);

                    //1.2 判断下一个char是不是oper,如果是oper，就将num压入numStack
                    if (isOper(charOfMath)) {   //此条件成功时，下次for循环就直接跳到else语句了
                        double num = Double.parseDouble(tempNum); //把tempNum里面的字符串转化为数字
                        if (flag == 1) {        //恢复math首位的负数
                            num = -num;
                            flag = 0;
                        }
                        numStack.push(num); //push num
                        tempNum = ""; //重置tempNum
                    }

                    //1.3 //回退，以免下次循环for语句自身的i++使得跳过了这个char
                    i--;
                }

                //(2)oper进栈
                else {

                    switch (getPrior(operStack.peek(), charOfMath)) {
                        //operStack.peek()取栈顶元素
                        //2.1 栈顶oper优先级低，新oper入栈
                        case '<':
                            operStack.push(charOfMath);
                            break;

                        //2.2 说明当前的charOfMath为')'，而栈顶oper为'('，去掉'('，其实也是math去括号的过程
                        case '=':   //() ##
                            operStack.pop();
                            break;

                        //2.3 栈顶oper优先级高，oper出栈，并将num运算结果push进numStack
                        case '>':
                            char oper = operStack.pop(); //取栈顶操作符
                            double b = numStack.pop(); //取出数字栈栈顶数据
                            double a = numStack.pop(); //取出数字栈栈顶数据
                            if (operate(a, oper, b) == Double.MAX_VALUE)
                                return Double.MAX_VALUE;
                            numStack.push(operate(a, oper, b));  //把结果放入数字栈中
                            i--; //继续比较该oper与栈顶oper的关系
                            break;
                    }
                }
            }
            return numStack.peek(); //最后的栈顶结果为最后的计算结果
        }
    }

    //计算math，添加了一些特殊math的处理
    double cal(String math) {
        if (math.length() == 0) { //处理异常
            return Double.MAX_VALUE;
        } else {
            //运算式只是数字的特征：从第2个char开始math中没有oper，比如-123或123
            if (!hasOper(math.substring(1, math.length())) ) {
                return Double.parseDouble(math);
            }
            //普通运算
            else {
                return calSubmath(math);
            }
        }
    }

    //判断String中是否有运算符
    private boolean hasOper(String s) {
        return s.contains("+") || s.contains("-") || s.contains("×") || s.contains("/");
    }

    //判断字符是否为运算符
    private boolean isOper(char c) {
        int i;
        for (i = 0; i < operSet.length; i++) {
            if (c == operSet[i]) {
                break;
            }
        }
        //break出来，说明是oper，i != operSize
        return i != operSet.length;  //i！=6说明 c是操作符
    }
}
