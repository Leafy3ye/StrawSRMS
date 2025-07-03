package com.example.coffee_management_system_backend.controller;

import com.example.coffee_management_system_backend.model.TableEntity;
import com.example.coffee_management_system_backend.service.TableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "*")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public List<TableEntity> getAllTables() {
        return tableService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableEntity> getTableById(@PathVariable Integer id) {
        return tableService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createTable(@RequestBody TableEntity tableEntity) {
        try {
            // 使用原生SQL插入，确保数据正确保存
            tableService.saveWithSql(tableEntity.getName(), tableEntity.getStatus());
            return ResponseEntity.ok("桌位创建成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("桌位创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateTable(@PathVariable Integer id, @RequestBody TableEntity tableEntity) {
        try {
            // 先检查桌位是否存在 - 使用 !isPresent() 替代 isEmpty()
            if (!tableService.findById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            // 使用原生SQL更新
            tableService.updateWithSql(id, tableEntity.getName(), tableEntity.getStatus());
            return ResponseEntity.ok("桌位更新成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("桌位更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTable(@PathVariable Integer id) {
        try {
            // 使用原生SQL删除
            tableService.deleteByIdWithSql(id);
            return ResponseEntity.ok("桌位删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("桌位删除失败: " + e.getMessage());
        }
    }

    // 新增接口：获取某桌位未完成订单总金额
    @GetMapping("/{id}/total")
    public ResponseEntity<Double> getTableTotalAmount(@PathVariable Integer id) {
        Double totalAmount = tableService.getUncompletedOrderTotalAmountByTableId(id);
        return ResponseEntity.ok(totalAmount);
    }
}