package jpabook.jpashop;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;

    public Long save (Member member) {
        em.persist(member);
        return member.getId(); // 사이드 이펙트를 위한 방지
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }
}
