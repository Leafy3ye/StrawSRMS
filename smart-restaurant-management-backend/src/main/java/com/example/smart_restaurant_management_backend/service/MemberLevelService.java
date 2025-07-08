package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.context.TenantContext;
import com.example.smart_restaurant_management_backend.model.MemberLevel;
import com.example.smart_restaurant_management_backend.repository.MemberLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberLevelService {

    @Autowired
    private MemberLevelRepository memberLevelRepository;

    // 获取当前租户的所有会员等级
    public List<MemberLevel> getAllMemberLevels() {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        return memberLevelRepository.findByTenantIdAndIsEnabledTrueOrderBySortOrder(currentTenantId);
    }

    // 根据ID获取会员等级
    public Optional<MemberLevel> getMemberLevelById(Long id) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        return memberLevelRepository.findByIdAndTenantId(id, currentTenantId);
    }

    // 添加会员等级
    public MemberLevel addMemberLevel(MemberLevel memberLevel) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }

        // 检查等级名称是否已存在
        if (memberLevelRepository.existsByTenantIdAndLevelName(currentTenantId, memberLevel.getLevelName())) {
            throw new RuntimeException("该等级名称已存在");
        }

        // 设置租户ID
        memberLevel.setTenantId(currentTenantId);
        
        // 如果没有设置排序权重，设置为最大值+1
        if (memberLevel.getSortOrder() == null) {
            List<MemberLevel> existingLevels = getAllMemberLevels();
            int maxOrder = existingLevels.stream()
                .mapToInt(MemberLevel::getSortOrder)
                .max()
                .orElse(0);
            memberLevel.setSortOrder(maxOrder + 1);
        }

        return memberLevelRepository.save(memberLevel);
    }

    // 更新会员等级
    public MemberLevel updateMemberLevel(Long id, MemberLevel memberLevel) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }

        Optional<MemberLevel> existingLevelOpt = memberLevelRepository.findByIdAndTenantId(id, currentTenantId);
        if (existingLevelOpt.isPresent()) {
            MemberLevel existingLevel = existingLevelOpt.get();
            
            // 检查等级名称是否与其他等级冲突
            if (memberLevelRepository.existsByTenantIdAndLevelNameAndIdNot(
                currentTenantId, memberLevel.getLevelName(), id)) {
                throw new RuntimeException("该等级名称已存在");
            }

            existingLevel.setLevelName(memberLevel.getLevelName());
            existingLevel.setDiscountRate(memberLevel.getDiscountRate());
            existingLevel.setPointRate(memberLevel.getPointRate());
            existingLevel.setBenefits(memberLevel.getBenefits());
            existingLevel.setUpgradeCondition(memberLevel.getUpgradeCondition());
            existingLevel.setSortOrder(memberLevel.getSortOrder());
            existingLevel.setIsEnabled(memberLevel.getIsEnabled());
            
            return memberLevelRepository.save(existingLevel);
        }
        return null;
    }

    // 删除会员等级（软删除）
    public void deleteMemberLevel(Long id) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }

        Optional<MemberLevel> memberLevelOpt = memberLevelRepository.findByIdAndTenantId(id, currentTenantId);
        if (memberLevelOpt.isPresent()) {
            MemberLevel memberLevel = memberLevelOpt.get();
            memberLevel.setIsEnabled(false);
            memberLevelRepository.save(memberLevel);
        }
    }
}