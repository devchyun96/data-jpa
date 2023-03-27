package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {
    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext EntityManager em;

    @Test
    public void testMember() throws Exception{
        //given
        System.out.println("memberRepository.getClass() = " + memberRepository.getClass());
        Member member=new Member("memberA");
        Member savedMember = memberRepository.save(member);
        //when
        Member findMember = memberRepository.findById(savedMember.getId()).get();
        //then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }
    @Test
    public void basicCRUD() throws Exception{
        //given
        Member member1=new Member("member1");
        Member member2=new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);
        //when
        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        //then
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);



        //리스트 조회 검증
        //when
        List<Member> all = memberRepository.findAll();
        //then
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        //when
        long count=memberRepository.count();
        //then
        assertThat(count).isEqualTo(2);

        //삭제 검증
        //when
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deleteCount=memberRepository.count();
        //then
        assertThat(deleteCount).isEqualTo(0);

    }

    @Test
    public void findByUsernameAndAgeGreaterThan() throws Exception{
        //given
        Member m1 =new Member("AAA",10);
        Member m2 =new Member("AAA",20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        //when
        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        //then
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testnamedQuery() throws Exception{
        //given
        Member m1 =new Member("AAA",10);
        Member m2 =new Member("BBB",20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        //when
        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        //then
        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    public void testQuery() throws Exception{
        //given
        Member m1 =new Member("AAA",10);
        Member m2 =new Member("BBB",20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        //when
        List<Member> result = memberRepository.findUser("AAA",10);
        //then
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void findUsernameList() throws Exception{
        //given
        Member m1 =new Member("AAA",10);
        Member m2 =new Member("BBB",20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        //when
        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s= " + s);
        }
        //then
    }

    @Test
    public void findMemberDto() throws Exception{
        //given
        Team team=new Team("teamA");
        teamRepository.save(team);

        Member m1 =new Member("AAA",10);
        m1.setTeam(team);
        memberRepository.save(m1);

        //when
        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto= " + dto);
        }
        //then
    }

    @Test
    public void findByNames() throws Exception{
        //given
        Member m1 =new Member("AAA",10);
        Member m2 =new Member("BBB",20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        //when
        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        //then
        for (Member member : result) {
            System.out.println("Member = " + member);
        }
    }

    @Test
    public void returnType() throws Exception{
        //given
        Member m1 =new Member("AAA",10);
        Member m2 =new Member("BBB",20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        //when
        Optional<Member> findOptional = memberRepository.findOptionalByUsername("AAA");
        System.out.println("findOptional = " + findOptional);
        //then


    }
    @Test
    public void paging() throws Exception{
        //given
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",10));
        memberRepository.save(new Member("member4",10));
        memberRepository.save(new Member("member5",10));

        int age=10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        //when
        Page<Member> page = memberRepository.findByAge(age,pageRequest);
        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));
        //then
        List<Member> content=page.getContent();
        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void bulkUpdate() throws Exception{
        //given
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",19));
        memberRepository.save(new Member("member3",20));
        memberRepository.save(new Member("member4",21));
        memberRepository.save(new Member("member5",40));
        //when
        int resultCount = memberRepository.bulkAgePlus(20);
        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);
        //then
        assertThat(resultCount).isEqualTo(3);
    }
    @Test
    public void findMemberLazy() throws Exception{
        //given
        //member1 -> teamA
        //member2 -> teamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1=new Member("member1",10,teamA);
        Member member2=new Member("member1",10,teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();
        //when
        List<Member> members = memberRepository.findEntityGraphByUsername("member1");

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }
        //then
    }
    @Test
    public void queryHint() throws Exception{
        //given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();
        //when
        Member findMember = memberRepository.findRealOnlyByUsername("member1");
        findMember.setUsername("member2");

        em.flush();
        //then
    }

    @Test
    public void lock() throws Exception{
        //given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();
        //when
        List<Member> result = memberRepository.findLockByUsername("member1");
        //then
    }

    @Test
    public void callCustom() throws Exception{
        //given
        List<Member> result = memberRepository.findMemberCustom();
        //when

        //then
    }
}