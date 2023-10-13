import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Test {
    public static void main(String[] args) {
        float f;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            System.out.print("please enter a number: ");
            try {
                f = Float.parseFloat(br.readLine());
                System.out.printf("%f\n", f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
