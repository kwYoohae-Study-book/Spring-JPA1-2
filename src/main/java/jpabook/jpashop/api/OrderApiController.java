package jpabook.jpashop.api;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.util.List;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.query.OrderDto;
import jpabook.jpashop.service.query.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        final List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // proxy 강제 초기화함 , 밑에도 함
            order.getDelivery().getAddress();
            final List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {

        final List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        final List<OrderDto> result = orders.stream()
            .map(o -> new OrderDto(o))
            .collect(toList());

        return result;
    }

    private final OrderQueryService orderQueryService;

    @GetMapping("/api/v3/orders")
    public List<OrderDto> orderV3() {

        return orderQueryService.orderV3();
    }

    // 그냥 가져오면, OrderItems가 fetch join안되어 있어 실행마다 쿼리발생
    // ToOne관계 페치 조인 이후, 지연로딩, batchsize설정
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> orderV3_page(
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findALlWithMemberDelivery(offset, limit);

        for (Order order : orders) {
            System.out.println("order ref= " + order + " id=" + order.getId());
        }

        final List<OrderDto> result = orders.stream()
            .map(o -> new OrderDto(o))
            .collect(toList());

        return result;
    }

    // N + 1 발생
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> orderV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    // 중복 데이터가 추가됨, Application에서의 추가 작업이 큼
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> orderV6() {
        final List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
            .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                    o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                    o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
            )).entrySet().stream()
            .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                e.getKey().getAddress(), e.getValue()))
            .collect(toList());

    }

//    @Getter
//    static class OrderDto {
//
//        private Long orderId;
//        private String name;
//        private LocalDateTime orderDate;
//        private OrderStatus orderStatus;
//        private Address address;
//        private List<OrderItemDto> orderItems; // DTO안에 Entity가 있으면 안됨 -> 외부에 노출되면안됨
//
//        public OrderDto(Order order) {
//            orderId = order.getId();
//            name = order.getMember().getName();
//            orderDate = order.getOrderDate();
//            orderStatus = order.getStatus();
//            address = order.getDelivery().getAddress();
////            order.getOrderItems().stream().forEach(o -> o.getItem().getName()); // 프록시 초기화
////            orderItems = order.getOrderItems();
//            orderItems = order.getOrderItems().stream()
//                .map(orderItem -> new OrderItemDto(orderItem))
//                .collect(toList());
//        }
//    }
//
//    @Getter
//    static class OrderItemDto {
//
//        private String itemName; // 상품명
//        private int orderPrice; // 주문 가격
//        private int count; // 주문 수량
//
//        public OrderItemDto(final OrderItem orderItem) {
//            itemName = orderItem.getItem().getName();
//            orderPrice = orderItem.getOrderPrice();
//            count = orderItem.getCount();
//        }
//    }
}
