package jpabook.jpashop.api;

import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
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
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

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

    // Lazy loading으로 많은 호출이 일어남, Order Delivery, Member총 3가지 Table을 사용
    // 즉, N+1의 문제를 일으킴
    // 1 + 회원 N + Delivery N으로 많으 쿼리가 이용됨 -> fetch join으로 해결가능
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {
        return orderRepository.findAllByString(new OrderSearch())
            .stream()
            .map(SimpleOrderDto::new)
            .collect(toList());
    }

    // fetch join을 통해서 기존 5개에서 쿼리가 1개로 줄어듬
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {
        final List<Order> orders = orderRepository.findALlWithMemberDelivery();

        return orders.stream()
            .map(SimpleOrderDto::new)
            .collect(Collectors.toList());
    }

    // 컬럼이 적어져 좀더 성능이 최적화 되지만, 재사용성이 현격히 적어짐
    // 또한, 코드도 지저분함 -> 새로운 성능 최적화용, Repository를 만들어 해결
    // Reposiotry는 가능한 순수한 엔티티를 다루는데 사용
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }


}
