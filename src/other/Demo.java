package other;

public class Demo {
    public void method1() {
        ClassA a = new ClassA();
        a.ma4();
        a.ma2();
        int numA = 0;
        int numB = a.ma1(numA);
    }

    public void method2() {
        ClassB b = new ClassB();
        b.mb3();
        b.mb6();
        ClassA a = new ClassA();
        a.ma2();
        String strA = "testStr";
        String strB = a.ma3(strA);
    }

    public void method3() {
        ClassA a = new ClassA();
        a.ma4();
        a.ma2();
        int intA = 6;
        int intB = a.ma1(intA);
    }
}
