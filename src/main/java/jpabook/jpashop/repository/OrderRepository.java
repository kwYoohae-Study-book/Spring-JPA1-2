package jpabook.jpashop.repository;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QOrder.order;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class OrderRepository {

    private final JPAQueryFactory query;
    private final EntityManager em;

    public OrderRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    // 1. 방법
    public List<Order> findAllByString(OrderSearch orderSearch) {
//        return em.createQuery("select o from Order o join o.member m" +
//                    " where o.status = :status" +
//                    " and m.name like :name"
//                , Order.class)
//            .setParameter("status", orderSearch.getOrderStatus())
//            .setParameter("name", orderSearch.getMemberName())
//            .setMaxResults(1000)
//            .getResultList();

        // 1. 방법
        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        // 주문 상태검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        // 파라미터 바인딩
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
            .setMaxResults(1000);

        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    /**
     * 방법 2 JPA Criteria 유지보수성이 없음, 어떤 쿼리를 사용하는지 바로 안보임
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        final Root<Order> o = cq.from(Order.class);
        final Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            final Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            final Predicate name = cb.like(m.<String>get("name"),
                "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        final TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

    public List<Order> findALlWithMemberDelivery() {
        return em.createQuery(
            "select o from Order o "
                + "join fetch o.member m "
                + "join fetch o.delivery d", Order.class
        ).getResultList();
    }

    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery
            ("select new jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) "
                + "from Order o "
                + "join o.member m "
                + "join o.delivery d", OrderSimpleQueryDto.class
            ).getResultList();
    }

    // 페치 조인으로 SQL이 1번만 실행된다는 장점
    // 페이징이 불가능하다는 단점
    public List<Order> findAllWithItem() {
        return em.createQuery(
            "SELECT distinct o FROM Order o" // distinct는 2가지 기능을한다는 것을 명심
                + " JOIN FETCH o.member m"
                + " JOIN FETCH o.delivery d"
                + " JOIN FETCH o.orderItems oi"
                + " JOIN FETCH oi.item i", Order.class
        ).getResultList();
    }

    public List<Order> findALlWithMemberDelivery(final int offset, final int limit) {
        return em.createQuery(
                "select o from Order o "
                    + "join fetch o.member m "
                    + "join fetch o.delivery d", Order.class
            ).setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    public List<Order> findAll(OrderSearch orderSearch) {

        return query.select(order)
            .from(order)
            .join(order.member, member)
            .where(statusEq(orderSearch.getOrderStatus()),nameLike(orderSearch.getMemberName()))
            .limit(1000)
            .fetch();
    }

    private BooleanExpression statusEq(OrderStatus orderStatus) {
        if (orderStatus == null) {
            return null;
        }
        return order.status.eq(orderStatus);
    }

    private BooleanExpression nameLike(String memberName) {
        if (!StringUtils.hasText(memberName)) {
            return null;
        }
        return member.name.like(memberName);
    }
}
