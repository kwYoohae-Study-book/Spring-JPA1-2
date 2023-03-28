package jpabook.jpashop.api;

import java.util.List;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * X To One인 관계 (ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    // 1. 양방향으로 계속 접근해서 안됨 JsonIgnore
    // 2. Lazy 이므로 Proxy로 생성이 되기 때문에 Jackson이 가져올 수 없음
    // 3. 엔티티를 직접 보내야해서, 쓸모없는 정보도 가져가야해서 비효율적임
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        final List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // 강제로 Lazy를 초기화하는 방법
            order.getDelivery().getAddress();
        }
        return all;
    }

}
