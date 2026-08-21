package com.learnspherex.batch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.learnspherex.batch.entity.Batch;
import com.learnspherex.batch.service.BatchService;

@Controller
public class BatchPageController {

    private final BatchService batchService;

    public BatchPageController(BatchService batchService) {
        this.batchService = batchService;
    }

    // =========================
    // SHOW ALL BATCHES
    // =========================
    @GetMapping("/batches")
    public String showBatches(Model model) {

        model.addAttribute("batches", batchService.getAllBatches());

        return "batches";
    }

    // =========================
    // SHOW CREATE FORM
    // =========================
    @GetMapping("/batches/new")
    public String showCreateBatchForm(Model model) {

        model.addAttribute("batch", new Batch());

        return "batch-form";
    }

    // =========================
    // CREATE BATCH
    // =========================
    @PostMapping("/batches")
    public String createBatch(@ModelAttribute Batch batch) {

        batchService.createBatch(batch);

        return "redirect:/batches";
    }

    // =========================
    // SHOW BATCH DETAILS
    // =========================
    @GetMapping("/batches/{id}")
    public String showBatchDetails(
            @PathVariable Long id,
            Model model) {

        Batch batch = batchService.getBatchById(id);

        model.addAttribute("batch", batch);

        return "batch-details";
    }

    // =========================
    // SHOW EDIT FORM
    // =========================
    @GetMapping("/batches/{id}/edit")
    public String showEditBatchForm(
            @PathVariable Long id,
            Model model) {

        Batch batch = batchService.getBatchById(id);

        model.addAttribute("batch", batch);

        return "batch-form";
    }

    // =========================
    // UPDATE BATCH
    // =========================
    @PostMapping("/batches/{id}")
    public String updateBatch(
            @PathVariable Long id,
            @ModelAttribute Batch batch) {

        batchService.updateBatch(id, batch);

        return "redirect:/batches";
    }

    // =========================
    // DELETE BATCH
    // =========================
    @PostMapping("/batches/{id}/delete")
    public String deleteBatch(@PathVariable Long id) {

        batchService.deleteBatch(id);

        return "redirect:/batches";
    }
}