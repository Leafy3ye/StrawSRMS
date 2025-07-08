package com.example.smart_restaurant_management_backend.controller;

import com.example.smart_restaurant_management_backend.model.MemberLevel;
import com.example.smart_restaurant_management_backend.service.MemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/member-levels")
@CrossOrigin(origins = "*")
public class MemberLevelController {

    @Autowired
    private MemberLevelService memberLevelService;

    // 获取所有会员等级
    @GetMapping
    public List<MemberLevel> getAllMemberLevels() {
        return memberLevelService.getAllMemberLevels();
    }

    // 根据ID获取会员等级
    @GetMapping("/{id}")
    public ResponseEntity<MemberLevel> getMemberLevelById(@PathVariable Long id) {
        Optional<MemberLevel> memberLevel = memberLevelService.getMemberLevelById(id);
        return memberLevel.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // 添加会员等级
    @PostMapping
    public ResponseEntity<?> addMemberLevel(@RequestBody MemberLevel memberLevel) {
        try {
            MemberLevel savedLevel = memberLevelService.addMemberLevel(memberLevel);
            return ResponseEntity.ok(savedLevel);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 更新会员等级
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMemberLevel(@PathVariable Long id, @RequestBody MemberLevel memberLevel) {
        try {
            MemberLevel updatedLevel = memberLevelService.updateMemberLevel(id, memberLevel);
            return updatedLevel != null ? ResponseEntity.ok(updatedLevel) : ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 删除会员等级
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemberLevel(@PathVariable Long id) {
        memberLevelService.deleteMemberLevel(id);
        return ResponseEntity.ok().build();
    }
}