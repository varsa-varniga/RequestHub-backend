package com.varniga.requestmanagement.config;

import com.varniga.requestmanagement.entity.RequestStatus;
import com.varniga.requestmanagement.repository.RequestStatusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RequestStatusRepository statusRepository;

    @Override
    public void run(String... args) throws Exception {
        if(statusRepository.count() == 0) {
            statusRepository.save(RequestStatus.builder()
                    .name("PENDING")
                    .displayName("Pending")
                    .colorCode("#FFA500")
                    .build());

            statusRepository.save(RequestStatus.builder()
                    .name("APPROVED")
                    .displayName("Approved")
                    .colorCode("#00FF00")
                    .build());

            statusRepository.save(RequestStatus.builder()
                    .name("REJECTED")
                    .displayName("Rejected")
                    .colorCode("#FF0000")
                    .build());

            System.out.println("RequestStatus seeded successfully!");
        }
    }
}