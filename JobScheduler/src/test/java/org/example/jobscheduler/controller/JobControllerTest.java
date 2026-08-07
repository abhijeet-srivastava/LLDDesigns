package org.example.jobscheduler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.jobscheduler.dto.CreateJobRequest;
import org.example.jobscheduler.dto.InterruptResponse;
import org.example.jobscheduler.dto.JobDetailsResponse;
import org.example.jobscheduler.dto.JobResponse;
import org.example.jobscheduler.dto.UpdateJobRequest;
import org.example.jobscheduler.exception.JobNotFoundException;
import org.example.jobscheduler.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobService jobService;

    @Test
    void schedule_withValidRequest_returnsOk() throws Exception {
        when(jobService.createJob(any())).thenReturn(new JobResponse(1L, "hello", "QUEUED"));

        mockMvc.perform(post("/scheduler/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateJobRequest("desc", "hello", 1, "0 0 * * * *"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(1))
                .andExpect(jsonPath("$.jobStatus").value("QUEUED"));
    }

    @Test
    void schedule_withPriorityBelowRange_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/scheduler/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateJobRequest("desc", "hello", 0, "0 0 * * * *"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void schedule_withPriorityAboveRange_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/scheduler/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateJobRequest("desc", "hello", 6, "0 0 * * * *"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void schedule_withInvalidCron_returnsBadRequest() throws Exception {
        when(jobService.createJob(any())).thenThrow(new IllegalArgumentException("Invalid cron expression"));

        mockMvc.perform(post("/scheduler/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateJobRequest("desc", "hello", 1, "garbage"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getJob_withExistingId_returnsOk() throws Exception {
        when(jobService.getJob(1L)).thenReturn(
                new JobDetailsResponse(1L, "desc", "hello", 1, "0 0 * * * *", true, 10L, "QUEUED", null));

        mockMvc.perform(get("/scheduler/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(1));
    }

    @Test
    void getJob_withUnknownId_returnsNotFound() throws Exception {
        when(jobService.getJob(99L)).thenThrow(new JobNotFoundException(99L));

        mockMvc.perform(get("/scheduler/jobs/99")).andExpect(status().isNotFound());
    }

    @Test
    void updateJob_withValidRequest_returnsOk() throws Exception {
        when(jobService.updateJob(eq(1L), any())).thenReturn(
                new JobDetailsResponse(1L, "desc2", "hello", 1, "0 0 * * * *", true, 10L, "QUEUED", null));

        mockMvc.perform(put("/scheduler/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateJobRequest("desc2", null, null, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("desc2"));
    }

    @Test
    void updateJob_withUnknownId_returnsNotFound() throws Exception {
        when(jobService.updateJob(eq(99L), any())).thenThrow(new JobNotFoundException(99L));

        mockMvc.perform(put("/scheduler/jobs/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateJobRequest("desc2", null, null, null, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteJob_withExistingId_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/scheduler/jobs/1")).andExpect(status().isNoContent());
    }

    @Test
    void deleteJob_withUnknownId_returnsNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new JobNotFoundException(99L)).when(jobService).deleteJob(99L);

        mockMvc.perform(delete("/scheduler/jobs/99")).andExpect(status().isNotFound());
    }

    @Test
    void interrupt_withRunningTask_returnsInterruptedTrue() throws Exception {
        when(jobService.interruptJob(1L)).thenReturn(new InterruptResponse(1L, true));

        mockMvc.perform(post("/scheduler/jobs/1/task/interrupt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interrupted").value(true));
    }

    @Test
    void interrupt_withNoRunningTask_returnsInterruptedFalse() throws Exception {
        when(jobService.interruptJob(1L)).thenReturn(new InterruptResponse(1L, false));

        mockMvc.perform(post("/scheduler/jobs/1/task/interrupt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interrupted").value(false));
    }

    @Test
    void interrupt_withUnknownJobId_returnsNotFound() throws Exception {
        when(jobService.interruptJob(99L)).thenThrow(new JobNotFoundException(99L));

        mockMvc.perform(post("/scheduler/jobs/99/task/interrupt")).andExpect(status().isNotFound());
    }
}