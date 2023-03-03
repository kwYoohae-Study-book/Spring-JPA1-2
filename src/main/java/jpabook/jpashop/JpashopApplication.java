package jpabook.jpashop;

import java.util.Arrays;
import java.util.HashMap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JpashopApplication {

  public static void main(String[] args) {
    SpringApplication.run(JpashopApplication.class, args);

  }
  public int[] solution(int[] emergency) {
    int[] answer = new int[emergency.length];
    HashMap<Integer, Integer> map = new HashMap<>();

    int[] copy = Arrays.copyOf(emergency, emergency.length);
    Arrays.sort(copy);

    for (int i = 1; i <= copy.length; i++) {
      map.put(copy[i - 1] , i);
    }

    for (int i = 0; i < emergency.length; i++) {
      answer[i] = map.get(emergency[i]);
    }
    return answer;
  }
}
