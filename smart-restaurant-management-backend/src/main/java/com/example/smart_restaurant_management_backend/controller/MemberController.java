package com.example.smart_restaurant_management_backend.controller;

import com.example.smart_restaurant_management_backend.dto.RechargeRequestDTO;
import com.example.smart_restaurant_management_backend.dto.DeductRequestDTO;
import com.example.smart_restaurant_management_backend.model.Member;
import com.example.smart_restaurant_management_backend.model.RechargeRecord;
import com.example.smart_restaurant_management_backend.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "*")
public class MemberController {

    @Autowired
    private MemberService memberService;

    // 获取所有会员
    @GetMapping
    public List<Member> getAllMembers() {
        return memberService.getAllMembers();
    }

    // 根据ID获取会员
    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        Optional<Member> member = memberService.getMemberById(id);
        return member.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // 添加会员
    @PostMapping
    public Member addMember(@RequestBody Member member) {
        return memberService.addMember(member);
    }

    // 编辑会员
    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member member) {
        Member updatedMember = memberService.updateMember(id, member);
        return updatedMember != null ? ResponseEntity.ok(updatedMember) : ResponseEntity.notFound().build();
    }

    // 删除会员
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok().build();
    }

    // 搜索会员
    @GetMapping("/search")
    public List<Member> searchMembers(@RequestParam String keyword) {
        return memberService.searchMembers(keyword);
    }

    // 会员充值
    @PostMapping("/recharge")
    public ResponseEntity<Member> recharge(@RequestBody RechargeRequestDTO rechargeRequest) {
        Member member = memberService.recharge(rechargeRequest);
        return member != null ? ResponseEntity.ok(member) : ResponseEntity.badRequest().build();
    }

    // 扣除会员余额
    @PostMapping("/deduct")
    public ResponseEntity<Member> deductBalance(@RequestBody DeductRequestDTO deductRequest) {
        Member member = memberService.deductBalance(deductRequest);
        return member != null ? ResponseEntity.ok(member) : ResponseEntity.badRequest().build();
    }

    // 获取会员充值记录
    @GetMapping("/{id}/recharge-records")
    public List<RechargeRecord> getMemberRechargeRecords(@PathVariable Long id) {
        return memberService.getMemberRechargeRecords(id);
    }
}