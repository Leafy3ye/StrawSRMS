package com.example.coffee_management_system_backend.service;

import com.example.coffee_management_system_backend.dto.RechargeRequestDTO;
import com.example.coffee_management_system_backend.dto.DeductRequestDTO;
import com.example.coffee_management_system_backend.model.Member;
import com.example.coffee_management_system_backend.model.RechargeRecord;
import com.example.coffee_management_system_backend.repository.MemberRepository;
import com.example.coffee_management_system_backend.repository.RechargeRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RechargeRecordRepository rechargeRecordRepository;
    
    @Autowired
    private EmailService emailService;

    // 获取所有会员
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // 根据ID获取会员 - 添加缓存
    @Cacheable(value = "members", key = "#id")
    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }
    
    // 获取会员余额 - 添加缓存
    @Cacheable(value = "memberBalance", key = "#memberId")
    public BigDecimal getMemberBalance(Long memberId) {
        return memberRepository.findById(memberId)
                .map(Member::getBalance)
                .orElse(BigDecimal.ZERO);
    }
    
    // 添加会员
    public Member addMember(Member member) {
        // 保存会员信息
        Member savedMember = memberRepository.save(member);
        
        // 发送欢迎邮件（如果邮箱不为空）
        if (savedMember.getEmail() != null && !savedMember.getEmail().trim().isEmpty()) {
            try {
                emailService.sendWelcomeEmail(
                    savedMember.getName(),
                    savedMember.getEmail(),
                    savedMember.getMemberLevel()
                );
            } catch (Exception e) {
                System.err.println("发送欢迎邮件失败，但会员添加成功: " + e.getMessage());
                // 邮件发送失败不影响会员添加
            }
        }
        
        return savedMember;
    }

    // 更新会员信息 - 更新缓存
    @CachePut(value = "members", key = "#id")
    @CacheEvict(value = "memberBalance", key = "#id")
    public Member updateMember(Long id, Member member) {
        Optional<Member> existingMember = memberRepository.findById(id);
        if (existingMember.isPresent()) {
            Member memberToUpdate = existingMember.get();
            memberToUpdate.setName(member.getName());
            memberToUpdate.setPhone(member.getPhone());
            memberToUpdate.setEmail(member.getEmail());
            memberToUpdate.setMemberLevel(member.getMemberLevel());
            return memberRepository.save(memberToUpdate);
        }
        return null;
    }

    // 删除会员 - 清除缓存
    @CacheEvict(value = {"members", "memberBalance"}, key = "#id")
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    // 搜索会员
    public List<Member> searchMembers(String keyword) {
        return memberRepository.findByNameContaining(keyword);
    }

    // 充值功能 - 清除相关缓存
    @CacheEvict(value = {"members", "memberBalance"}, key = "#rechargeRequest.memberId")
    @Transactional
    public Member recharge(RechargeRequestDTO rechargeRequest) {
        Optional<Member> memberOpt = memberRepository.findById(rechargeRequest.getMemberId());
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            
            // 更新会员余额
            BigDecimal newBalance = member.getBalance().add(rechargeRequest.getAmount());
            member.setBalance(newBalance);
            
            // 保存充值记录
            RechargeRecord record = new RechargeRecord();
            record.setMemberId(member.getId());
            record.setAmount(rechargeRequest.getAmount());
            record.setPaymentMethod(rechargeRequest.getPaymentMethod());
            record.setRemark(rechargeRequest.getRemark());
            rechargeRecordRepository.save(record);
            
            // 保存会员信息
            Member savedMember = memberRepository.save(member);
            
            // 发送充值成功邮件
            if (member.getEmail() != null && !member.getEmail().trim().isEmpty()) {
                try {
                    emailService.sendRechargeSuccessEmail(
                        member.getName(), 
                        member.getEmail(), 
                        rechargeRequest.getAmount(), 
                        newBalance
                    );
                } catch (Exception e) {
                    System.err.println("发送邮件失败，但充值操作已成功: " + e.getMessage());
                }
            }
            
            return savedMember;
        }
        return null;
    }

    // 获取会员充值记录
    public List<RechargeRecord> getMemberRechargeRecords(Long memberId) {
        return rechargeRecordRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    // 扣除会员余额 - 清除相关缓存
    @CacheEvict(value = {"members", "memberBalance"}, key = "#deductRequest.memberId")
    @Transactional
    public Member deductBalance(DeductRequestDTO deductRequest) {
        Optional<Member> memberOpt = memberRepository.findById(deductRequest.getMemberId());
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            
            // 检查余额是否足够
            if (member.getBalance().compareTo(deductRequest.getAmount()) >= 0) {
                // 扣除余额
                BigDecimal newBalance = member.getBalance().subtract(deductRequest.getAmount());
                member.setBalance(newBalance);
                
                // 保存会员信息
                Member savedMember = memberRepository.save(member);
                
                // 发送消费成功邮件
                if (savedMember.getEmail() != null && !savedMember.getEmail().trim().isEmpty()) {
                    try {
                        emailService.sendConsumeSuccessEmail(
                            savedMember, 
                            deductRequest.getAmount(),
                            deductRequest.getTableName() != null ? deductRequest.getTableName() : "未知桌位",
                            deductRequest.getConsumeItems() != null ? deductRequest.getConsumeItems() : Collections.singletonList("消费详情"),
                            deductRequest.getTotalItems() != null ? deductRequest.getTotalItems() : 1
                        );
                    } catch (Exception e) {
                        System.err.println("发送消费邮件失败，但不影响消费流程: " + e.getMessage());
                    }
                }
                
                return savedMember;
            }
        }
        return null;
    }
    
    // 保存会员 - 更新缓存
    @CachePut(value = "members", key = "#member.id")
    @CacheEvict(value = "memberBalance", key = "#member.id")
    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }
}