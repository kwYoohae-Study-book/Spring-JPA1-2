package jpabook.jpashop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepositoryOld;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepositoryOld memberRepository;

    @Autowired
    EntityManager em;

    @Test
    @Rollback(false)
    void 회원가입() throws Exception {
        // given
        Member member = new Member();
        member.setName("kim");

        // when
        final Long savedId = memberService.join(member);

        // then
        em.flush();
        assertEquals(member, memberRepository.findOne(savedId));
    }

    @Test
    void 중복_회원_예외() throws Exception {
        // given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        // when
        memberService.join(member1);

        // then
        assertThrows(IllegalArgumentException.class, () -> {
            memberService.join(member2);
        });
    }
}