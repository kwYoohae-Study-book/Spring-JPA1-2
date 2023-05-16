package jpabook.jpashop.repository.order.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;


    // N + 1 발생
    public List<OrderQueryDto> findOrderQueryDtos() {
        final List<OrderQueryDto> result = findOrders(); // query 1번 -> 2(N)개

        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); // Query N번
            o.setOrderItems(orderItems);
        });

        return result;
    }

    public List<OrderQueryDto> findAllByDto_optimization() {
        final List<OrderQueryDto> result = findOrders();

        final List<OrderItemQueryDto> orderItems = findOrderItemMap(toOrderIds(result));

        final Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream().
            collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private List<OrderItemQueryDto> findOrderItemMap(final List<Long> orderIds) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
                    + " from OrderItem oi"
                    + " join oi.item i"
                    + " where oi.order.id in :orderIds", OrderItemQueryDto.class)
            .setParameter("orderIds", orderIds)
            .getResultList();
    }

    private static List<Long> toOrderIds(final List<OrderQueryDto> result) {
        return result.stream().map(o -> o.getOrderId())
            .collect(Collectors.toList());
    }

    private List<OrderItemQueryDto> findOrderItems(final Long orderId) {
        return em.createQuery(
            "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
                + " from OrderItem oi"
                + " join oi.item i"
                + " where oi.order.id = :orderId", OrderItemQueryDto.class
        ).setParameter("orderId", orderId)
            .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
            "SELECT new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"
                + " FROM Order o"
                + " join o.member m"
                + " join o.delivery d", OrderQueryDto.class
        ).getResultList();
    }
}
