package com.job.tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.job.tracker.dto.ReferralDTO;
import com.job.tracker.service.ReferralService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/referrals")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class ReferralController {

    @Autowired
    private ReferralService referralService;

    @PostMapping
    public ResponseEntity<ReferralDTO.ReferralResponse> createReferral(
            @RequestBody ReferralDTO.CreateReferralRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        ReferralDTO.ReferralResponse response = referralService.createReferral(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ReferralDTO.ReferralListResponse> getAllReferrals(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        ReferralDTO.ReferralListResponse response = referralService.getAllReferrals(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReferralDTO.ReferralResponse> getReferral(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        ReferralDTO.ReferralResponse response = referralService.getReferral(id, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReferralDTO.ReferralResponse> updateReferral(
            @PathVariable Long id,
            @RequestBody ReferralDTO.UpdateReferralRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        ReferralDTO.ReferralResponse response = referralService.updateReferral(id, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReferral(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        referralService.deleteReferral(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<ReferralDTO.ReferralStats> getReferralStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        ReferralDTO.ReferralStats stats = referralService.getReferralStats(userId);
        return ResponseEntity.ok(stats);
    }
}
