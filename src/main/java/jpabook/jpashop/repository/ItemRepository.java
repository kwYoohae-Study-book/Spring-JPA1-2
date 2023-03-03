package jpabook.jpashop.repository;

import java.util.List;
import javax.persistence.EntityManager;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) { // 처음에 저장할떄는 없음 (새로운 객체)
            em.persist(item);
        } else {
            em.merge(item); // 병합방법, 이보다, 변경감지를 사용하는것이 더 좋음
            // 새롭게 변경된 코드를 아예 새롭게 변경하는 방법임
            // 모든 속성이 변경되므로, 위험함, 변경감지는 변경된 값만 변경됨
        }
    }

     public Item findOne(Long id) {
        return em.find(Item.class, id);
     }

     public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
            .getResultList();
     }
}
