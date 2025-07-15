package com.example.smart_restaurant_management_backend.controller;

import com.example.smart_restaurant_management_backend.model.TableEntity;
import com.example.smart_restaurant_management_backend.dto.TableDTO;
import com.example.smart_restaurant_management_backend.service.TableService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "*")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public List<TableDTO> getAllTables() {
        List<TableEntity> tables = tableService.findAll();
        return tables.stream()
                .map(TableDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableDTO> getTableById(@PathVariable Long id) {
        Optional<TableEntity> table = tableService.findById(id);
        if (table.isPresent()) {
            return ResponseEntity.ok(new TableDTO(table.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public TableDTO createTable(@RequestBody TableEntity table) {
        TableEntity savedTable = tableService.save(table);
        return new TableDTO(savedTable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TableDTO> updateTable(@PathVariable Long id, @RequestBody TableEntity tableDetails) {
        Optional<TableEntity> optionalTable = tableService.findById(id);
        if (optionalTable.isPresent()) {
            TableEntity table = optionalTable.get();
            table.setName(tableDetails.getName());
            table.setStatus(tableDetails.getStatus());
            TableEntity updatedTable = tableService.save(table);
            return ResponseEntity.ok(new TableDTO(updatedTable));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTable(@PathVariable Long id) {
        Optional<TableEntity> table = tableService.findById(id);
        if (table.isPresent()) {
            tableService.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/revenue/{id}")
    public ResponseEntity<Double> getTableRevenue(@PathVariable Long id) {
        try {
            double revenue = tableService.calculateTableRevenue(id);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}