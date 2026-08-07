package org.example.jobscheduler.controller;

import jakarta.validation.Valid;
import org.example.jobscheduler.dto.CreateJobRequest;
import org.example.jobscheduler.dto.InterruptResponse;
import org.example.jobscheduler.dto.JobDetailsResponse;
import org.example.jobscheduler.dto.JobResponse;
import org.example.jobscheduler.dto.UpdateJobRequest;
import org.example.jobscheduler.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduler")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/schedule")
    public ResponseEntity<JobResponse> schedule(@Valid @RequestBody CreateJobRequest request) {
        return ResponseEntity.ok(jobService.createJob(request));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobDetailsResponse> getJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getJob(jobId));
    }

    @PutMapping("/jobs/{jobId}")
    public ResponseEntity<JobDetailsResponse> updateJob(@PathVariable Long jobId,
                                                          @Valid @RequestBody UpdateJobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(jobId, request));
    }

    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long jobId) {
        jobService.deleteJob(jobId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/jobs/{jobId}/task/interrupt")
    public ResponseEntity<InterruptResponse> interrupt(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.interruptJob(jobId));
    }
}