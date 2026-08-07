package org.example.jobscheduler.repository;

import org.example.jobscheduler.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
}