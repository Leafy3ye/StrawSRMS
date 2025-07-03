package com.example.coffee_management_system_backend.repository;

import com.example.coffee_management_system_backend.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // 根据手机号查询会员
    Optional<Member> findByPhone(String phone);
    
    // 根据邮箱查询会员
    Optional<Member> findByEmail(String email);
    
    // 根据姓名模糊查询会员
    List<Member> findByNameContaining(String name);
    
    // 根据手机号模糊查询会员
    List<Member> findByPhoneContaining(String phone);
}