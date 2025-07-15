package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.context.TenantContext;
import com.example.smart_restaurant_management_backend.dto.RechargeRequestDTO;
import com.example.smart_restaurant_management_backend.dto.DeductRequestDTO;
import com.example.smart_restaurant_management_backend.model.Member;
import com.example.smart_restaurant_management_backend.model.RechargeRecord;
import com.example.smart_restaurant_management_backend.repository.MemberRepository;
import com.example.smart_restaurant_management_backend.repository.RechargeRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);
    
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RechargeRecordRepository rechargeRecordRepository;
    
    @Autowired
    private TenantEmailService tenantEmailService;

    // 获取当前租户和门店的所有会员
    public List<Member> getAllMembers() {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        return memberRepository.findByTenantIdAndStoreId(currentTenantId, currentStoreId);
    }

    // 根据ID获取当前租户和门店的会员
    @Cacheable(value = "members", key = "#id")
    public Optional<Member> getMemberById(Long id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        return memberRepository.findByIdAndTenantIdAndStoreId(id, currentTenantId, currentStoreId);
    }

    // 添加会员
    @CacheEvict(value = "members", allEntries = true)
    public Member addMember(Member member) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        
        // 设置租户ID和门店ID
        member.setTenantId(currentTenantId);
        member.setStoreId(currentStoreId);
        
        // 保存会员信息
        Member savedMember = memberRepository.save(member);
        
        // 发送欢迎邮件（如果邮箱不为空）
        if (savedMember.getEmail() != null && !savedMember.getEmail().trim().isEmpty()) {
            try {
                tenantEmailService.sendWelcomeEmail(
                    savedMember.getName(),
                    savedMember.getEmail(),
                    savedMember.getMemberLevel(),
                    currentTenantId
                );
            } catch (Exception e) {
                logger.error("发送欢迎邮件失败，但会员添加成功: {}", e.getMessage());
            }
        }
        
        return savedMember;
    }

    // 更新会员
    @CachePut(value = "members", key = "#id")
    public Member updateMember(Long id, Member member) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        
        Optional<Member> existingMemberOpt = memberRepository.findByIdAndTenantIdAndStoreId(id, currentTenantId, currentStoreId);
        if (existingMemberOpt.isPresent()) {
            Member existingMember = existingMemberOpt.get();
            existingMember.setName(member.getName());
            existingMember.setPhone(member.getPhone());
            existingMember.setEmail(member.getEmail());
            existingMember.setMemberLevel(member.getMemberLevel());
            existingMember.setBalance(member.getBalance());
            return memberRepository.save(existingMember);
        }
        return null;
    }

    // 删除会员
    @CacheEvict(value = "members", key = "#id")
    public void deleteMember(Long id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        
        Optional<Member> memberOpt = memberRepository.findByIdAndTenantIdAndStoreId(id, currentTenantId, currentStoreId);
        if (memberOpt.isPresent()) {
            memberRepository.delete(memberOpt.get());
        }
    }

    // 搜索会员
    public List<Member> searchMembers(String keyword) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        return memberRepository.findByTenantIdAndStoreIdAndNameContaining(currentTenantId, currentStoreId, keyword);
    }

    // 充值功能 - 清除相关缓存
    @CacheEvict(value = {"members", "memberBalance"}, key = "#rechargeRequest.memberId")
    @Transactional
    public Member recharge(RechargeRequestDTO rechargeRequest) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        
        // 使用租户和门店过滤的查询方法
        Optional<Member> memberOpt = memberRepository.findByIdAndTenantIdAndStoreId(rechargeRequest.getMemberId(), currentTenantId, currentStoreId);
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
            record.setTenantId(currentTenantId);
            record.setStoreId(currentStoreId);
            rechargeRecordRepository.save(record);
            
            // 保存会员信息
            Member savedMember = memberRepository.save(member);
            
            // 发送充值成功邮件
            if (member.getEmail() != null && !member.getEmail().trim().isEmpty()) {
                try {
                    tenantEmailService.sendRechargeSuccessEmail(
                        member.getName(), 
                        member.getEmail(), 
                        rechargeRequest.getAmount(), 
                        newBalance,
                        currentTenantId
                    );
                } catch (Exception e) {
                    logger.error("发送邮件失败，但充值操作已成功: {}", e.getMessage());
                }
            }
            
            return savedMember;
        }
        throw new RuntimeException("会员不存在或无权限访问");
    }

    // 扣除会员余额 - 清除相关缓存
    @CacheEvict(value = {"members", "memberBalance"}, key = "#deductRequest.memberId")
    @Transactional
    public Member deductBalance(DeductRequestDTO deductRequest) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        
        // 使用租户和门店过滤的查询方法
        Optional<Member> memberOpt = memberRepository.findByIdAndTenantIdAndStoreId(deductRequest.getMemberId(), currentTenantId, currentStoreId);
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
                        tenantEmailService.sendConsumeSuccessEmail(
                            savedMember, 
                            deductRequest.getAmount(),
                            deductRequest.getTableName() != null ? deductRequest.getTableName() : "未知桌位",
                            deductRequest.getConsumeItems() != null ? deductRequest.getConsumeItems() : Collections.singletonList("消费详情"),
                            deductRequest.getTotalItems() != null ? deductRequest.getTotalItems() : 1,
                            currentTenantId
                        );
                    } catch (Exception e) {
                        logger.error("发送消费邮件失败，但不影响消费流程: {}", e.getMessage());
                    }
                }
                
                return savedMember;
            } else {
                throw new RuntimeException("余额不足");
            }
        }
        throw new RuntimeException("会员不存在或无权限访问");
    }
    
    // 获取会员充值记录
    public List<RechargeRecord> getMemberRechargeRecords(Long memberId) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        return rechargeRecordRepository.findByMemberIdAndTenantIdAndStoreIdOrderByCreatedAtDesc(memberId, currentTenantId, currentStoreId);
    }
    
    // 保存会员 - 更新缓存
    @CachePut(value = "members", key = "#member.id")
    @CacheEvict(value = "memberBalance", key = "#member.id")
    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }
}